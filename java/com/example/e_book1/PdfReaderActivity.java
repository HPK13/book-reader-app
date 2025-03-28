package com.example.e_book1;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PdfReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);

        String pdfUrl = getIntent().getStringExtra("pdfUrl");

        WebView pdfWebView = findViewById(R.id.pdfWebView);
        pdfWebView.getSettings().setJavaScriptEnabled(true);
        pdfWebView.setWebViewClient(new WebViewClient());

        // Use Google Docs viewer to open the PDF within the WebView
        if (pdfUrl != null) {
            pdfWebView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + Uri.encode(pdfUrl));
        } else {
            Toast.makeText(this, "Error: PDF URL is missing11.", Toast.LENGTH_SHORT).show();
        }
    }
}
