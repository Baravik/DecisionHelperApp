package com.decisionhelperapp.repository;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import com.decisionhelperapp.database.DatabaseHelper;
import com.decisionhelperapp.database.QuestionDAO;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.database.RatingDAO;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.models.Rating;

// Repository that abstracts data operations from multiple DAOs.
public class DecisionRepository {

    private DatabaseHelper dbHelper;
    private QuestionDAO questionDAO;
    private QuizDAO quizDAO;
    private RatingDAO ratingDAO;

    public DecisionRepository(Context context) {
        // Initialize the DatabaseHelper and DAO instances
        dbHelper = new DatabaseHelper(context);
        questionDAO = new QuestionDAO(dbHelper);
        // Instead of directly instantiating the abstract QuizDAO, we provide an anonymous implementation
        quizDAO = new QuizDAO(dbHelper);
        ratingDAO = new RatingDAO(dbHelper);
    }

    // Example methods to interact with QuestionDAO
    public List<Question> getAllQuestions() {
        return questionDAO.getAllQuestions();
    }

    public void addQuestion(Question question) {
        questionDAO.insertQuestion(question);
    }

    // Example methods to interact with QuizDAO
    public Quiz getQuizById(int id) {
        return quizDAO.getQuiz(id);
    }

    public void updateQuiz(Quiz quiz) {
        quizDAO.update(quiz);
    }

    // Example methods to interact with RatingDAO
    public List<RatingDAO.Rating> getAllRatings() {
        return ratingDAO.getAllRatings();
    }

    public void addRating(RatingDAO.Rating rating) {
        ratingDAO.insertRating(rating);
    }

    // Additional repository methods can be added here to manage complex data operations
}
