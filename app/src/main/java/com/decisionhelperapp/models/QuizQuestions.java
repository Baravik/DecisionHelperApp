package com.decisionhelperapp.models;

import java.util.List;

public class QuizQuestions {
    private List<String> questionsId;
    private String quizId;
    private int order;
    private String id; // Document ID for Firebase

    // Empty constructor for Firebase
    public QuizQuestions() {
    }

    public QuizQuestions(List<String> questionsId, String quizId, int order) {
        this.questionsId = questionsId;
        this.quizId = quizId;
        this.order = order;
    }

    public List<String> getQuestionsId() {
        return questionsId;
    }

    public void setQuestionsId(List<String> questionsId) {
        this.questionsId = questionsId;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}