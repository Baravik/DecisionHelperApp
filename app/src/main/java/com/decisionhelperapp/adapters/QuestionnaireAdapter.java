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

public class QuestionnaireAdapter extends RecyclerView.Adapter<QuestionnaireAdapter.QuestionnaireViewHolder> {
    private List<Quiz> questionnaires;
    private final OnQuestionnaireClickListener listener;

    public interface OnQuestionnaireClickListener {
        void onQuestionnaireClick(Quiz quiz);
    }

    public QuestionnaireAdapter(List<Quiz> questionnaires, OnQuestionnaireClickListener listener) {
        this.questionnaires = questionnaires;
        this.listener = listener;
    }

    public void updateQuestionnaires(List<Quiz> newQuestionnaires) {
        if (newQuestionnaires != null) {
            this.questionnaires = newQuestionnaires;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public QuestionnaireViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_questionnaire, parent, false);
        return new QuestionnaireViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionnaireViewHolder holder, int position) {
        if (questionnaires != null && position >= 0 && position < questionnaires.size()) {
            Quiz quiz = questionnaires.get(position);
            holder.bind(quiz, listener);
        }
    }

    @Override
    public int getItemCount() {
        return questionnaires != null ? questionnaires.size() : 0;
    }

    static class QuestionnaireViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;

        public QuestionnaireViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.questionnaire_title);
            descriptionTextView = itemView.findViewById(R.id.questionnaire_description);
        }

        public void bind(final Quiz quiz, final OnQuestionnaireClickListener listener) {
            if (quiz != null) {
                String title = quiz.getCustomTitle();
                String description = quiz.getDescription();
                
                if (titleTextView != null) {
                    titleTextView.setText(title != null ? title : "Untitled Questionnaire");
                }
                
                if (descriptionTextView != null) {
                    descriptionTextView.setText(description != null ? description : "No description available");
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onQuestionnaireClick(quiz);
                    }
                });
            }
        }
    }
}