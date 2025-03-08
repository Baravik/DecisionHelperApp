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
    private OnQuestionnaireClickListener listener;

    public interface OnQuestionnaireClickListener {
        void onQuestionnaireClick(Quiz quiz);
    }

    public QuestionnaireAdapter(List<Quiz> questionnaires, OnQuestionnaireClickListener listener) {
        this.questionnaires = questionnaires;
        this.listener = listener;
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
        Quiz quiz = questionnaires.get(position);
        holder.bind(quiz, listener);
    }

    @Override
    public int getItemCount() {
        return questionnaires != null ? questionnaires.size() : 0;
    }

    static class QuestionnaireViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;

        public QuestionnaireViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.questionnaire_title);
            descriptionTextView = itemView.findViewById(R.id.questionnaire_description);
        }

        public void bind(final Quiz quiz, final OnQuestionnaireClickListener listener) {
            titleTextView.setText(quiz.getCustomTitle());
            descriptionTextView.setText(quiz.getDescription());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onQuestionnaireClick(quiz);
                }
            });
        }
    }
}