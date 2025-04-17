package com.decisionhelperapp.models;

public class Scores {
    private String quizName;
    private String id;
    private Integer score;

    // Empty constructor required for Firestore
    public Scores() {
    }

    public Scores(String description, String id, Integer score) {
        this.quizName = description;
        this.id = id;
        this.score = score;
    }

    public String getQuizName() {
        return quizName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }
}