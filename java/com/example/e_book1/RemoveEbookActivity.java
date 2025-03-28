package com.example.e_book1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class RemoveEbookActivity extends AppCompatActivity {
    private EditText titleEditText;
    private Button removeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_ebook);

        titleEditText = findViewById(R.id.titleEditText);
        removeButton = findViewById(R.id.removeButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEditText.getText().toString().trim();

                if (title.isEmpty()) {
                    Toast.makeText(RemoveEbookActivity.this, "Please enter a title.", Toast.LENGTH_SHORT).show();
                } else {
                    // Mark eBook as removed in Firestore
                    markEbookAsRemovedInFirestore(title);
                }
            }
        });
    }

    private void markEbookAsRemovedInFirestore(String title) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ebooks")
                .whereEqualTo("title", title)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(RemoveEbookActivity.this, "eBook not found.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Assuming there is only one document with this title
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            db.collection("ebooks").document(document.getId())
                                    .update("isRemoved", true)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RemoveEbookActivity.this, "eBook marked as removed successfully!", Toast.LENGTH_SHORT).show();
                                        // Notify MainActivity to refresh the list
                                        setResult(RESULT_OK);
                                        finish(); // Close the activity
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RemoveEbookActivity.this, "Failed to mark eBook as removed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RemoveEbookActivity.this, "Failed to find eBook: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
