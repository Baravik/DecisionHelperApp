package com.decisionhelperapp.repository;

import android.content.Context;

import com.decisionhelperapp.database.DatabaseHelper;
import com.decisionhelperapp.database.QuestionDAO;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.database.QuizQuestionsDAO;
import com.decisionhelperapp.database.ScoresDAO;
import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.models.QuizQuestions;
import com.decisionhelperapp.models.User;

/**
 * Repository class that abstracts data operations from multiple DAOs.
 * This class follows the Repository Pattern in MVVM architecture.
 */
public class DecisionRepository {

    private final QuestionDAO questionDAO;
    private final QuizDAO quizDAO;
    private final UserDAO userDAO;
    private final ScoresDAO scoresDAO;
    private final QuizQuestionsDAO quizQuestionsDAO;

    // Constructor with all required DAOs
    public DecisionRepository(Context context) {
        // Initialize the DatabaseHelper and DAO instances
        new DatabaseHelper(context);
        questionDAO = new QuestionDAO();
        quizDAO = new QuizDAO();
        userDAO = new UserDAO();
        scoresDAO = new ScoresDAO();
        quizQuestionsDAO = new QuizQuestionsDAO();
    }

    // User related methods
    public void getUserById(String userId, UserDAO.SingleUserCallback callback) {
        userDAO.getUserById(userId, callback);
    }

    public void addUser(User user, UserDAO.ActionCallback callback) {
        userDAO.addUser(user, callback);
    }

    public void updateUser(User user, UserDAO.ActionCallback callback) {
        userDAO.updateUser(user, callback);
    }

    // Quiz related methods
    public void getAllQuizzes(QuizDAO.QuizCallback callback) {
        quizDAO.getAllQuizzes(callback);
    }
    public void getQuizById(String quizId, QuizDAO.SingleQuizCallback callback) {
        quizDAO.getQuizById(quizId, callback);
    }
    public void addQuiz(Quiz quiz, QuizDAO.ActionCallback callback) {
        quizDAO.addQuiz(quiz, callback);
    }
    public void updateQuiz(Quiz quiz, QuizDAO.ActionCallback callback) {
        quizDAO.updateQuiz(quiz, callback);
    }
    public void addQuestion(Question question, QuestionDAO.ActionCallback callback) {
        questionDAO.addQuestion(question, callback);
    }
    // Scores related methods
    public void getAllScores(ScoresDAO.ScoresCallback callback) {
        scoresDAO.getAllScores(callback);
    }
    // QuizQuestions related methods
    public void getQuestionsForQuiz(String quizId, QuizQuestionsDAO.QuestionsCallback callback) {
        quizQuestionsDAO.getQuestionsForQuiz(quizId, callback);
    }
    public void addQuizQuestion(QuizQuestions quizQuestion, String quizID,QuizQuestionsDAO.ActionCallback callback) {
        quizQuestionsDAO.addQuizQuestion(quizQuestion, quizID, callback);
    }

    public void deleteQuestion(String id, QuizDAO.ActionCallback actionCallback) {
        questionDAO.deleteQuestion(id, actionCallback);
    }

    public void deleteQuizQuestions(String quizId, QuizQuestionsDAO.ActionCallback quizDeletedSuccessfully) {
        quizQuestionsDAO.deleteQuizQuestions(quizId, quizDeletedSuccessfully);
    }

    public void deleteQuiz(String quizId, QuizDAO.ActionCallback quizDeletedSuccessfully) {
        quizDAO.deleteQuiz(quizId, quizDeletedSuccessfully);
    }

    public void deleteScore(String scoreId, ScoresDAO.ActionCallback scoreDeletedSuccessfully) {
        scoresDAO.deleteScore(scoreId, scoreDeletedSuccessfully);
    }

}
