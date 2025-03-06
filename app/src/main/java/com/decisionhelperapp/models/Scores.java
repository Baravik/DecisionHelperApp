package com.decisionhelperapp.models;

public class Scores {
    private String description;
    private String id;
    private String scoreRange;

    // Empty constructor required for Firestore
    public Scores() {
    }

    public Scores(String description, String id, String scoreRange) {
        this.description = description;
        this.id = id;
        this.scoreRange = scoreRange;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScoreRange() {
        return scoreRange;
    }

    public void setScoreRange(String scoreRange) {
        this.scoreRange = scoreRange;
    }
}