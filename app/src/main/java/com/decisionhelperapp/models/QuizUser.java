package com.decisionhelperapp.models;

public class QuizUser {
    private String userId;
    private final String quizId;
    private int score;
    private String id; // Document ID for Firebase

    // Empty constructor for Firestore
    public QuizUser(String quizId) {
        this.quizId = quizId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuizId() {
        return quizId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}