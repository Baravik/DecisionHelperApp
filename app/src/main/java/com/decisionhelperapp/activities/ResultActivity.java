package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.database.RatingDAO;
import com.decisionhelperapp.models.Rating;

public class ResultActivity extends AppCompatActivity {

    private TextView feedbackTextView;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get data from intent
        int score = getIntent().getIntExtra("SCORE", 0);
        String quizTitle = getIntent().getStringExtra("QUIZ_TITLE");
        userId = getIntent().getStringExtra("USER_ID");
        Rating rating = getIntent().getParcelableExtra("RATING", Rating.class);

        // Initialize UI components
        TextView scoreTextView = findViewById(R.id.result_score);
        TextView titleTextView = findViewById(R.id.result_title);
        feedbackTextView = findViewById(R.id.result_feedback);
        Button homeButton = findViewById(R.id.home_button);
        Button viewDetailsButton = findViewById(R.id.details_button);

        // Set the UI values
        scoreTextView.setText(String.valueOf(score));
        titleTextView.setText(quizTitle);
        
        // Set feedback based on score
        setFeedbackText(score);

        // Save rating to database
        if (rating != null) {
            saveRatingToDatabase(rating);
        }

        // Set up button listeners
        homeButton.setOnClickListener(v -> {
            // Return to main activity
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        viewDetailsButton.setOnClickListener(v -> {
            // Navigate to score details
            Intent intent = new Intent(ResultActivity.this, ScoresActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    private void setFeedbackText(int score) {
        String feedback;
        
        if (score >= 90) {
            feedback = getString(R.string.feedback_excellent);
        } else if (score >= 75) {
            feedback = getString(R.string.feedback_good);
        } else if (score >= 50) {
            feedback = getString(R.string.feedback_average);
        } else if (score >= 25) {
            feedback = getString(R.string.feedback_low);
        } else {
            feedback = getString(R.string.feedback_poor);
        }
        
        feedbackTextView.setText(feedback);
    }
    
    private void saveRatingToDatabase(Rating rating) {
        RatingDAO ratingDAO = new RatingDAO();
        ratingDAO.saveRating(rating, new RatingDAO.RatingCallback() {
            @Override
            public void onSuccess() {
                // Rating saved successfully
            }
            
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ResultActivity.this, 
                        "Failed to save rating: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}