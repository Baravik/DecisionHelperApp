package com.decisionhelperapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.decisionhelperapp.R;

public class RatingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        TextView recommendationTextView = findViewById(R.id.recommendationTextView);

        int score = getIntent().getIntExtra("score", 0);
        scoreTextView.setText("Score: " + score);

        String recommendation;
        if (score <= 5) {
            recommendation = "Not recommended";
        } else if (score <= 10) {
            recommendation = "Consider it";
        } else {
            recommendation = "Seems like a good idea";
        }
        recommendationTextView.setText(recommendation);
    }
}