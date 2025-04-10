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
    private String completedAt;
    
    @PropertyName("isPublic")
    private boolean isPublic;

    public Quiz(String id, String description, String customTitle, String userId, int score, String completedAt, boolean isPublic) {
        this.id = id;
        this.description = description;
        this.customTitle = customTitle;
        this.userId = userId;
        this.score = score;
        this.completedAt = completedAt;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
    
    @PropertyName("isPublic")
    public boolean isPublic() {
        return isPublic;
    }
    
    @PropertyName("isPublic")
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}