package com.decisionhelperapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.adapters.QuizAdapter;
import com.decisionhelperapp.adapters.UserAdapter;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.viewmodel.AdminViewModel;

public class AdminActivity extends BaseActivity {
    
    private AdminViewModel adminViewModel;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private RecyclerView usersRecyclerView;
    private RecyclerView quizzesRecyclerView;
    private Button refreshButton;
    private UserAdapter userAdapter;
    private QuizAdapter quizAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize views
        statusTextView = findViewById(R.id.adminStatusTextView);
        progressBar = findViewById(R.id.adminProgressBar);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        quizzesRecyclerView = findViewById(R.id.quizzesRecyclerView);
        refreshButton = findViewById(R.id.refreshButton);
        
        // Set up RecyclerViews
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        quizzesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize AdminViewModel
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        
        // Set up observers for LiveData
        setupObservers();
        
        // Set up button click listeners
        refreshButton.setOnClickListener(v -> adminViewModel.loadDashboardData());
        
        // Load initial data
        adminViewModel.loadDashboardData();
    }
    
    private void setupObservers() {
        // Observe status changes
        adminViewModel.getStatus().observe(this, status -> {
            if (statusTextView != null) {
                statusTextView.setText(status);
            }
        });
        
        // Observe loading state
        adminViewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
        
        // Observe error messages
        adminViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe users list
        adminViewModel.getUserList().observe(this, users -> {
            if (users != null) {
                userAdapter = new UserAdapter(users, user -> {
                    // Handle user click - could show details or options to edit/delete
                    showUserOptions(user);
                });
                usersRecyclerView.setAdapter(userAdapter);
            }
        });
        
        // Observe quizzes list
        adminViewModel.getQuizList().observe(this, quizzes -> {
            if (quizzes != null) {
                quizAdapter = new QuizAdapter(quizzes, quiz -> {
                    // Handle quiz click - could show details or options to edit/delete
                    showQuizOptions(quiz);
                });
                quizzesRecyclerView.setAdapter(quizAdapter);
            }
        });
    }
    
    private void showUserOptions(User user) {
        // Could show a dialog with options to view details, edit, or delete the user
        Toast.makeText(this, "Selected user: " + user.getName(), Toast.LENGTH_SHORT).show();
    }
    
    private void showQuizOptions(Quiz quiz) {
        // Could show a dialog with options to view details, edit, or delete the quiz
        Toast.makeText(this, "Selected quiz: " + quiz.getTitle(), Toast.LENGTH_SHORT).show();
    }
}