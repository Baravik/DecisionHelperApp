package com.decisionhelperapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.database.ScoresDAO;
import com.decisionhelperapp.models.Scores;
import java.util.List;
import java.util.Objects;

public class RatingActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        Objects.requireNonNull(getSupportActionBar()).hide();


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

        // Fetch scores from Firebase
        ScoresDAO scoresDAO = new ScoresDAO();
        scoresDAO.getAllScores(new ScoresDAO.ScoresCallback() {
            @Override
            public void onCallback(List<Scores> scoresList) {
                // Update UI with scores
                // ...
            }

            @Override
            public void onFailure(Exception e) {
                // Handle error
            }
        });
    }
}