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
                emptyStateText.setText(com.OpenU.decisionhelperapp.R.string.no_questionnaires_available);
            }
        }
    }

    private void showError() {
        if (recyclerView != null && emptyStateView != null && errorMessageView != null) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.VISIBLE);
            errorMessageView.setText(com.OpenU.decisionhelperapp.R.string.failed_to_load_questionnaires_please_try_again);
            Toast.makeText(this, "Failed to load questionnaires. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQuestionnaireClick(Quiz quiz) {
        if (quiz != null && quiz.getId() != null) {
            try {
                Intent intent = new Intent(this, TakeQuizActivity.class);
                intent.putExtra("QUIZ_ID", quiz.getId());
                intent.putExtra("QUIZ_TITLE", quiz.getCustomTitle());
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting TakeQuizActivity", e);
                Toast.makeText(this, "Error opening questionnaire", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid questionnaire selected", Toast.LENGTH_SHORT).show();
        }
    }
}