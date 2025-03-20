package com.decisionhelperapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.models.Quiz;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
    private final List<Quiz> quizList;
    private final OnQuizClickListener listener;

    // Interface for handling quiz clicks
    public interface OnQuizClickListener {
        void onQuizClick(Quiz quiz);
    }

    public QuizAdapter(List<Quiz> quizList, OnQuizClickListener listener) {
        this.quizList = quizList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.tvTitle.setText(quiz.getCustomTitle());
        holder.tvDescription.setText(quiz.getDescription());
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuizClick(quiz);
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuizItemTitle);
            tvDescription = itemView.findViewById(R.id.tvQuizItemDescription);
        }
    }
}