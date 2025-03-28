package com.example.e_book1;

public class Ebook {
    private String title;
    private String author;
    private String pdfUrl;
    private String fileName;
    private String documentId;

    // Default constructor required for Firebase
    public Ebook() {
        // Default constructor required for calls to DataSnapshot.getValue(Ebook.class)
    }

    // Constructor with title, author, pdfUrl, fileName, and documentId
    public Ebook(String title, String author, String pdfUrl, String fileName, String documentId) {
        this.title = title;
        this.author = author;
        this.pdfUrl = pdfUrl;
        this.fileName = fileName;
        this.documentId = documentId;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
