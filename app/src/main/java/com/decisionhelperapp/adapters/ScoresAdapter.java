package com.decisionhelperapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.decisionhelperapp.models.Scores;
import com.OpenU.decisionhelperapp.R;

import java.util.List;

public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ScoresViewHolder> {
    private List<Scores> scoresList;

    public ScoresAdapter(List<Scores> scoresList) {
        this.scoresList = scoresList;
    }

    @NonNull
    @Override
    public ScoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ScoresViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoresViewHolder holder, int position) {
        Scores score = scoresList.get(position);
        holder.bindScore(score);

    }

    @Override
    public int getItemCount() {
        return scoresList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Scores> newScores) {
        this.scoresList = newScores;
        notifyDataSetChanged();
    }

    public static class ScoresViewHolder extends RecyclerView.ViewHolder {
        TextView scoreDescriptionTextView;
        TextView scoreRangeTextView;

        public ScoresViewHolder(@NonNull View itemView) {
            super(itemView);
            scoreDescriptionTextView = itemView.findViewById(R.id.scoreDescriptionTextView);
            scoreRangeTextView = itemView.findViewById(R.id.scoreRangeTextView);
        }

        @SuppressLint("SetTextI18n")
        public void bindScore(Scores score) {
            scoreRangeTextView.setText("Quiz name: " + score.getQuizName() + "\n\nScore: " + score.getScore());

        }
    }
}