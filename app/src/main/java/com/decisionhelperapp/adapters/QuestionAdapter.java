package com.decisionhelperapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.models.Question;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private Context context;
    private List<Question> questions;
    private QuestionAdapterListener listener;

    public interface QuestionAdapterListener {
        void onQuestionDeleted(int position);
        void onImageRequested(int position);
        void onImageRemoved(int position);
    }

    public QuestionAdapter(Context context, List<Question> questions, QuestionAdapterListener listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questions.get(position);
        
        // Set question number
        holder.questionNumber.setText("Question " + (position + 1));
        
        // Set existing question text if available
        if (question.getTitle() != null && !question.getTitle().isEmpty()) {
            holder.questionText.setText(question.getTitle());
        }
        
        // Set up question type
        if ("open_question".equals(question.getType())) {
            holder.radioOpenQuestion.setChecked(true);
            holder.containerMultipleChoice.setVisibility(View.GONE);
        } else {
            holder.radioMultipleChoice.setChecked(true);
            holder.containerMultipleChoice.setVisibility(View.VISIBLE);
        }
        
        // Set up answer options if this is a multiple choice question
        if (holder.answerOptionsAdapter == null) {
            holder.answerOptionsAdapter = new AnswerOptionAdapter(extractAnswerOptions(question.getDescription()));
            holder.recyclerAnswerOptions.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerAnswerOptions.setAdapter(holder.answerOptionsAdapter);
            holder.answerOptionsAdapter.setAnswerChangeListener(options -> {
                // Update the description field with the new options
                StringBuilder descBuilder = new StringBuilder();
                
                // Preserve image information if it exists
                if (question.getDescription().contains("has_image:true")) {
                    String imageUrlLine = "";
                    for (String line : question.getDescription().split("\n")) {
                        if (line.startsWith("image_url:")) {
                            imageUrlLine = line;
                            break;
                        }
                    }
                    
                    if (!imageUrlLine.isEmpty()) {
                        descBuilder.append("has_image:true\n");
                        descBuilder.append(imageUrlLine).append("\n");
                    }
                }
                
                // Add all options
                for (String option : options) {
                    descBuilder.append("option:").append(option).append("\n");
                }
                
                question.setDescription(descBuilder.toString().trim());
            });
        } else {
            holder.answerOptionsAdapter.updateOptions(extractAnswerOptions(question.getDescription()));
        }
        
        // Setup the question text change listener
        holder.questionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    questions.get(adapterPosition).setTitle(editable.toString());
                }
            }
        });
        
        // Set up radio group change listener
        holder.radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                if (checkedId == R.id.radio_multiple_choice) {
                    questions.get(adapterPosition).setType("multiple_choice");
                    holder.containerMultipleChoice.setVisibility(View.VISIBLE);
                } else {
                    questions.get(adapterPosition).setType("open_question");
                    holder.containerMultipleChoice.setVisibility(View.GONE);
                }
            }
        });
        
        // Set up Add Answer button
        holder.btnAddAnswer.setOnClickListener(v -> {
            holder.answerOptionsAdapter.addNewOption();
        });
        
        // Set up Delete Question button
        holder.btnDeleteQuestion.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onQuestionDeleted(adapterPosition);
            }
        });
        
        // Set up Add Image button
        holder.btnAddImage.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onImageRequested(adapterPosition);
            }
        });
        
        // Check if the question has an image and show it if so
        String description = question.getDescription();
        if (description != null && description.contains("has_image:true")) {
            holder.imageContainer.setVisibility(View.VISIBLE);
            
            String imageUrl = null;
            for (String line : description.split("\n")) {
                if (line.startsWith("image_url:")) {
                    imageUrl = line.substring("image_url:".length());
                    break;
                }
            }
            
            if (imageUrl != null) {
                Glide.with(context)
                        .load(Uri.parse(imageUrl))
                        .centerCrop()
                        .placeholder(R.drawable.default_profile)
                        .into(holder.questionImage);
            }
            
            // Set up remove image button
            holder.btnRemoveImage.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    holder.imageContainer.setVisibility(View.GONE);
                    listener.onImageRemoved(adapterPosition);
                }
            });
        } else {
            holder.imageContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    private List<String> extractAnswerOptions(String description) {
        List<String> options = new ArrayList<>();
        if (description != null && !description.isEmpty()) {
            for (String line : description.split("\n")) {
                if (line.startsWith("option:")) {
                    options.add(line.substring("option:".length()));
                }
            }
        }
        return options;
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionNumber;
        TextInputEditText questionText;
        RadioGroup radioGroupType;
        RadioButton radioMultipleChoice;
        RadioButton radioOpenQuestion;
        LinearLayout containerMultipleChoice;
        RecyclerView recyclerAnswerOptions;
        Button btnAddAnswer;
        Button btnAddImage;
        ImageButton btnDeleteQuestion;
        FrameLayout imageContainer;
        ImageView questionImage;
        ImageButton btnRemoveImage;
        
        AnswerOptionAdapter answerOptionsAdapter;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumber = itemView.findViewById(R.id.text_question_number);
            questionText = itemView.findViewById(R.id.edit_question_text);
            radioGroupType = itemView.findViewById(R.id.radio_group_type);
            radioMultipleChoice = itemView.findViewById(R.id.radio_multiple_choice);
            radioOpenQuestion = itemView.findViewById(R.id.radio_open_question);
            containerMultipleChoice = itemView.findViewById(R.id.container_multiple_choice);
            recyclerAnswerOptions = itemView.findViewById(R.id.recycler_answer_options);
            btnAddAnswer = itemView.findViewById(R.id.btn_add_answer);
            btnAddImage = itemView.findViewById(R.id.btn_add_image);
            btnDeleteQuestion = itemView.findViewById(R.id.btn_delete_question);
            imageContainer = itemView.findViewById(R.id.image_container);
            questionImage = itemView.findViewById(R.id.question_image);
            btnRemoveImage = itemView.findViewById(R.id.btn_remove_image);
        }
    }

    // Nested adapter for answer options
    private static class AnswerOptionAdapter extends RecyclerView.Adapter<AnswerOptionAdapter.OptionViewHolder> {
        private List<String> options;
        private AnswerChangeListener listener;

        interface AnswerChangeListener {
            void onOptionsChanged(List<String> options);
        }

        AnswerOptionAdapter(List<String> options) {
            this.options = new ArrayList<>(options);
            if (this.options.isEmpty()) {
                // Add two default empty options
                this.options.add("");
                this.options.add("");
            }
        }

        void setAnswerChangeListener(AnswerChangeListener listener) {
            this.listener = listener;
            notifyOptionsChanged();
        }

        void addNewOption() {
            options.add("");
            notifyItemInserted(options.size() - 1);
            notifyOptionsChanged();
        }

        void updateOptions(List<String> newOptions) {
            options.clear();
            options.addAll(newOptions);
            if (options.isEmpty()) {
                options.add("");
                options.add("");
            }
            notifyDataSetChanged();
        }

        private void notifyOptionsChanged() {
            if (listener != null) {
                // Filter out empty options
                List<String> nonEmptyOptions = new ArrayList<>();
                for (String option : options) {
                    if (!option.trim().isEmpty()) {
                        nonEmptyOptions.add(option);
                    }
                }
                listener.onOptionsChanged(nonEmptyOptions);
            }
        }

        @NonNull
        @Override
        public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_answer_option, parent, false);
            return new OptionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
            holder.optionText.setText(options.get(position));
            
            holder.optionText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        options.set(adapterPosition, s.toString());
                        notifyOptionsChanged();
                    }
                }
            });
            
            holder.btnDeleteOption.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    if (options.size() > 2) { // Keep at least 2 options
                        options.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        notifyItemRangeChanged(adapterPosition, options.size() - adapterPosition);
                        notifyOptionsChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        static class OptionViewHolder extends RecyclerView.ViewHolder {
            EditText optionText;
            ImageButton btnDeleteOption;

            OptionViewHolder(@NonNull View itemView) {
                super(itemView);
                optionText = itemView.findViewById(R.id.edit_answer_text);
                btnDeleteOption = itemView.findViewById(R.id.btn_delete_answer);
            }
        }
    }
}