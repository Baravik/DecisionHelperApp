package com.decisionhelperapp.models;

public class Quiz {
    // Added no-argument constructor
    public Quiz() {}

    private String description;
    private String id;
    private String topic;
    private String customTitle;
    private String userId;
    private int score;
    private String completedAt;

    public Quiz(String id, String topic, String description, String customTitle, String userId, int score, String completedAt) {
        this.id = id;
        this.topic = topic;
        this.description = description;
        this.customTitle = customTitle;
        this.userId = userId;
        this.score = score;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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
}