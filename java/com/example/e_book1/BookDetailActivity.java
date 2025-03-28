package com.example.e_book1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookDetailActivity extends AppCompatActivity {

    private static final String TAG = "BookDetailActivity";

    private TextView titleTextView;
    private TextView authorTextView;
    private ImageView pdfImageView;
    private Button readButton;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitButton;
    private LinearLayout commentsLayout;
    private String documentId;
    public String pdfUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        titleTextView = findViewById(R.id.titleTextView);
        authorTextView = findViewById(R.id.authorTextView);
        pdfImageView = findViewById(R.id.pdfImageView);
        readButton = findViewById(R.id.readButton);
        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        submitButton = findViewById(R.id.submitButton);
        commentsLayout = findViewById(R.id.commentsLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the up button (back button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            documentId = savedInstanceState.getString("documentId");
        } else {
            Intent intent = getIntent();
            documentId = intent.getStringExtra("documentId");
        }

        if (documentId != null) {
            fetchPdfDetailsFromFirestore(documentId);
        } else {
            Log.e(TAG, "Missing book details: Document ID is null.");
            Toast.makeText(this, "Error: Missing book details.", Toast.LENGTH_SHORT).show();
        }

        readButton.setOnClickListener(v -> {
            if (pdfUrl != null) {
                Intent readerIntent = new Intent(BookDetailActivity.this, PdfReaderActivity.class);
                readerIntent.putExtra("pdfUrl", pdfUrl);
                startActivity(readerIntent);
            } else {
                Toast.makeText(BookDetailActivity.this, "PDF URL not available.", Toast.LENGTH_SHORT).show();
            }
        });

        submitButton.setOnClickListener(v -> submitRatingAndComment());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (documentId != null) {
            outState.putString("documentId", documentId);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            documentId = savedInstanceState.getString("documentId");
        }
    }

    private String getDisplayName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getDisplayName() != null ? user.getDisplayName() : "Anonymous"; // Use display name if available
        }
        return "Anonymous"; // Default name if user is not signed in
    }

    private void fetchPdfDetailsFromFirestore(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("ebooks").document(documentId);

        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve data from documentSnapshot
                        String title = documentSnapshot.getString("title");
                        String author = documentSnapshot.getString("author");
                        pdfUrl = documentSnapshot.getString("pdfUrl");
                        String filename = documentSnapshot.getString("fileName");

                        // Debug logs
                        Log.d(TAG, "Title: " + title);
                        Log.d(TAG, "Author: " + author);
                        Log.d(TAG, "PDF URL: " + pdfUrl);
                        Log.d(TAG, "Filename: " + filename);

                        // Update UI
                        titleTextView.setText(title != null ? title : "Unknown");
                        authorTextView.setText(author != null ? author : "Unknown");

                        if (pdfUrl != null) {
                            Log.d(TAG, "PDF URL successfully retrieved.");
                            downloadAndDisplayPdf(filename);
                        } else {
                            Log.e(TAG, "PDF URL is null.");
                        }

                        fetchRatingsAndComments();
                    } else {
                        Log.e(TAG, "No such document.");
                        Toast.makeText(BookDetailActivity.this, "Book details not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting document: ", e);
                    Toast.makeText(BookDetailActivity.this, "Failed to load book details.", Toast.LENGTH_SHORT).show();
                });
    }

    private void downloadAndDisplayPdf(String filename) {
        if (filename == null) {
            Log.e(TAG, "Filename is null, cannot download PDF.");
            return;
        }

        File pdfFile = new File(getFilesDir(), filename);
        if (pdfFile.exists()) {
            Log.d(TAG, "PDF file already exists locally.");
            displayPdfThumbnail(pdfFile);
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(pdfUrl);

            storageRef.getFile(pdfFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "PDF file downloaded successfully.");
                        displayPdfThumbnail(pdfFile);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to download PDF file: ", e);
                        Toast.makeText(BookDetailActivity.this, "Failed to download PDF file.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void displayPdfThumbnail(File pdfFile) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            PdfRenderer.Page page = pdfRenderer.openPage(0);

            int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
            int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            pdfImageView.setImageBitmap(bitmap);

            page.close();
            pdfRenderer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error displaying PDF thumbnail", e);
            Toast.makeText(this, "Failed to display PDF thumbnail.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchRatingsAndComments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ebooks").document(documentId).collection("comments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            displayRatingsAndComments(querySnapshot.getDocuments());
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch comments: ", task.getException());
                        Toast.makeText(BookDetailActivity.this, "Failed to load comments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayRatingsAndComments(List<DocumentSnapshot> comments) {
        commentsLayout.removeAllViews();
        for (DocumentSnapshot comment : comments) {
            String text = comment.getString("text");
            Double rating = comment.getDouble("rating");
            String username = comment.getString("username"); // Use display name

            if (text != null && rating != null && username != null) {
                TextView commentView = new TextView(this);
                commentView.setText("User: " + username + "\nRating: " + rating + " - " + text);
                commentsLayout.addView(commentView);
            }
        }
    }

    private void submitRatingAndComment() {
        String commentText = commentEditText.getText().toString();
        float ratingValue = ratingBar.getRating();
        String displayName = getDisplayName();

        if (commentText.isEmpty() || ratingValue == 0) {
            Toast.makeText(this, "Please enter a comment and rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> comment = new HashMap<>();
        comment.put("text", commentText);
        comment.put("rating", (double) ratingValue);
        comment.put("username", displayName); // Store display name

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ebooks").document(documentId).collection("comments")
                .add(comment)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(BookDetailActivity.this, "Comment submitted.", Toast.LENGTH_SHORT).show();
                        fetchRatingsAndComments();
                    } else {
                        Log.e(TAG, "Failed to submit comment: ", task.getException());
                        Toast.makeText(BookDetailActivity.this, "Failed to submit comment.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Clear the comment input after submission
        commentEditText.setText("");
        ratingBar.setRating(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
