package com.example.e_book1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEbookActivity extends AppCompatActivity {
    private EditText titleEditText;
    private EditText authorEditText;
    private Button addButton;
    private Spinner pdfSpinner;
    private List<String> pdfUrls;
    private List<String> pdfNames;
    private List<String> documentIds;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ebook);

        titleEditText = findViewById(R.id.titleEditText);
        authorEditText = findViewById(R.id.authorEditText);
        addButton = findViewById(R.id.addButton);
        pdfSpinner = findViewById(R.id.pdfSpinner);

        firestore = FirebaseFirestore.getInstance();
        pdfUrls = new ArrayList<>();
        pdfNames = new ArrayList<>();
        documentIds = new ArrayList<>();

        fetchPdfsFromFirestore();

        pdfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Optionally handle item selection if needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        addButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String author = authorEditText.getText().toString().trim();
            int selectedPosition = pdfSpinner.getSelectedItemPosition();
            if (selectedPosition == -1) {
                Toast.makeText(AddEbookActivity.this, "Please select a PDF.", Toast.LENGTH_SHORT).show();
                return;
            }
            String selectedPdfUrl = pdfUrls.get(selectedPosition);
            String documentId = documentIds.get(selectedPosition);

            if (title.isEmpty() || author.isEmpty()) {
                Toast.makeText(AddEbookActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            } else {
                updateEbookInFirestore(documentId, title, author);
            }
        });
    }

    private void fetchPdfsFromFirestore() {
        firestore.collection("ebooks")
                .whereEqualTo("isRemoved", true) // Fetch only eBooks where isRemoved is true
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pdfUrls.clear();
                    pdfNames.clear();
                    documentIds.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String title = document.getString("title");
                        String url = document.getString("pdfUrl");
                        String documentId = document.getId();
                        if (title != null && url != null && documentId != null) {
                            pdfNames.add(title);
                            pdfUrls.add(url);
                            documentIds.add(documentId);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEbookActivity.this, android.R.layout.simple_spinner_item, pdfNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    pdfSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(AddEbookActivity.this, "Failed to fetch PDFs.", Toast.LENGTH_SHORT).show());
    }

    private void updateEbookInFirestore(String documentId, String title, String author) {
        firestore.collection("ebooks").document(documentId)
                .update("title", title, "author", author, "isRemoved", false)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddEbookActivity.this, "eBook updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> Toast.makeText(AddEbookActivity.this, "Failed to update eBook: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
