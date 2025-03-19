package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.OpenU.decisionhelperapp.R;
import com.bumptech.glide.Glide;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.QuizQuestions;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private Map<String, String> userAnswers = new HashMap<>();
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_quiz);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        // Get data from intent
        quizId = getIntent().getStringExtra("QUIZ_ID");
        String quizTitle = getIntent().getStringExtra("QUIZ_TITLE");
        isPreview = getIntent().getBooleanExtra("IS_PREVIEW", false);
        
        if (isPreview) {
            previewQuestions = getIntent().getParcelableArrayListExtra("QUESTIONS");
        }
        
        // Set toolbar title
        if (actionBar != null) {
            if (isPreview) {
                actionBar.setTitle("Preview: " + quizTitle);
            } else {
                actionBar.setTitle(quizTitle);
            }
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
            setupYesNoQuestion(question);
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
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(optionText);
                radioButton.setTag(percentage); // Store percentage as tag
                
                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        // Record this answer
                        userAnswers.put(question.getId(), optionText);
                    }
                });
                
                radioOptions.addView(radioButton);
            }
        }
    }
    
    private void setupYesNoQuestion(Question question) {
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
        
        // Record answer
        userAnswers.put(question.getId(), answer);
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
        builder.setMessage("Your score: " + roundedScore + "%");
        
        if (!isPreview && currentUser != null) {
            // Save score to Firestore
            saveScore(roundedScore);
            
            builder.setPositiveButton("View My Scores", (dialog, which) -> {
                Intent intent = new Intent(TakeQuizActivity.this, ScoresActivity.class);
                startActivity(intent);
                finish();
            });
        }
        
        builder.setNegativeButton(isPreview ? "Back to Edit" : "Done", (dialog, which) -> finish());
        
        builder.setCancelable(false);
        builder.show();
    }
    
    private void saveScore(int score) {
        if (currentUser == null || isPreview) {
            return;
        }
        
        // Update quiz with completion info
        Map<String, Object> completionData = new HashMap<>();
        completionData.put("score", score);
        completionData.put("completedAt", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date()));
        completionData.put("userId", currentUser.getUid());
        
        db.collection("userQuizScores").document(currentUser.getUid() + "_" + quizId)
                .set(completionData)
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