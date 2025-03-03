package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // Initialize MainViewModel
        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Example: Update a TextView with the current user
        TextView userTextView = findViewById(R.id.userStatusTextView);
        mainViewModel.getCurrentUser().observe(this, user -> {
            userTextView.setText(user);
        });

        // Button to start a quiz
        Button startQuizButton = findViewById(R.id.btnStartQuiz);
        startQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                startActivity(intent);
            }
        });
        
        // Button to view scores
        Button viewScoresButton = findViewById(R.id.btnViewScores);
        viewScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScoresActivity.class);
                startActivity(intent);
            }
        });
        
        // Button to view users
        Button viewUsersButton = findViewById(R.id.btnViewUsers);
        viewUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(intent);
            }
        });
    }
}