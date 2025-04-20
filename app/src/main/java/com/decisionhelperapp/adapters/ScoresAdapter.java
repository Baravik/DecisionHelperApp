package com.decisionhelperapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.decisionhelperapp.models.Scores;
import com.OpenU.decisionhelperapp.R;

import java.util.List;

public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ScoresViewHolder> {
    private final List<Scores> scoresList;
    private OnScoreDeleteListener deleteListener;

    // Interface for handling score deletion
    public interface OnScoreDeleteListener {
        void onScoreDelete(Scores score);
    }

    public ScoresAdapter(List<Scores> scoresList) {
        this.scoresList = scoresList;
    }

    public void setOnScoreDeleteListener(OnScoreDeleteListener listener) {
        this.deleteListener = listener;
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

        // Set delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Score")
                    .setMessage("Are you sure you want to delete this score?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onScoreDelete(score);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return scoresList.size();
    }

    public static class ScoresViewHolder extends RecyclerView.ViewHolder {
        TextView scoreDescriptionTextView;
        TextView scoreRangeTextView;
        ImageButton btnDelete;

        public ScoresViewHolder(@NonNull View itemView) {
            super(itemView);
            scoreDescriptionTextView = itemView.findViewById(R.id.scoreDescriptionTextView);
            scoreRangeTextView = itemView.findViewById(R.id.scoreRangeTextView);
            btnDelete = itemView.findViewById(R.id.btnDeleteScore);
        }

        @SuppressLint("SetTextI18n")
        public void bindScore(Scores score) {
            scoreRangeTextView.setText("Quiz name: " + score.getQuizName() + "\n\nScore: " + score.getScore());
        }
    }
}