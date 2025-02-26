package com.decisionhelperapp.models;

public class Question {
    private int questionId;
    private int quizId;
    private String questionText;
    private String questionType;
    private String possibleAnswers;
    private int scoreIfYes;

    public Question(int questionId, int quizId, String questionText, String questionType, String possibleAnswers, int scoreIfYes) {
        this.questionId = questionId;
        this.quizId = quizId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.possibleAnswers = possibleAnswers;
        this.scoreIfYes = scoreIfYes;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getPossibleAnswers() {
        return possibleAnswers;
    }

    public void setPossibleAnswers(String possibleAnswers) {
        this.possibleAnswers = possibleAnswers;
    }

    public int getScoreIfYes() {
        return scoreIfYes;
    }

    public void setScoreIfYes(int scoreIfYes) {
        this.scoreIfYes = scoreIfYes;
    }
}