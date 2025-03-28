package com.example.e_book1;

public class CommentRating {
    private String title;
    private String author;
    private String comment;
    private float rating;

    public CommentRating() {
        // Default constructor required for calls to DataSnapshot.getValue(CommentRating.class)
    }

    public CommentRating(String title, String author, String comment, float rating) {
        this.title = title;
        this.author = author;
        this.comment = comment;
        this.rating = rating;
    }

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
