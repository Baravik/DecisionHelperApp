package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.adapters.QuestionnaireAdapter;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.activities.TakeQuizActivity;

import java.util.ArrayList;
import java.util.List;

public class QuestionnaireListActivity extends AppCompatActivity implements QuestionnaireAdapter.OnQuestionnaireClickListener {
    private static final String TAG = "QuestionnaireList";
    private RecyclerView recyclerView;
    private QuestionnaireAdapter adapter;
    private String userId;
    private ProgressBar progressBar;
    private View emptyStateView;
    private TextView errorMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire_list);

        // Get user ID from intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Log.e(TAG, "No USER_ID provided in intent");
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        recyclerView = findViewById(R.id.questionnaire_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateView = findViewById(R.id.empty_state_view);
        errorMessageView = findViewById(R.id.error_message);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionnaireAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Load questionnaires
        loadQuestionnaires();
    }

    private void loadQuestionnaires() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        QuizDAO quizDAO = new QuizDAO();
        quizDAO.getAllQuizzes(new QuizDAO.QuizCallback() {
            @Override
            public void onCallback(List<Quiz> quizzes) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (quizzes != null && !quizzes.isEmpty()) {
                    showContent(quizzes);
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load questionnaires", e);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                showError();
            }
        });
    }

    private void showContent(List<Quiz> quizzes) {
        if (recyclerView != null && emptyStateView != null && errorMessageView != null) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.GONE);
            adapter.updateQuestionnaires(quizzes);
        }
    }

    private void showEmptyState() {
        if (recyclerView != null && emptyStateView != null && errorMessageView != null) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
            errorMessageView.setVisibility(View.GONE);
            TextView emptyStateText = emptyStateView.findViewById(R.id.empty_state_text);
            if (emptyStateText != null) {
                // Changed resource reference to local package R
                emptyStateText.setText(R.string.no_questionnaires_available);
            }
        }
    }

    private void showError() {
        if (recyclerView != null && emptyStateView != null && errorMessageView != null) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.VISIBLE);
            // Changed resource reference to local package R
            errorMessageView.setText(R.string.failed_to_load_questionnaires_please_try_again);
            Toast.makeText(this, "Failed to load questionnaires. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQuestionnaireClick(Quiz quiz) {
        if (quiz == null) {
            Log.e(TAG, "Null quiz selected");
            Toast.makeText(this, "Error: Invalid questionnaire", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String quizId = quiz.getId();
        if (quizId == null || quizId.isEmpty()) {
            Log.e(TAG, "Quiz with null or empty ID selected");
            Toast.makeText(this, "Error: Invalid questionnaire ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Starting TakeQuizActivity with quiz: " + quizId);
        
        try {
            Intent intent = new Intent(this, TakeQuizActivity.class);
            
            // Add required extras
            intent.putExtra("QUIZ_ID", quizId);
            intent.putExtra("USER_ID", userId);
            
            // Add quiz title with fallback for null
            String quizTitle = quiz.getCustomTitle();
            intent.putExtra("QUIZ_TITLE", (quizTitle != null && !quizTitle.isEmpty()) ? 
                    quizTitle : "Untitled Quiz");
            
            // Don't rely on quiz.getQuestions() as it might be null or not loaded yet
            // Instead, we'll load questions in TakeQuizActivity
            
            // Start the activity
            startActivity(intent);
            Log.d(TAG, "TakeQuizActivity started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting TakeQuizActivity", e);
            Toast.makeText(this, "Error loading quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}