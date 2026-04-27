package com.example.travelshare.model;

import java.util.Date;

public class Comment {
    private String id;
    private String postId;
    private String authorId;
    private String authorName;
    private String text;
    private Date date;
    private String authorPictureUrl;

    public Comment() {}

    public Comment(String postId, String authorId, String authorName, String text,
                   String authorPictureUrl) {
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.date = new Date();
        this.authorPictureUrl = authorPictureUrl;
    }

    public String getId() { return id; }
    public String getPostId() { return postId; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getText() { return text; }
    public Date getDate() { return date; }
    public String getAuthorPictureUrl() { return authorPictureUrl; }

    public void setId(String id) { this.id = id; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setText(String text) { this.text = text; }
    public void setDate(Date date) { this.date = date; }
    public void setAuthorPictureUrl(String authorPictureUrl) { this.authorPictureUrl = authorPictureUrl; }
}