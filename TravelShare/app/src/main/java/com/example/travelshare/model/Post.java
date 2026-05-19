package com.example.travelshare.model;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    private String id;
    private String authorId;
    private String authorName;
    private String description;
    private GeoPoint location;
    private String indication;
    private Date date;
    private String imageUrl;
    private String imageDeleteUrl;
    private String authorProfilPictureUrl;
    private List<String> likers;
    private int likesCount;

    private int commentCount;
    private boolean isPublic;
    private List<String> groupIds;
    private List<String> tags;
    private String lieuId;
    public Post() {}

    public Post(String authorId, String authorName, String description, GeoPoint location, String indication,
                String imageUrl, String authorProfilPictureUrl, boolean isPublic, List<String> groupIds, List<String> tags) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.description = description;
        this.location = location;
        this.indication = indication;
        this.date = new Date();
        this.imageUrl = imageUrl;
        this.authorProfilPictureUrl = authorProfilPictureUrl;
        this.likers = new ArrayList<>();
        this.likesCount = 0;
        this.commentCount = 0;
        this.isPublic = isPublic;
        this.groupIds = groupIds != null ? groupIds : new ArrayList<>();
        this.tags = tags;
    }

    public String getId() { return id; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getDescription() { return description; }
    public GeoPoint getLocation() { return location; }
    public String getIndication() { return indication; }
    public Date getDate() { return date; }
    public String getImageUrl() { return imageUrl; }
    public String getImageDeleteUrl() { return imageDeleteUrl; }
    public String getAuthorProfilPictureUrl() { return authorProfilPictureUrl; }
    public List<String> getLikers() { return likers; }
    public int getLikesCount() { return likesCount; }
    public int getCommentCount() { return commentCount; }
    public boolean isPublic() { return isPublic; }
    public List<String> getGroupIds() { return groupIds; }
    public List<String> getTags() { return tags; }
    public String getLieuId() { return lieuId; }

    public void setId(String id) { this.id = id; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(GeoPoint location) { this.location = location; }
    public void setIndication(String indication) { this.indication = indication; }
    public void setDate(Date date) { this.date = date; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setImageDeleteUrl(String imageDeleteUrl) { this.imageDeleteUrl = imageDeleteUrl; }
    public void setAuthorProfilPictureUrl(String authorProfilPictureUrl) { this.authorProfilPictureUrl = authorProfilPictureUrl; }
    public void setLikers(List<String> likers) { this.likers = likers; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public void setGroupIds(List<String> groupIds) { this.groupIds = groupIds; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setLieuId(String lieuId) { this.lieuId = lieuId; }
}