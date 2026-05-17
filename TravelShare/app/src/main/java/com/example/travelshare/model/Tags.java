package com.example.travelshare.model;

import java.util.ArrayList;
import java.util.List;

public class Tags {
    private List<String> tags;

    public Tags() {
        this.tags = new ArrayList<>();
    }

    public Tags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
