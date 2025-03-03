package com.decisionhelperapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.decisionhelperapp.adapters.ScoresAdapter;
import com.decisionhelperapp.models.Scores;
import com.decisionhelperapp.viewmodel.ScoresViewModel;
import com.OpenU.decisionhelperapp.R;

import java.util.Objects;

public class ScoresActivity extends AppCompatActivity implements ScoresAdapter.OnScoreClickListener {
    
    private ScoresViewModel scoresViewModel;
    private RecyclerView recyclerView;
    private ScoresAdapter scoresAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        Objects.requireNonNull(getSupportActionBar()).hide();


        // Initialize views
        recyclerView = findViewById(R.id.scoresRecyclerView);
        progressBar = findViewById(R.id.scoresProgressBar);
        emptyView = findViewById(R.id.scoresEmptyView);
        
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize ViewModel
        scoresViewModel = new ViewModelProvider(this).get(ScoresViewModel.class);
        
        // Observe the scores list
        scoresViewModel.getScoresList().observe(this, scores -> {
            if (scores != null && !scores.isEmpty()) {
                scoresAdapter = new ScoresAdapter(scores, this);
                recyclerView.setAdapter(scoresAdapter);
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe error messages
        scoresViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe loading state
        scoresViewModel.getIsLoading().observe(this, isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        
        // Load scores data
        scoresViewModel.loadScores();
    }

    @Override
    public void onScoreClick(Scores score) {
        // Handle score click - e.g., show details, edit, etc.
        scoresViewModel.setSelectedScore(score);
        Toast.makeText(this, "Selected score: " + score.getDescription(), Toast.LENGTH_SHORT).show();
    }
}