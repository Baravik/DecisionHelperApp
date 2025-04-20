package com.decisionhelperapp.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.models.Quiz;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
    private final List<Quiz> quizList;
    private final OnQuizClickListener listener;
    private OnQuizDeleteListener deleteListener;
    private final FirebaseUser currentUser;

    // Interface for handling quiz clicks
    public interface OnQuizClickListener {
        void onQuizClick(Quiz quiz);
    }

    // Interface for handling quiz deletion
    public interface OnQuizDeleteListener {
        void onQuizDelete(Quiz quiz);
    }

    public QuizAdapter(List<Quiz> quizList, OnQuizClickListener listener) {
        this.quizList = quizList;
        this.listener = listener;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void setOnQuizDeleteListener(OnQuizDeleteListener listener) {
        this.deleteListener = listener;
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

        // Show delete button only for non-public quizzes owned by the current user
        if (currentUser != null && !quiz.getIsPublic() &&
                quiz.getUserId() != null && quiz.getUserId().equals(currentUser.getUid())) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                // Show confirmation dialog
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Quiz")
                        .setMessage("Are you sure you want to delete this questionnaire?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            if (deleteListener != null) {
                                deleteListener.onQuizDelete(quiz);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Set click listener for the entire item
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
        ImageButton btnDelete;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuizItemTitle);
            tvDescription = itemView.findViewById(R.id.tvQuizItemDescription);
            btnDelete = itemView.findViewById(R.id.btnDeleteQuiz);
        }
    }
}