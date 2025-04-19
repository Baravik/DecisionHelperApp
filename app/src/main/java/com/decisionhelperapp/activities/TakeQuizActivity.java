package com.decisionhelperapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.OpenU.decisionhelperapp.R;
import com.bumptech.glide.Glide;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.QuizQuestions;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TakeQuizActivity extends BaseActivity {

    private String quizId;
    private boolean isPreview;
    private ArrayList<Question> previewQuestions;

    private TextView textQuestionTitle;
    private TextView textQuestionNumber;
    private LinearProgressIndicator progressIndicator;
    private RadioGroup radioOptions;
    private Button btnYes;
    private Button btnNo;
    private Button btnNext;
    private Button btnPrevious;
    private ImageView imageQuestion;
    private LinearLayout layoutYesNo;
    
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private final Map<String, String> userAnswers = new HashMap<>();
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_quiz);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Get data from intent
        quizId = getIntent().getStringExtra("QUIZ_ID");
        isPreview = getIntent().getBooleanExtra("IS_PREVIEW", false);
        
        if (isPreview) {
            previewQuestions = getIntent().getParcelableArrayListExtra("QUESTIONS");
        }

        // Initialize views
        textQuestionTitle = findViewById(R.id.text_question_title);
        textQuestionNumber = findViewById(R.id.text_question_number);
        progressIndicator = findViewById(R.id.progress_indicator);
        radioOptions = findViewById(R.id.radio_options);
        btnYes = findViewById(R.id.btn_yes);
        btnNo = findViewById(R.id.btn_no);
        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        imageQuestion = findViewById(R.id.image_question);
        layoutYesNo = findViewById(R.id.layout_yes_no);
        
        // Set button listeners
        btnNext.setOnClickListener(v -> goToNextQuestion());
        btnPrevious.setOnClickListener(v -> goToPreviousQuestion());
        btnYes.setOnClickListener(v -> selectYesNoAnswer("YES"));
        btnNo.setOnClickListener(v -> selectYesNoAnswer("NO"));

        // Disable next button initially
        btnNext.setEnabled(false);
        btnYes.setBackgroundColor(Color.GRAY);
        btnNo.setBackgroundColor(Color.GRAY);

        // Load quiz data
        if (isPreview) {
            // Use preview questions directly
            questions = new ArrayList<>(previewQuestions);
            showQuestion(0);
        } else {
            // Load from Firestore
            loadQuizQuestions();
        }
    }
    
    private void loadQuizQuestions() {
        db.collection("quizQuestions").document(quizId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        QuizQuestions quizQuestions = documentSnapshot.toObject(QuizQuestions.class);
                        if (quizQuestions != null && quizQuestions.getQuestionsId()!= null) {
                            loadQuestions(quizQuestions.getQuestionsId());
                        } else {
                            showError("Quiz data not found");
                        }
                    } else {
                        showError("Quiz not found");
                    }
                })
                .addOnFailureListener(e -> showError("Error loading quiz: " + e.getMessage()));
    }
    
    private void loadQuestions(List<String> questionIds) {
        questions.clear();
        
        // Show loading
        progressIndicator.setVisibility(View.VISIBLE);
        
        // Create a counter to track when all questions are loaded
        final int[] loadedCount = {0};
        
        for (String questionId : questionIds) {
            db.collection("questions").document(questionId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Question question = documentSnapshot.toObject(Question.class);
                        if (question != null) {
                            questions.add(question);
                        }
                        
                        loadedCount[0]++;
                        if (loadedCount[0] == questionIds.size()) {
                            // All questions loaded
                            progressIndicator.setVisibility(View.GONE);
                            if (!questions.isEmpty()) {
                                showQuestion(0);
                            } else {
                                showError("No questions found for this quiz");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == questionIds.size()) {
                            progressIndicator.setVisibility(View.GONE);
                            if (!questions.isEmpty()) {
                                showQuestion(0);
                            } else {
                                showError("No questions found for this quiz");
                            }
                        }
                    });
        }
    }
    
    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            return;
        }
        
        currentQuestionIndex = index;
        Question question = questions.get(index);

        // Reset button states before setting up the new question
        btnNext.setEnabled(false);
        btnYes.setBackgroundColor(Color.GRAY);
        btnNo.setBackgroundColor(Color.GRAY);

        // Update progress
        textQuestionNumber.setText(String.format(Locale.getDefault(), 
                "Question %d of %d", index + 1, questions.size()));
        progressIndicator.setProgress((index * 100) / questions.size());
        
        // Set question title
        textQuestionTitle.setText(question.getTitle());
        
        // Show/hide image if present
        if (question.getDescription().contains("has_image:true")) {
            imageQuestion.setVisibility(View.VISIBLE);
            
            // Extract image URL
            String description = question.getDescription();
            String[] lines = description.split("\n");
            for (String line : lines) {
                if (line.startsWith("image_url:")) {
                    String imageUrl = line.substring("image_url:".length());
                    Glide.with(this).load(imageUrl).into(imageQuestion);
                    break;
                }
            }
        } else {
            imageQuestion.setVisibility(View.GONE);
        }
        
        // Set up question type UI
        if ("multiple_choice".equals(question.getType())) {
            setupMultipleChoiceQuestion(question);
        } else if ("yes_no_question".equals(question.getType())) {
            setupYesNoQuestion();
        }
        
        // Update navigation buttons
        btnPrevious.setVisibility(index > 0 ? View.VISIBLE : View.INVISIBLE);
        if (index == questions.size() - 1) {
            btnNext.setText(R.string.finish);
        } else {
            btnNext.setText(R.string.next);
        }
        
        // Restore previous answers if any
        restorePreviousAnswers(question);
    }
    
    private void setupMultipleChoiceQuestion(Question question) {
        // Show radio buttons, hide yes/no buttons
        radioOptions.setVisibility(View.VISIBLE);
        layoutYesNo.setVisibility(View.GONE);
        
        // Clear previous options
        radioOptions.removeAllViews();
        
        // Parse options from description
        String description = question.getDescription();
        String[] lines = description.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            if (line.startsWith("option:")) {
                String optionText = line.substring("option:".length());
                
                // Find percentage for this option (assumes percentage line follows option line)
                int percentage = 0;
                if (i + 1 < lines.length && lines[i + 1].startsWith("percentage:")) {
                    try {
                        percentage = Integer.parseInt(lines[i + 1].substring("percentage:".length()));
                    } catch (NumberFormatException e) {
                        // Use default 0
                    }
                }
                
                // Create radio button
                RadioButton radioButton = getRadioButton(question, optionText, percentage);

                radioOptions.addView(radioButton);
            }
        }
    }

    @NonNull
    private RadioButton getRadioButton(Question question, String optionText, int percentage) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(optionText);
        radioButton.setTag(percentage); // Store percentage as tag

        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Record this answer
                userAnswers.put(question.getId(), optionText);
                btnNext.setEnabled(true);
            }
        });
        return radioButton;
    }

    private void setupYesNoQuestion() {
        // Show yes/no buttons, hide radio buttons
        radioOptions.setVisibility(View.GONE);
        layoutYesNo.setVisibility(View.VISIBLE);
        
        // Reset button state
        btnYes.setSelected(false);
        btnNo.setSelected(false);
    }
    
    private void selectYesNoAnswer(String answer) {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        Question question = questions.get(currentQuestionIndex);
        
        // Update button state
        btnYes.setSelected("YES".equals(answer));
        btnNo.setSelected("NO".equals(answer));

        btnYes.setBackgroundColor("YES".equals(answer) ? Color.RED : Color.GRAY);
        btnNo.setBackgroundColor("NO".equals(answer) ? Color.RED : Color.GRAY);
        
        // Record answer
        userAnswers.put(question.getId(), answer);
        // Enable "Next" button
        btnNext.setEnabled(true);
    }
    
    private void restorePreviousAnswers(Question question) {
        String previousAnswer = userAnswers.get(question.getId());
        if (previousAnswer == null) {
            return;
        }
        
        if ("multiple_choice".equals(question.getType())) {
            // Find and check the matching radio button
            for (int i = 0; i < radioOptions.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) radioOptions.getChildAt(i);
                if (radioButton.getText().toString().equals(previousAnswer)) {
                    radioButton.setChecked(true);
                    break;
                }
            }
        } else if ("yes_no_question".equals(question.getType())) {
            btnYes.setSelected("YES".equals(previousAnswer));
            btnNo.setSelected("NO".equals(previousAnswer));

            btnYes.setBackgroundColor("YES".equals(previousAnswer) ? Color.RED : Color.GRAY);
            btnNo.setBackgroundColor("NO".equals(previousAnswer) ? Color.RED : Color.GRAY);

            btnNext.setEnabled(true); // Ensure next button is enabled if answer exists
        }
    }
    
    private void goToNextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            showQuestion(currentQuestionIndex + 1);
        } else {
            // Last question - finish quiz
            calculateScore();
        }
    }
    
    private void goToPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            showQuestion(currentQuestionIndex - 1);
        }
    }
    
    private void calculateScore() {
        double totalScore = 0;
        double maxPossibleScore = 0;
        
        for (Question question : questions) {
            maxPossibleScore += 100; // Each question worth 100 points maximum
            
            String userAnswer = userAnswers.get(question.getId());
            if (userAnswer == null) {
                continue; // Unanswered question
            }
            
            if ("multiple_choice".equals(question.getType())) {
                // Find percentage for the chosen option
                String description = question.getDescription();
                String[] lines = description.split("\n");
                
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    
                    if (line.startsWith("option:") && line.substring("option:".length()).equals(userAnswer)) {
                        // Found the selected option, get its percentage
                        if (i + 1 < lines.length && lines[i + 1].startsWith("percentage:")) {
                            try {
                                int percentage = Integer.parseInt(lines[i + 1].substring("percentage:".length()));
                                totalScore += percentage;
                            } catch (NumberFormatException e) {
                                // Ignore parsing errors
                            }
                        }
                        break;
                    }
                }
            } else if ("yes_no_question".equals(question.getType())) {
                // Check if the user selected the correct answer
                String description = question.getDescription();
                String[] lines = description.split("\n");
                
                for (String line : lines) {
                    if (line.startsWith("yes_full_score:")) {
                        String fullScoreAnswer = line.substring("yes_full_score:".length());
                        boolean isYesFullScore = "true".equals(fullScoreAnswer);
                        
                        if (("YES".equals(userAnswer) && isYesFullScore) || 
                            ("NO".equals(userAnswer) && !isYesFullScore)) {
                            totalScore += 100;
                        }
                        break;
                    }
                }
            }
        }
        
        // Calculate final score as a percentage
        double finalScore = (totalScore / maxPossibleScore) * 100;
        
        // Show result dialog
        showResultDialog(finalScore);
    }
    
    private void showResultDialog(double finalScore) {
        int roundedScore = (int) Math.round(finalScore);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Complete");
        builder.setMessage("Your score: " + roundedScore + "%\nPlease enter a name for this quiz:");

        final EditText input = new EditText(this);
        input.setHint("Enter quiz name");
        builder.setView(input);

        builder.setCancelable(false);
        builder.setPositiveButton("View My Scores", null);
        builder.setNegativeButton(isPreview ? "Back to Edit" : "Done", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positive.setEnabled(false);
            negative.setEnabled(false);

            input.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean enabled = !s.toString().trim().isEmpty();
                    positive.setEnabled(enabled);
                    negative.setEnabled(enabled);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });

            View.OnClickListener saveAndFinish = v -> {
                String quizName = input.getText().toString().trim();
                if (!isPreview && currentUser != null) {
                    db.collection("userQuizScores")
                            .whereEqualTo("userId", currentUser.getUid())
                            .whereEqualTo("quizName", quizName)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (!snapshot.isEmpty()) {
                                    new AlertDialog.Builder(TakeQuizActivity.this)
                                            .setTitle("Name Exists")
                                            .setMessage("This test name already exists. Continuing will replace the previous score. Do you want to continue?")
                                            .setPositiveButton("Continue", (d2, which) -> {
                                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                                    doc.getReference().delete();
                                                }
                                                saveScore(roundedScore, quizName);
                                                dialog.dismiss();
                                                if (v == positive) {
                                                    startActivity(new Intent(TakeQuizActivity.this, ScoresActivity.class));
                                                }
                                                finish();
                                            })
                                            .setNegativeButton("Cancel", (d2, which) -> d2.dismiss())
                                            .show();
                                } else {
                                    saveScore(roundedScore, quizName);
                                    dialog.dismiss();
                                    if (v == positive) {
                                        startActivity(new Intent(TakeQuizActivity.this, ScoresActivity.class));
                                    }
                                    finish();
                                }
                            });
                } else {
                    dialog.dismiss();
                    finish();
                }
            };

            positive.setOnClickListener(saveAndFinish);
            negative.setOnClickListener(saveAndFinish);
        });

        dialog.show();
    }

    private void saveScore(int score, String quizName) {
        // Update quiz with completion info
        Map<String, Object> completionData = new HashMap<>();
        completionData.put("score", score);
        completionData.put("completedAt", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date()));
        completionData.put("userId", currentUser.getUid());
        completionData.put("quizId", quizId);
        completionData.put("quizName", quizName);

        db.collection("userQuizScores")
                .add(completionData)
                .addOnFailureListener(e -> 
                        Toast.makeText(this, "Failed to save score", Toast.LENGTH_SHORT).show());
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}