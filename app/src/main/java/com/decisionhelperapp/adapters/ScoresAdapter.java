package com.decisionhelperapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.decisionhelperapp.models.Scores;
import com.example.decisionhelperapp.R;

import java.util.List;

public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ScoresViewHolder> {
    private List<Scores> scoresList;
    private final OnScoreClickListener onScoreClickListener;

    // Interface for handling click events
    public interface OnScoreClickListener {
        void onScoreClick(Scores score);
    }

    public ScoresAdapter(List<Scores> scoresList, OnScoreClickListener listener) {
        this.scoresList = scoresList;
        this.onScoreClickListener = listener;
    }

    @NonNull
    @Override
    public ScoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new ScoresViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoresViewHolder holder, int position) {
        Scores score = scoresList.get(position);
        holder.bindScore(score);
        
        // Set up click listener
        holder.itemView.setOnClickListener(v -> {
            if (onScoreClickListener != null) {
                onScoreClickListener.onScoreClick(score);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scoresList.size();
    }

    public void updateData(List<Scores> newScores) {
        this.scoresList = newScores;
        notifyDataSetChanged();
    }

    public static class ScoresViewHolder extends RecyclerView.ViewHolder {
        TextView scoreDescriptionTextView;
        TextView scoreRangeTextView;

        public ScoresViewHolder(@NonNull View itemView) {
            super(itemView);
            scoreDescriptionTextView = itemView.findViewById(R.id.questionTextView);
            scoreRangeTextView = itemView.findViewById(R.id.questionDetailsTextView);
            scoreRangeTextView.setVisibility(View.VISIBLE);
        }

        public void bindScore(Scores score) {
            scoreDescriptionTextView.setText(score.getDescription());
            scoreRangeTextView.setText("Score Range: " + score.getScoreRange());
        }
    }
}