package com.decisionhelperapp.models;

import java.util.List;

public class QuizQuestions {
    private List<String> questionsId;
    
    // We'll use the document ID in Firestore as quizId
    // No need to store it as a field

    // Empty constructor for Firebase
    public QuizQuestions() {
    }

    public QuizQuestions(List<String> questionsId) {
        this.questionsId = questionsId;
    }

    // Added constructor for single question
    public QuizQuestions(String questionId) {
        this.questionsId = List.of(questionId);
    }

    public List<String> getQuestionsId() {
        return questionsId;
    }

    public void setQuestionsId(List<String> questionsId) {
        this.questionsId = questionsId;
    }
    
    // The document ID itself is the quizId, so no need for separate getters/setters
}