package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.adapters.QuestionnaireAdapter;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.models.Quiz;

import java.util.List;

public class QuestionnaireListActivity extends AppCompatActivity implements QuestionnaireAdapter.OnQuestionnaireClickListener {

    private RecyclerView recyclerView;
    private QuestionnaireAdapter adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire_list);

        // Get user ID from intent
        userId = getIntent().getStringExtra("USER_ID");

        // Set up RecyclerView
        recyclerView = findViewById(R.id.questionnaire_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load questionnaires from database
        loadQuestionnaires();
    }

    private void loadQuestionnaires() {
        QuizDAO quizDAO = new QuizDAO();
        quizDAO.getAllQuizzes(new QuizDAO.QuizCallback() {
            @Override
            public void onCallback(List<Quiz> quizzes) {
                if (quizzes != null && !quizzes.isEmpty()) {
                    adapter = new QuestionnaireAdapter(quizzes, QuestionnaireListActivity.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(QuestionnaireListActivity.this, 
                        "Failed to load questionnaires: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        View emptyStateView = findViewById(R.id.empty_state_view);
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onQuestionnaireClick(Quiz quiz) {
        Intent intent = new Intent(this, TakeQuizActivity.class);
        intent.putExtra("QUIZ_ID", quiz.getId());
        intent.putExtra("QUIZ_TITLE", quiz.getCustomTitle());
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }
}