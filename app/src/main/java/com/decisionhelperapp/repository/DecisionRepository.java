package com.decisionhelperapp.repository;

import android.content.Context;

import java.util.List;

import com.decisionhelperapp.database.DatabaseHelper;
import com.decisionhelperapp.database.QuestionDAO;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.database.QuizQuestionsDAO;
import com.decisionhelperapp.database.QuizUserDAO;
import com.decisionhelperapp.database.ScoresDAO;
import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.models.QuizQuestions;
import com.decisionhelperapp.models.QuizUser;
import com.decisionhelperapp.models.Scores;
import com.decisionhelperapp.models.User;

/**
 * Repository class that abstracts data operations from multiple DAOs.
 * This class follows the Repository Pattern in MVVM architecture.
 */
public class DecisionRepository {

    private DatabaseHelper dbHelper;
    private QuestionDAO questionDAO;
    private QuizDAO quizDAO;
    private UserDAO userDAO;
    private ScoresDAO scoresDAO;
    private QuizQuestionsDAO quizQuestionsDAO;
    private QuizUserDAO quizUserDAO;

    // Constructor with all required DAOs
    public DecisionRepository(Context context) {
        // Initialize the DatabaseHelper and DAO instances
        dbHelper = new DatabaseHelper(context);
        questionDAO = new QuestionDAO();
        quizDAO = new QuizDAO();
        userDAO = new UserDAO();
        scoresDAO = new ScoresDAO();
        quizQuestionsDAO = new QuizQuestionsDAO();
        quizUserDAO = new QuizUserDAO();
    }

    // User related methods
    public void getUserById(String userId, UserDAO.SingleUserCallback callback) {
        userDAO.getUserById(userId, callback);
    }

    public void getUserByEmail(String email, UserDAO.SingleUserCallback callback) {
        userDAO.getUserByEmail(email, callback);
    }

    public void getAllUsers(UserDAO.UserCallback callback) {
        userDAO.getAllUsers(callback);
    }

    public void addUser(User user, UserDAO.ActionCallback callback) {
        userDAO.addUser(user, callback);
    }

    public void updateUser(User user, UserDAO.ActionCallback callback) {
        userDAO.updateUser(user, callback);
    }

    public void deleteUser(String userId, UserDAO.ActionCallback callback) {
        userDAO.deleteUser(userId, callback);
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

    public void deleteQuiz(String quizId, QuizDAO.ActionCallback callback) {
        quizDAO.deleteQuiz(quizId, callback);
    }

    // Question related methods
    public void getAllQuestions(QuestionDAO.QuestionCallback callback) {
        questionDAO.getAllQuestions(callback);
    }

    public void getQuestionById(String questionId, QuestionDAO.SingleQuestionCallback callback) {
        questionDAO.getQuestionById(questionId, callback);
    }

    public void addQuestion(Question question, QuestionDAO.ActionCallback callback) {
        questionDAO.addQuestion(question, callback);
    }

    public void updateQuestion(Question question, QuestionDAO.ActionCallback callback) {
        questionDAO.updateQuestion(question, callback);
    }

    public void deleteQuestion(String questionId, QuestionDAO.ActionCallback callback) {
        questionDAO.deleteQuestion(questionId, callback);
    }

    /**
     * Updates multiple questions in a batch operation
     * @param questions List of questions to update
     * @param callback Callback to handle success or failure
     */
    public void updateQuestions(List<Question> questions, QuestionDAO.ActionCallback callback) {
        questionDAO.updateQuestions(questions, callback);
    }

    // Scores related methods
    public void getAllScores(ScoresDAO.ScoresCallback callback) {
        scoresDAO.getAllScores(callback);
    }

    public void getScoreById(String scoreId, ScoresDAO.SingleScoreCallback callback) {
        scoresDAO.getScoreById(scoreId, callback);
    }

    public void addScore(Scores score, ScoresDAO.ActionCallback callback) {
        scoresDAO.addScore(score, callback);
    }

    public void updateScore(Scores score, ScoresDAO.ActionCallback callback) {
        scoresDAO.updateScore(score, callback);
    }

    public void deleteScore(String scoreId, ScoresDAO.ActionCallback callback) {
        scoresDAO.deleteScore(scoreId, callback);
    }

    // QuizQuestions related methods
    public void getQuestionsForQuiz(String quizId, QuizQuestionsDAO.QuestionsCallback callback) {
        quizQuestionsDAO.getQuestionsForQuiz(quizId, callback);
    }

    public void addQuizQuestion(QuizQuestions quizQuestion, String quizID,QuizQuestionsDAO.ActionCallback callback) {
        quizQuestionsDAO.addQuizQuestion(quizQuestion, quizID, callback);
    }

    // QuizUser related methods
    public void getAllQuizUsers(QuizUserDAO.QuizUserCallback callback) {
        quizUserDAO.getAllQuizUsers(callback);
    }
    
    public void getQuizUsersByQuizId(String quizId, QuizUserDAO.QuizUserCallback callback) {
        quizUserDAO.getQuizUsersByQuizId(quizId, callback);
    }
    
    public void getQuizUserById(String quizUserId, QuizUserDAO.SingleQuizUserCallback callback) {
        quizUserDAO.getQuizUserById(quizUserId, callback);
    }
    
    public void getQuizUsersByUserId(String userId, QuizUserDAO.QuizUserCallback callback) {
        quizUserDAO.getQuizUsersByUserId(userId, callback);
    }

    public void getQuizzesForUser(String userId, QuizUserDAO.QuizCallback callback) {
        quizUserDAO.getQuizzesForUser(userId, callback);
    }

    public void addQuizUser(QuizUser quizUser, QuizUserDAO.ActionCallback callback) {
        quizUserDAO.addQuizUser(quizUser, callback);
    }
    
    public void updateQuizUser(QuizUser quizUser, QuizUserDAO.ActionCallback callback) {
        quizUserDAO.updateQuizUser(quizUser, callback);
    }
    
    public void deleteQuizUser(String quizUserId, QuizUserDAO.ActionCallback callback) {
        quizUserDAO.deleteQuizUser(quizUserId, callback);
    }
}
