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

    private boolean hasCheckedUser = false;  // ensures we redirect only after loading completes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        userTextView = findViewById(R.id.userStatusTextView);
        userProfileImageView = findViewById(R.id.userProfileImageView);

        userProfileImageView.setOnClickListener(v -> navigateToUserProfile());
        userTextView.setOnClickListener(v -> navigateToUserProfile());

        // Observers
        mainViewModel.getCurrentUserObject().observe(this, user -> {
            Log.d(TAG, "Observed user: " + (user != null ? user.getEmail() : "null"));
            if (user != null) {
                updateUIWithUser(user);
            } else if (hasCheckedUser) {
                Log.w(TAG, "User is null after loading completed. Redirecting to login.");
                navigateToLogin();
            }
        });

        mainViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Log.e(TAG, "Error: " + errorMsg);
                userTextView.setText(MessageFormat.format("Error: {0}", errorMsg));
            }
        });

        mainViewModel.getLoading().observe(this, isLoading -> {
            Log.d(TAG, "Loading: " + isLoading);
            if (Boolean.FALSE.equals(isLoading)) {
                hasCheckedUser = true;
                User user = mainViewModel.getCurrentUserObject().getValue();
                if (user == null) {
                    Log.w(TAG, "No user found after loading finished. Redirecting to login.");
                    navigateToLogin();
                }
            }
        });

        // Get userId
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            userId = sharedPreferences.getString(KEY_USER_ID, null);
        }

        if (userId != null) {
            mainViewModel.loadUserData(userId);
        } else {
            Log.w(TAG, "No user ID found. Redirecting to login.");
            navigateToLogin();
        }

        // Buttons
        Button startQuizButton = findViewById(R.id.btnStartQuiz);
        startQuizButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            User currentUser = mainViewModel.getCurrentUserObject().getValue();
            if (currentUser != null) {
                intent.putExtra("USER_ID", currentUser.getId());
            }
            startActivity(intent);
        });

        Button viewScoresButton = findViewById(R.id.btnViewScores);
        viewScoresButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ScoresActivity.class));
        });

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
        Log.d(TAG, "Redirecting to LoginActivity...");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        String welcomeText = "Welcome, " + (user.getName().equals("Guest") ? "Guest! Create an account to save your progress." : user.getName() + "!");
        userTextView.setText(welcomeText);

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfilePictureUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(userProfileImageView);
        } else {
            userProfileImageView.setImageResource(R.drawable.default_profile);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        if (userId != null) {
            mainViewModel.loadUserData(userId);
        }
    }
}