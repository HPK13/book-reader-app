package com.example.e_book1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class UploadPdfActivity extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;
    private EditText titleEditText;
    private EditText authorEditText;
    private Button uploadPdfButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_ebook);

        titleEditText = findViewById(R.id.titleEditText);
        authorEditText = findViewById(R.id.authorEditText);
        uploadPdfButton = findViewById(R.id.uploadPdfButton);

        if (isAdminUser()) {
            uploadPdfButton.setVisibility(View.VISIBLE);
        } else {
            uploadPdfButton.setVisibility(View.GONE);
        }

        uploadPdfButton.setOnClickListener(v -> openFilePicker());
    }

    private boolean isAdminUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_admin", false);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_REQUEST);
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in to upload files.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pdfUri != null) {
            String title = titleEditText.getText().toString().trim();
            String author = authorEditText.getText().toString().trim();

            if (title.isEmpty() || author.isEmpty()) {
                Toast.makeText(this, "Please enter both title and author.", Toast.LENGTH_SHORT).show();
                return;
            }

            String sanitizedTitle = title.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String fileName = sanitizedTitle + ".pdf";

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference pdfRef = storageRef.child("pdfs/" + fileName);

            pdfRef.putFile(pdfUri)
                    .addOnSuccessListener(taskSnapshot -> pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String pdfUrl = uri.toString();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> pdfData = new HashMap<>();
                        pdfData.put("title", title);
                        pdfData.put("author", author);
                        pdfData.put("pdfUrl", pdfUrl);
                        pdfData.put("fileName", fileName);
                        pdfData.put("isRemoved", false);

                        db.collection("ebooks")
                                .add(pdfData)
                                .addOnSuccessListener(documentReference -> {
                                    String documentId = documentReference.getId();
                                    db.collection("ebooks").document(documentId).update("documentId", documentId)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(UploadPdfActivity.this, "PDF uploaded successfully.", Toast.LENGTH_SHORT).show();
                                                clearFields();
                                                finish(); // Go back to MainActivity
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(UploadPdfActivity.this, "Failed to update document ID.", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(UploadPdfActivity.this, "Failed to upload PDF.", Toast.LENGTH_SHORT).show();
                                });
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to get download URL.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No PDF selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        titleEditText.setText("");
        authorEditText.setText("");
        pdfUri = null;
    }
}
