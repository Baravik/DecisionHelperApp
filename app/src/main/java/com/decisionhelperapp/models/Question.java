package com.decisionhelperapp.models;

public class Question {
    private String description;
    private String id;
    private int score;
    private String type;
    private String title;

    public Question(String description, String id, int score, String type, String title) {
        this.description = description;
        this.id = id;
        this.score = score;
        this.type = type;
        this.title = title;
    }

    public Question() {
        // Empty constructor added
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}