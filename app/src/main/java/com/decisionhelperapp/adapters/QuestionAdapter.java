package com.decisionhelperapp.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.models.Question;
import com.google.android.material.textfield.TextInputEditText;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private final Context context;
    private List<Question> questions;
    private final QuestionAdapterListener listener;

    public interface QuestionAdapterListener {
        void onQuestionDeleted(int position);

    }

    public QuestionAdapter(Context context, List<Question> questions, QuestionAdapterListener listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
    }
    
    /**
     * Updates the questions list and refreshes the adapter
     * @param newQuestions The new list of questions to display
     */
    public void updateQuestions(List<Question> newQuestions) {
        this.questions = newQuestions;
        notifyDataSetChanged();
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
        holder.questionNumber.setText(MessageFormat.format("Question {0}", position + 1));
        
        // FIXED: Always set the question text from the model, even if empty
        // This ensures recycled views don't show previous question titles
        holder.questionText.setText(question.getTitle());
        
        // Set up question type
        if ("yes_no_question".equals(question.getType())) {
            holder.radioYesNoQuestion.setChecked(true);
            holder.containerMultipleChoice.setVisibility(View.GONE);
            holder.containerYesNoQuestion.setVisibility(View.VISIBLE);
            
            // Set the correct radio button based on which answer is worth 100%
            if (question.getDescription() != null && question.getDescription().contains("yes_full_score:true")) {
                holder.radioYesFullScore.setChecked(true);
            } else {
                holder.radioNoFullScore.setChecked(true);
            }
        } else {
            holder.radioMultipleChoice.setChecked(true);
            holder.containerMultipleChoice.setVisibility(View.VISIBLE);
            holder.containerYesNoQuestion.setVisibility(View.GONE);
        }
        
        // Load answers from description field
        question.loadAnswersFromDescription();
        
        // Set up answer options if this is a multiple choice question
        if (holder.answerOptionsAdapter == null) {
            Map<String, Integer> optionsWithPercentages = extractAnswerOptions(question.getDescription());
            holder.answerOptionsAdapter = new AnswerOptionAdapter(optionsWithPercentages);
            holder.recyclerAnswerOptions.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerAnswerOptions.setAdapter(holder.answerOptionsAdapter);
            
            // Set up drag-and-drop functionality
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new AnswerItemTouchHelperCallback(holder.answerOptionsAdapter));
            itemTouchHelper.attachToRecyclerView(holder.recyclerAnswerOptions);
            holder.answerOptionsAdapter.setTouchHelper(itemTouchHelper); // Set the touch helper reference
            
            holder.answerOptionsAdapter.setAnswerChangeListener(optionsWithPercentages1 -> {
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
                
                // Add all options with percentages
                for (Map.Entry<String, Integer> entry : optionsWithPercentages1.entrySet()) {
                    String option = entry.getKey();
                    Integer percentage = entry.getValue();
                    descBuilder.append("option:").append(option).append("\n");
                    descBuilder.append("percentage:").append(percentage).append("\n");
                }
                
                question.setDescription(descBuilder.toString().trim());
                
                // Update the answers list in the Question object
                question.loadAnswersFromDescription();
            });
            
            // Call once to initialize percentages based on position
            holder.answerOptionsAdapter.updatePercentagesBasedOnPosition();
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
                    holder.containerYesNoQuestion.setVisibility(View.GONE);
                } else {
                    questions.get(adapterPosition).setType("yes_no_question");
                    holder.containerMultipleChoice.setVisibility(View.GONE);
                    holder.containerYesNoQuestion.setVisibility(View.VISIBLE);
                    
                    // Set default Yes/No values
                    Question currentQuestion = questions.get(adapterPosition);
                    StringBuilder descBuilder = getStringBuilder();
                    currentQuestion.setDescription(descBuilder.toString().trim());
                    holder.radioYesFullScore.setChecked(true);
                }
            }
        });
        
        // Set up Yes/No score radio group listener
        holder.radioGroupYesNoScore.setOnCheckedChangeListener((group, checkedId) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Question currentQuestion = questions.get(adapterPosition);
                StringBuilder descBuilder = new StringBuilder();
                
                // Update the yes/no score setting
                if (checkedId == R.id.radio_yes_full_score) {
                    descBuilder.append("yes_full_score:true\n");
                } else {
                    descBuilder.append("yes_full_score:false\n");
                }
                
                currentQuestion.setDescription(descBuilder.toString().trim());
            }
        });
        
        // Set up Add Answer button
        holder.btnAddAnswer.setOnClickListener(v -> holder.answerOptionsAdapter.addNewOption());
        
        // Set up Delete Question button
        holder.btnDeleteQuestion.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onQuestionDeleted(adapterPosition);
            }
        });

    }

    @NonNull
    private static StringBuilder getStringBuilder() {
        StringBuilder descBuilder = new StringBuilder();

        // Set Yes to 100% by default
        descBuilder.append("yes_full_score:true\n");
        return descBuilder;
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    private Map<String, Integer> extractAnswerOptions(String description) {
        Map<String, Integer> optionsWithPercentages = new HashMap<>();
        if (description != null && !description.isEmpty()) {
            String currentOption = null;
            for (String line : description.split("\n")) {
                if (line.startsWith("option:")) {
                    currentOption = line.substring("option:".length());
                    optionsWithPercentages.put(currentOption, 0); // Default percentage
                } else if (line.startsWith("percentage:") && currentOption != null) {
                    try {
                        int percentage = Integer.parseInt(line.substring("percentage:".length()));
                        optionsWithPercentages.put(currentOption, percentage);
                    } catch (NumberFormatException e) {
                        // Use default percentage if parsing fails
                    }
                }
            }
        }
        
        // If no options found, return empty map
        return optionsWithPercentages;
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionNumber;
        TextInputEditText questionText;
        RadioGroup radioGroupType;
        RadioButton radioMultipleChoice;
        RadioButton radioYesNoQuestion;
        LinearLayout containerMultipleChoice;
        LinearLayout containerYesNoQuestion;
        RadioGroup radioGroupYesNoScore;
        RadioButton radioYesFullScore;
        RadioButton radioNoFullScore;
        RecyclerView recyclerAnswerOptions;
        Button btnAddAnswer;
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
            radioYesNoQuestion = itemView.findViewById(R.id.radio_yes_no_question);
            containerMultipleChoice = itemView.findViewById(R.id.container_multiple_choice);
            containerYesNoQuestion = itemView.findViewById(R.id.container_yes_no_question);
            radioGroupYesNoScore = itemView.findViewById(R.id.radio_group_yes_no_score);
            radioYesFullScore = itemView.findViewById(R.id.radio_yes_full_score);
            radioNoFullScore = itemView.findViewById(R.id.radio_no_full_score);
            recyclerAnswerOptions = itemView.findViewById(R.id.recycler_answer_options);
            btnAddAnswer = itemView.findViewById(R.id.btn_add_answer);
            //btnAddImage = itemView.findViewById(R.id.btn_add_image);
            btnDeleteQuestion = itemView.findViewById(R.id.btn_delete_question);
            imageContainer = itemView.findViewById(R.id.image_container);
            questionImage = itemView.findViewById(R.id.question_image);
            btnRemoveImage = itemView.findViewById(R.id.btn_remove_image);
        }
    }

    /**
     * ItemTouchHelper callback to handle drag-and-drop for answer options
     */
    private static class AnswerItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private final AnswerOptionAdapter adapter;

        AnswerItemTouchHelperCallback(AnswerOptionAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false; // We'll start drag in onTouch with the drag handle
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false; // No swipe to delete
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
            adapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // No swipe functionality
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            // Update percentages after drag completes
            adapter.updatePercentagesBasedOnPosition();
        }
    }

    // Nested adapter for answer options
    private static class AnswerOptionAdapter extends RecyclerView.Adapter<AnswerOptionAdapter.OptionViewHolder> {
        private final List<String> options;
        private final List<Integer> percentages;
        private AnswerChangeListener listener;
        private ItemTouchHelper touchHelper;

        interface AnswerChangeListener {
            void onOptionsChanged(Map<String, Integer> optionsWithPercentages);
        }

        AnswerOptionAdapter(Map<String, Integer> optionsWithPercentages) {
            this.options = new ArrayList<>(optionsWithPercentages.keySet());
            this.percentages = new ArrayList<>();
            
            for (String option : options) {
                percentages.add(optionsWithPercentages.get(option));
            }
            
            if (this.options.isEmpty()) {
                // Add two default empty options with even percentages
                this.options.add("");
                this.options.add("");
                this.percentages.add(50);
                this.percentages.add(50);
            }
        }

        void setTouchHelper(ItemTouchHelper touchHelper) {
            this.touchHelper = touchHelper;
        }

        void setAnswerChangeListener(AnswerChangeListener listener) {
            this.listener = listener;
            notifyOptionsChanged();
        }

        void addNewOption() {
            options.add("");
            percentages.add(0);
            notifyItemInserted(options.size() - 1);
            // Update percentages based on new position
            updatePercentagesBasedOnPosition();
        }

        void updateOptions(Map<String, Integer> optionsWithPercentages) {
            options.clear();
            percentages.clear();
            
            for (Map.Entry<String, Integer> entry : optionsWithPercentages.entrySet()) {
                options.add(entry.getKey());
                percentages.add(entry.getValue());
            }
            
            if (options.isEmpty()) {
                options.add("");
                options.add("");
                percentages.add(50);
                percentages.add(50);
            }
            
            notifyDataSetChanged();
        }

        void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(options, i, i + 1);
                    Collections.swap(percentages, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(options, i, i - 1);
                    Collections.swap(percentages, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        /**
         * Updates percentages based on position - highest option gets 100%, lowest gets 0%,
         * and others are distributed evenly between them.
         * For example: 5 options -> 100%, 75%, 50%, 25%, 0%
         * 3 options -> 100%, 50%, 0%
         */
        void updatePercentagesBasedOnPosition() {
            int totalOptions = options.size();
            if (totalOptions == 0) return;
            
            // For each option, calculate its percentage based on position
            for (int i = 0; i < totalOptions; i++) {
                // If there's only one option, give it 100%
                if (totalOptions == 1) {
                    percentages.set(i, 100);
                } else {
                    // Calculate based on position - index 0 is the highest (100%), last index is lowest (0%)
                    // For positions in between, calculate evenly
                    
                    // Calculate as a percentage between 0 and 100 based on position
                    // Formula: (n-1-i)/(n-1) * 100 where n is totalOptions and i is index
                    // This gives 100% for i=0, 0% for i=n-1, and evenly distributed values in between
                    
                    int percent = (int)((float)(totalOptions - 1 - i) / (totalOptions - 1) * 100);
                    
                    percentages.set(i, percent);
                }
            }
            
            notifyDataSetChanged();
            notifyOptionsChanged();
        }

        private void notifyOptionsChanged() {
            if (listener != null) {
                // Filter out empty options and check for duplicates
                Map<String, Integer> optionsWithPercentages = new HashMap<>();
                Set<String> uniqueOptions = new HashSet<>();
                boolean hasDuplicates = false;
                
                for (int i = 0; i < options.size(); i++) {
                    String option = options.get(i).trim();
                    if (!option.isEmpty()) {
                        // Check if this is a duplicate
                        if (!uniqueOptions.add(option)) {
                            hasDuplicates = true;
                            // We don't return early as we want to check all options
                            // and highlight all duplicates through the UI
                        }
                        optionsWithPercentages.put(option, percentages.get(i));
                    }
                }
                
                // Only update the model if there are no duplicates
                if (!hasDuplicates) {
                    listener.onOptionsChanged(optionsWithPercentages);
                }
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
            holder.percentText.setText(String.valueOf(percentages.get(position)));
            
            // Reset any previous error state
            holder.optionText.setError(null);
            
            // Check for duplicates at bind time and show error if found
            checkForDuplicatesAndShowError(holder, position);
            
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
                        String newValue = s.toString().trim();
                        options.set(adapterPosition, newValue);
                        
                        // Check for duplicates and set error if found
                        checkForDuplicatesAndShowError(holder, adapterPosition);
                        
                        notifyOptionsChanged();
                    }
                }
            });
            
            // Set up drag handle
            holder.btnDragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (touchHelper != null) {
                        touchHelper.startDrag(holder);
                    }
                }
                return false;
            });
            
            holder.btnDeleteOption.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    if (options.size() > 2) { // Keep at least 2 options
                        options.remove(adapterPosition);
                        percentages.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        notifyItemRangeChanged(adapterPosition, options.size() - adapterPosition);
                        // Update percentages after deletion
                        updatePercentagesBasedOnPosition();
                    } else {
                        // Show error message - can't have less than 2 options
                        Toast.makeText(v.getContext(), "Need at least 2 options", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
        /**
         * Checks if the answer text at the given position is a duplicate of any other answer
         * and shows an error if it is.
         * @param holder The ViewHolder containing the answer text field
         * @param position The position of the answer to check
         * @return true if there is a duplicate, false otherwise
         */
        private boolean checkForDuplicatesAndShowError(OptionViewHolder holder, int position) {
            String currentText = options.get(position).trim();
            
            // Don't consider empty text as duplicate
            if (currentText.isEmpty()) {
                holder.optionText.setError(null);
                return false;
            }
            
            // Check for duplicates
            boolean hasDuplicate = false;
            for (int i = 0; i < options.size(); i++) {
                if (i != position && options.get(i).trim().equals(currentText)) {
                    hasDuplicate = true;
                    break;
                }
            }
            
            // Show error if duplicate found
            if (hasDuplicate) {
                holder.optionText.setError("Duplicate answer text");
                return true;
            } else {
                holder.optionText.setError(null);
                return false;
            }
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        static class OptionViewHolder extends RecyclerView.ViewHolder {
            EditText optionText;
            EditText percentText;
            ImageButton btnDeleteOption;
            ImageButton btnDragHandle;

            OptionViewHolder(@NonNull View itemView) {
                super(itemView);
                optionText = itemView.findViewById(R.id.edit_answer_text);
                percentText = itemView.findViewById(R.id.edit_answer_percent);
                btnDeleteOption = itemView.findViewById(R.id.btn_delete_answer);
                btnDragHandle = itemView.findViewById(R.id.btn_drag_handle);
            }
        }
    }
}