package com.example.travelshare.model;

import java.util.Objects;

public class User {
    private String id;
    private String pseudo;
    private String description;
    private String profilePictureUrl;

    private int nbPublications;
    private int nbGroups;
    private int nbLikes;

    public User() {}

    public User(String id, String pseudo, String description, String profilePictureUrl, int nbPublications, int nbGroups, int nbLikes) {
        this.id = id;
        this.pseudo = pseudo;
        this.description = description;
        this.profilePictureUrl = profilePictureUrl;
        this.nbPublications = nbPublications;
        this.nbGroups = nbGroups;
        this.nbLikes = nbLikes;
    }

    public String getId() { return id; }
    public String getPseudo() { return pseudo; }
    public String getDescription() { return description; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public int getNbPublications() { return nbPublications; }
    public int getNbGroups() { return nbGroups; }
    public int getNbLikes() { return nbLikes; }

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