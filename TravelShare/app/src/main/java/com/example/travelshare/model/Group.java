package com.example.travelshare.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {
    private String groupId;
    private String groupName;
    private String description;
    private String imageUrl;
    private Date lastActivityDate;
    private Map<String, Boolean> members;
    private List<String> publications;

    public Group() {}

    public Group(String name, String description, String userId) {
        this.groupName = name;
        this.description = description;
        this.imageUrl = "";
        this.members = new HashMap<>();
        this.members.put(userId, true);
        this.publications = new ArrayList<>();
        this.lastActivityDate = new Date();
    }

    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public Date getLastActivityDate() { return lastActivityDate; }
    public Map<String, Boolean> getMembers() { return members; }
    public List<String> getPublications() { return publications; }

    public void setGroupName(String name) { this.groupName = name; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setLastActivityDate(Date lastActivityDate) { this.lastActivityDate = lastActivityDate; }
}