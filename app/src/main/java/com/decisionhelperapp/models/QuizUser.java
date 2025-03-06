package com.decisionhelperapp.models;

import java.util.Date;

public class QuizUser {
    private String customTitle;
    private String userId;
    private String quizId;
    private int score;
    private String completedAt;
    private String id; // Document ID for Firebase

    // Empty constructor for Firestore
    public QuizUser() {
    }

    public QuizUser(String customTitle, String userId, String quizId, int score, String completedAt) {
        this.customTitle = customTitle;
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.completedAt = completedAt;
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

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}