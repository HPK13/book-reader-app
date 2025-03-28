package com.example.e_book1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_REMOVE_EBOOK = 2;
    private List<Ebook> ebookList = new ArrayList<>();
    private EbookAdapter ebookAdapter;
    private EditText searchEditText;
    private Button addEbookButton;
    private Button removeEbookButton;
    private Button uploadPdfButton;

    private SharedPreferences sharedPreferences;
    private boolean isAdmin;
    private String selectedDocumentId; // To keep track of the selected document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        isAdmin = sharedPreferences.getBoolean("is_admin", false);

        searchEditText = findViewById(R.id.searchEditText);
        ListView bookListView = findViewById(R.id.bookListView);
        addEbookButton = findViewById(R.id.addEbookButton);
        removeEbookButton = findViewById(R.id.removeEbookButton);
        uploadPdfButton = findViewById(R.id.uploadPdfButton);

        ebookAdapter = new EbookAdapter(this, ebookList);
        bookListView.setAdapter(ebookAdapter);

        if (savedInstanceState != null) {
            // Restore the document ID if it was saved
            selectedDocumentId = savedInstanceState.getString("selectedDocumentId");
        }

        bookListView.setOnItemClickListener((parent, view, position, id) -> {
            Ebook selectedEbook = ebookList.get(position);
            selectedDocumentId = selectedEbook.getDocumentId(); // Save the selected document ID

            Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
            intent.putExtra("documentId", selectedDocumentId);
            startActivity(intent);
        });

        addEbookButton.setOnClickListener(v -> {
            if (isAdmin) {
                Intent intent = new Intent(MainActivity.this, AddEbookActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "You are not authorized to add ebooks.", Toast.LENGTH_SHORT).show();
            }
        });

        removeEbookButton.setOnClickListener(v -> {
            if (isAdmin) {
                Intent intent = new Intent(MainActivity.this, RemoveEbookActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REMOVE_EBOOK);
            } else {
                Toast.makeText(MainActivity.this, "You are not authorized to remove ebooks.", Toast.LENGTH_SHORT).show();
            }
        });

        uploadPdfButton.setOnClickListener(v -> {
            if (isAdmin) {
                Intent intent = new Intent(MainActivity.this, UploadPdfActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "You are not authorized to upload PDFs.", Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ebookAdapter.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });


        updateButtonVisibility();
        loadProfilePicture();
        loadEbooks();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the selected document ID
        if (selectedDocumentId != null) {
            outState.putString("selectedDocumentId", selectedDocumentId);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the selected document ID
        if (savedInstanceState != null) {
            selectedDocumentId = savedInstanceState.getString("selectedDocumentId");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEbooks(); // Refresh the list when returning to this activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_REMOVE_EBOOK && resultCode == RESULT_OK) {
            loadEbooks(); // Reload the list to reflect changes
        }
    }

    private void loadEbooks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ebooks")
                .whereEqualTo("isRemoved", false) // Only fetch eBooks that are not marked as removed
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Ebook> fetchedEbooks = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Ebook ebook = document.toObject(Ebook.class);
                        if (ebook != null) {
                            ebook.setDocumentId(document.getId());  // Set the document ID
                            fetchedEbooks.add(ebook);
                        }
                    }
                    ebookAdapter.clear();
                    ebookAdapter.addAll(fetchedEbooks);
                    ebookAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to load eBooks.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateButtonVisibility() {
        if (isAdmin) {
            addEbookButton.setVisibility(View.VISIBLE);
            removeEbookButton.setVisibility(View.VISIBLE);
            uploadPdfButton.setVisibility(View.VISIBLE);
        } else {
            addEbookButton.setVisibility(View.GONE);
            removeEbookButton.setVisibility(View.GONE);
            uploadPdfButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about_us) {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadProfilePicture() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String profilePictureUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
            ImageView profilePicture = findViewById(R.id.profilePicture);

            Glide.with(this)
                    .load(profilePictureUrl)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .into(profilePicture);
        }
    }
}
