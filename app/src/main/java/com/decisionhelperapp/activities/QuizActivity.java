package com.decisionhelperapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.adapters.QuizAdapter;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.models.Quiz;
import java.util.List;
import java.util.Objects;

public class QuizActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Objects.requireNonNull(getSupportActionBar()).hide();


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        QuizDAO quizDAO = new QuizDAO();
        quizDAO.getAllQuizzes(new QuizDAO.QuizCallback() {
            @Override
            public void onCallback(List<Quiz> quizList) {
                quizAdapter = new QuizAdapter(quizList);
                recyclerView.setAdapter(quizAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(QuizActivity.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}