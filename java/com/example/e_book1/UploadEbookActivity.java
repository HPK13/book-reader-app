package com.example.e_book1;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadEbookActivity extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;
    private EditText titleEditText;
    private EditText authorEditText;
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_ebook);

        titleEditText = findViewById(R.id.titleEditText);
        authorEditText = findViewById(R.id.authorEditText);
        uploadButton = findViewById(R.id.uploadPdfButton);

        if (isAdminUser()) {
            uploadButton.setVisibility(View.VISIBLE);
        } else {
            uploadButton.setVisibility(View.GONE);
        }

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });
    }

    private boolean isAdminUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_admin", false);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();
            uploadPdfToFirebase();
        }
    }

    private void uploadPdfToFirebase() {
        if (pdfUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference pdfRef = storageRef.child("ebooks/" + getFileName(pdfUri));

            UploadTask uploadTask = pdfRef.putFile(pdfUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pdfRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String pdfUrl = uri.toString();
                            saveEbookToDatabase(titleEditText.getText().toString(), authorEditText.getText().toString(), pdfUrl);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadEbookActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveEbookToDatabase(String title, String author, String pdfUrl) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String id = database.push().getKey();
        Ebook ebook = new Ebook(title, author, pdfUrl);
        if (id != null) {
            database.child("ebooks").child(id).setValue(ebook).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(UploadEbookActivity.this, "Ebook uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UploadEbookActivity.this, "Failed to upload ebook", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    cursor.moveToFirst();
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static class Ebook {
        public String title;
        public String author;
        public String pdfUrl;

        public Ebook() {
            // Default constructor required for calls to DataSnapshot.getValue(Ebook.class)
        }

        public Ebook(String title, String author, String pdfUrl) {
            this.title = title;
            this.author = author;
            this.pdfUrl = pdfUrl;
        }
    }
}
