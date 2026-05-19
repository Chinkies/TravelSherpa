package com.example.travelshare.model;

import java.util.Date;

public class Notification {
    private String id;
    private String receiverId;
    private String senderId;
    private String senderName;
    private String senderProfilePictureUrl;
    private String type; // "LIKE", "COMMENT", "GROUP_INVITE", etc.
    private String postId;
    private String message;
    private Date timestamp;
    private boolean read;

    public Notification() {}

    public Notification(String receiverId, String senderId, String senderName, String senderProfilePictureUrl, String type, String postId, String message) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfilePictureUrl = senderProfilePictureUrl;
        this.type = type;
        this.postId = postId;
        this.message = message;
        this.timestamp = new Date();
        this.read = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderProfilePictureUrl() { return senderProfilePictureUrl; }
    public void setSenderProfilePictureUrl(String senderProfilePictureUrl) { this.senderProfilePictureUrl = senderProfilePictureUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
