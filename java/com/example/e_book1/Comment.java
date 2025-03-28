package com.example.e_book1;

public class Comment {
    private String username;
    private String commentText;

    // Default constructor required for Firebase
    public Comment() {}

    public Comment(String username, String commentText) {
        this.username = username;
        this.commentText = commentText;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}
