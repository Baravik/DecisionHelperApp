package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.decisionhelperapp.R;
import com.decisionhelperapp.adapters.QuizAdapter;
import com.decisionhelperapp.database.DatabaseHelper;
import com.decisionhelperapp.models.Question;

import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;
    private DatabaseHelper databaseHelper;
    private List<Question> questionList;
    private int totalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        recyclerView = findViewById(R.id.recyclerView); // Updated to correct id as defined in your layout resource
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        // Updated to include a string parameter as required
        questionList = Collections.singletonList((Question) databaseHelper.getQuestionsForQuiz("quiz"));

        quizAdapter = new QuizAdapter(questionList);
        recyclerView.setAdapter(quizAdapter);

        // Calculate total score based on the selected answers
        totalScore = 0;
        for (Question question : questionList) {
            // Assuming a method getSelectedOptionScore() exists that returns the score for the chosen answer.
            totalScore += question.getSelectedOptionScore();
        }

        // Navigate to RatingActivity with the calculated score
        Intent intent = new Intent(QuizActivity.this, RatingActivity.class);
        intent.putExtra("EXTRA_SCORE", totalScore);
        startActivity(intent);
    }
}