package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModelProvider;

import com.OpenU.decisionhelperapp.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.viewmodel.MainViewModel;

import java.text.MessageFormat;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final String PREF_NAME = "DecisionHelperPrefs";
    private static final String KEY_USER_ID = "userId";
    
    private MainViewModel mainViewModel;
    private TextView userTextView;
    private ImageView userProfileImageView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize MainViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        // Setup UI components
        userTextView = findViewById(R.id.userStatusTextView);
        userProfileImageView = findViewById(R.id.userProfileImageView);
        
        // Set click listeners for profile image and username
        userProfileImageView.setOnClickListener(v -> navigateToUserProfile());
        userTextView.setOnClickListener(v -> navigateToUserProfile());
       
        // Setup LiveData observers
        mainViewModel.getCurrentUserObject().observe(this, user -> {
            if (user != null) {
                updateUIWithUser(user);
            } else {
                // Handle null user case
                navigateToLogin();
            }
        });
        
        mainViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Log.e(TAG, "Error: " + errorMsg);
                userTextView.setText(MessageFormat.format("Error: {0}", errorMsg));
            }
        });
        
        // Get the user ID from the intent or shared preferences
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            // If not in intent, try to get from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            userId = sharedPreferences.getString(KEY_USER_ID, null);
        }
        
        if (userId != null) {
            mainViewModel.loadUserData(userId);
        } else {
            // No user ID found, redirect to login
            navigateToLogin();
            return;
        }
        
        // Button to start a quiz
        Button startQuizButton = findViewById(R.id.btnStartQuiz);
        startQuizButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            User currentUser = mainViewModel.getCurrentUserObject().getValue();
            if (currentUser != null) {
                intent.putExtra("USER_ID", currentUser.getId());
            }
            startActivity(intent);
        });
        
        // Button to view scores
        Button viewScoresButton = findViewById(R.id.btnViewScores);
        viewScoresButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScoresActivity.class);
            startActivity(intent);
        });
        
        // Button to add/create questions and quizzes
        Button addQuestionsButton = findViewById(R.id.btnAddQuestions);
        addQuestionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
            User currentUser = mainViewModel.getCurrentUserObject().getValue();
            if (currentUser != null) {
                intent.putExtra("USER_ID", currentUser.getId());
            }
            startActivity(intent);
        });
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void navigateToUserProfile() {
        Intent intent = new Intent(MainActivity.this, UserActivity.class);
        User currentUser = mainViewModel.getCurrentUserObject().getValue();
        if (currentUser != null) {
            intent.putExtra("USER_ID", currentUser.getId());
        }
        startActivity(intent);
    }
    
    private void updateUIWithUser(User user) {
        String welcomeText;
        
        if ("Guest".equals(user.getName())) {
            welcomeText = "Welcome, Guest! Create an account to save your progress.";
        } else {
            welcomeText = "Welcome, " + user.getName() + "!";
        }
        
        userTextView.setText(welcomeText);
        
        // Load profile picture if available
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(this)
                .load(user.getProfilePictureUrl())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(userProfileImageView);
        } else {
            // Set default profile picture
            userProfileImageView.setImageResource(R.drawable.default_profile);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to this activity
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        if (userId != null) {
            mainViewModel.loadUserData(userId);
        }
    }
}