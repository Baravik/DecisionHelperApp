package com.decisionhelperapp.repository;

import android.content.Context;
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
        quizDAO = new QuizDAO(dbHelper);
        ratingDAO = new RatingDAO(dbHelper);
    }

    // Example methods to interact with QuestionDAO
    public List<Question> getAllQuestions() {
        return questionDAO.getQuestions();
    }

    public void addQuestion(Question question) {
        questionDAO.insert(question);
    }

    // Example methods to interact with QuizDAO
    public Quiz getQuizById(int id) {
        return quizDAO.getQuiz(id);
    }

    public void updateQuiz(Quiz quiz) {
        quizDAO.update(quiz);
    }

    // Example methods to interact with RatingDAO
    public List<Rating> getAllRatings() {
        return ratingDAO.getRatings();
    }

    public void addRating(Rating rating) {
        ratingDAO.insert(rating);
    }

    // Additional repository methods can be added here to manage complex data operations
}
