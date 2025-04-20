package com.decisionhelperapp.models;

import com.google.firebase.firestore.PropertyName;

public class Quiz {
    // Added no-argument constructor
    public Quiz() {}

    private String description;
    private String id;
    private String customTitle;
    private String userId;
    private int score;

    @PropertyName("isPublic")
    private boolean isPublic;

    public Quiz(String id, String description, String customTitle, String userId, int score, boolean isPublic) {
        this.id = id;
        this.description = description;
        this.customTitle = customTitle;
        this.userId = userId;
        this.score = score;
        this.isPublic = isPublic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public String getUserId() {
        return userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @PropertyName("isPublic")
    public boolean getIsPublic() {
        return isPublic;
    }

}