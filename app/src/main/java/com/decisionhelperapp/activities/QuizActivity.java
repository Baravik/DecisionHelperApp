package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.adapters.QuizAdapter;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.viewmodel.QuizViewModel;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private QuizViewModel quizViewModel;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        userId = getIntent().getStringExtra("USER_ID");

        // Set up recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ViewModel
        quizViewModel = new ViewModelProvider(this).get(QuizViewModel.class);
        
        // Set up observers
        setupObservers();
        
        // Load quizzes
        quizViewModel.loadAllQuizzes();
    }
    
    private void setupObservers() {
        // Observe quiz list
        quizViewModel.getQuizList().observe(this, quizzes -> {
            if (quizzes != null && !quizzes.isEmpty()) {
                // Filter quizzes: public or created by current user
                List<Quiz> filteredQuizzes = new ArrayList<>();
                for (Quiz quiz : quizzes) {
                    if (quiz.getIsPublic() || quiz.getUserId().equals(userId)) {
                        filteredQuizzes.add(quiz);
                    }
                }

                if (!filteredQuizzes.isEmpty()) {
                    quizAdapter = new QuizAdapter(filteredQuizzes, quiz -> {
                        String quizId = quiz.getId();
                        if (quizId != null && !quizId.isEmpty()) {
                            quizViewModel.loadQuizById(quizId);
                        } else {
                            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
                        }
                    });
                    recyclerView.setAdapter(quizAdapter);
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe loading state
        quizViewModel.getIsLoading().observe(this, isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        
        // Observe errors
        quizViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe quiz status
        quizViewModel.getQuizStatus().observe(this, status -> {
            // Could update a status text view here if needed
        });
        
        // Observe current quiz
        quizViewModel.getCurrentQuiz().observe(this, quiz -> {
            if (quiz != null) {
                // Show Toast with quiz title
                Toast.makeText(this, "Selected quiz: " + quiz.getCustomTitle(), Toast.LENGTH_SHORT).show();
                // Create Intent to open TakeQuizActivity
                Intent intent = new Intent(QuizActivity.this, TakeQuizActivity.class);

                // Optionally, pass any quiz-related information (e.g., quiz ID, title) to TakeQuizActivity
                intent.putExtra("QUIZ_TITLE", quiz.getCustomTitle()); // You can send other details if needed
                intent.putExtra("QUIZ_ID", quiz.getId()); // You can send other details if needed

                // Start the TakeQuizActivity
                startActivity(intent);
            } else {
               // Handle case where quiz is null (optional)
                Toast.makeText(this, "No quiz selected", Toast.LENGTH_SHORT).show();
            }
        });

    }
}