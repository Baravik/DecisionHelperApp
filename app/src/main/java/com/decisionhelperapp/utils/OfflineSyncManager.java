package com.decisionhelperapp.utils;

import android.content.Context;

import com.decisionhelperapp.database.DatabaseHelper;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.models.Quiz;

import java.util.List;

// This class handles offline storage of quizzes and syncs them once internet is available
public class OfflineSyncManager {

    private static DatabaseHelper dbHelper;

    // Save quiz data locally when offline
    public static void saveQuizOffline(Context context, Quiz quiz) {
        dbHelper = new DatabaseHelper(context);
        QuizDAO quizDAO = new QuizDAO(dbHelper);
        // Assume the Quiz model has a 'synced' boolean field
        quiz.setSynced(false);
        quizDAO.insertQuiz(quiz);
    }

    // Attempt to sync unsynced quizzes to the remote database
    public static void syncQuizzes(Context context) {
        // Only sync if internet is available
        if (!ApiService.isInternetAvailable(context)) {
            return;
        }
        QuizDAO quizDAO = new QuizDAO(dbHelper);
        List<Quiz> unsyncedQuizzes = quizDAO.getUnsyncedQuizzes();
        if (unsyncedQuizzes == null || unsyncedQuizzes.isEmpty()) {
            return;
        }
        for (Quiz quiz : unsyncedQuizzes) {
            // Replace the URL and logic below with actual remote upload logic (e.g., HTTP POST request)
            String response = ApiService.fetchDataFromUrl(context, "http://example.com/uploadQuiz?data=" + quiz.toString());
            if (!response.startsWith("Error") && !response.equals("Connection timed out")) {
                // Mark quiz as synced upon successful upload
                quiz.setSynced(true);
                quizDAO.update(quiz);
            }
        }
    }
}
