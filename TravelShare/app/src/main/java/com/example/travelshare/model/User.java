package com.example.travelshare.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private String id;
    private String pseudo;
    private String description;
    private String profilePictureUrl;

    private int nbPublications;
    private int nbGroups;
    private int nbLikes;
    
    private List<String> favoriteLieuIds;
    private List<String> followedUserIds;
    private List<String> followedGroupIds;
    private List<String> followedTags;

    public User() {}

    public User(String id, String pseudo, String description, String profilePictureUrl, int nbPublications, int nbGroups, int nbLikes) {
        this.id = id;
        this.pseudo = pseudo;
        this.description = description;
        this.profilePictureUrl = profilePictureUrl;
        this.nbPublications = nbPublications;
        this.nbGroups = nbGroups;
        this.nbLikes = nbLikes;
        this.favoriteLieuIds = new ArrayList<>();
        this.followedUserIds = new ArrayList<>();
        this.followedGroupIds = new ArrayList<>();
        this.followedTags = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getPseudo() { return pseudo; }
    public String getDescription() { return description; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public int getNbPublications() { return nbPublications; }
    public int getNbGroups() { return nbGroups; }
    public int getNbLikes() { return nbLikes; }
    public List<String> getFavoriteLieuIds() { return favoriteLieuIds != null ? favoriteLieuIds : new ArrayList<>(); }
    public List<String> getFollowedUserIds() { return followedUserIds != null ? followedUserIds : new ArrayList<>(); }
    public List<String> getFollowedGroupIds() { return followedGroupIds != null ? followedGroupIds : new ArrayList<>(); }
    public List<String> getFollowedTags() { return followedTags != null ? followedTags : new ArrayList<>(); }

    public void setId(String id) { this.id = id; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public void setDescription(String description) { this.description = description; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public void setNbPublications(int nbPublications) { this.nbPublications = nbPublications; }
    public void setNbGroups(int nbGroups) { this.nbGroups = nbGroups; }
    public void setNbLikes(int nbLikes) { this.nbLikes = nbLikes; }
    public void setFavoriteLieuIds(List<String> favoriteLieuIds) { this.favoriteLieuIds = favoriteLieuIds; }
    public void setFollowedUserIds(List<String> followedUserIds) { this.followedUserIds = followedUserIds; }
    public void setFollowedGroupIds(List<String> followedGroupIds) { this.followedGroupIds = followedGroupIds; }
    public void setFollowedTags(List<String> followedTags) { this.followedTags = followedTags; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
