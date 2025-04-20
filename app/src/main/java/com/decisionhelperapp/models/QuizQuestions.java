package com.decisionhelperapp.models;

import java.util.List;

public class QuizQuestions {
    private List<String> questionsId;

    // Empty constructor for Firebase
    public QuizQuestions() {}

    public QuizQuestions(List<String> questionsId) {
        this.questionsId = questionsId;
    }

    public List<String> getQuestionsId() {
        return questionsId;
    }

}