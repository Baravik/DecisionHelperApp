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
        questionDAO = new QuestionDAO();
        // Instead of directly instantiating the abstract QuizDAO, we provide an anonymous implementation
        quizDAO = new QuizDAO() {
            public Quiz getQuiz(int id) {
                return dbHelper.getQuiz(id); // call DatabaseHelper's implementation
            }

            public void update(Quiz quiz) {
                dbHelper.updateQuiz(quiz); // call DatabaseHelper's implementation
            }
        };
        ratingDAO = new RatingDAO(dbHelper);
    }

    // Example methods to interact with QuestionDAO
    public void getAllQuestions(final QuestionDAO.QuestionCallback callback) {
        questionDAO.getAllQuestions(callback);
    }

    public void addQuestion(Question question) {
        questionDAO.insertQuestion(question);
    }

    // Example methods to interact with QuizDAO
    public void /*Quiz*/ getQuizById(int id) {
        //return quizDAO.getQuiz(id);
    }

    public void updateQuiz(Quiz quiz) {
        //quizDAO.update(quiz);
    }

    // Example methods to interact with RatingDAO
    public void/*List<Rating>*/ getAllRatings() {
        //return ratingDAO.getAllRatings();
    }

    public void addRating(Rating rating) {
        //ratingDAO.insertRating(rating);
    }

    // Additional repository methods can be added here to manage complex data operations
}
