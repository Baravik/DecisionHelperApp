package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.OpenU.decisionhelperapp.R;
import com.bumptech.glide.Glide;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.database.QuizQuestionsDAO;
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

public class TakeQuizActivity extends AppCompatActivity {
    private static final String TAG = "TakeQuizActivity";
    
    private String quizId;
    private String userId;
    private String quizTitle;
    private List<Question> questions = new ArrayList<>();
    
    private TextView tvTitle;
    private TextView tvQuestion;
    private TextView tvProgress;
    private Button btnYes;
    private Button btnNo;
    private Button btnPrev;
    private Button btnNext;
    private Button btnSubmit;
    private ProgressBar progressBar;
    
    private int currentQuestionIndex = 0;
    private List<Boolean> answers = new ArrayList<>();
    private List<Integer> importanceRatings = new ArrayList<>();
    
    private TextView textQuestionTitle;
    private TextView textQuestionNumber;
    private LinearProgressIndicator progressIndicator;
    private RadioGroup radioOptions;
    private ImageView imageQuestion;
    private LinearLayout layoutYesNo;
    
    // Rating UI elements
    private LinearLayout layoutImportanceRating;
    private Button[] ratingButtons = new Button[5];
    private TextView tvImportancePrompt;
    
    // Question flow state
    private boolean isRatingPhase = true;
    
    private final Map<String, String> userAnswers = new HashMap<>();
    private final Map<String, Integer> questionImportance = new HashMap<>();
    
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
        userId = getIntent().getStringExtra("USER_ID");
        quizTitle = getIntent().getStringExtra("QUIZ_TITLE");
        
        // Set toolbar title
        if (actionBar != null) {
            actionBar.setTitle(quizTitle);
        }
        
        // Initialize views
        textQuestionTitle = findViewById(R.id.text_question_title);
        textQuestionNumber = findViewById(R.id.text_question_number);
        progressIndicator = findViewById(R.id.progress_indicator);
        radioOptions = findViewById(R.id.radio_options);
        btnYes = findViewById(R.id.btn_yes);
        btnNo = findViewById(R.id.btn_no);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        imageQuestion = findViewById(R.id.image_question);
        layoutYesNo = findViewById(R.id.layout_yes_no);
        
        // Initialize importance rating views
        layoutImportanceRating = findViewById(R.id.layout_importance_rating);
        tvImportancePrompt = findViewById(R.id.tv_importance_prompt);
        ratingButtons[0] = findViewById(R.id.btn_rating_1);
        ratingButtons[1] = findViewById(R.id.btn_rating_2);
        ratingButtons[2] = findViewById(R.id.btn_rating_3);
        ratingButtons[3] = findViewById(R.id.btn_rating_4);
        ratingButtons[4] = findViewById(R.id.btn_rating_5);
        
        // Set up rating button listeners
        for (int i = 0; i < ratingButtons.length; i++) {
            final int rating = i + 1;
            ratingButtons[i].setOnClickListener(v -> setImportanceRating(rating));
        }
        
        // Set button listeners
        btnNext.setOnClickListener(v -> goToNextQuestion());
        btnPrev.setOnClickListener(v -> goToPreviousQuestion());
        btnYes.setOnClickListener(v -> selectYesNoAnswer("YES"));
        btnNo.setOnClickListener(v -> selectYesNoAnswer("NO"));
        
        // Load quiz data
        loadQuizQuestions(quizId);
        
        // Initialize UI components
        tvTitle = findViewById(R.id.tvQuizTitle);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        btnYes = findViewById(R.id.btn_yes);
        btnNo = findViewById(R.id.btn_no);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnSubmit = findViewById(R.id.btnSubmitQuiz);
        progressBar = findViewById(R.id.progressBar);
        
        // Set up button listeners
        setupButtonListeners();
    }
    
    private void loadQuizQuestions(String quizId) {
        showLoading(true);
        
        QuizQuestionsDAO quizQuestionsDAO = new QuizQuestionsDAO();
        quizQuestionsDAO.getQuestionsByQuizId(quizId, new QuizQuestionsDAO.QuizQuestionsCallback() {
            @Override
            public void onCallback(List<QuizQuestions> quizQuestionsList) {
                if (quizQuestionsList != null && !quizQuestionsList.isEmpty()) {
                    // Get question IDs from the first QuizQuestions object
                    QuizQuestions quizQuestions = quizQuestionsList.get(0);
                    List<String> questionIds = quizQuestions.getQuestionsId();
                    
                    if (questionIds != null && !questionIds.isEmpty()) {
                        // Now load each question
                        loadQuestionsById(questionIds);
                    } else {
                        showLoading(false);
                        showError("No questions found for this quiz");
                    }
                } else {
                    showLoading(false);
                    showError("No questions mapping found for this quiz");
                }
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Error loading quiz questions", e);
                showError("Failed to load questions: " + e.getMessage());
            }
        });
    }
    
    private void loadQuestionsById(List<String> questionIds) {
        // For each question ID, load the actual question
        // This is a simplified version - you'll need to adapt to your actual Question DAO
        
        // Initialize answers array with nulls (no answer)
        for (int i = 0; i < questionIds.size(); i++) {
            answers.add(null);
            importanceRatings.add(null); // Initialize importance ratings
        }
        
        // TODO: Replace this with actual question loading from your database
        // This is just a placeholder implementation
        for (String questionId : questionIds) {
            Question question = new Question();
            question.setId(questionId);
            question.setTitle("Sample question " + questionId);
            questions.add(question);
        }
        
        showLoading(false);
        
        // Display the first question
        if (!questions.isEmpty()) {
            displayQuestion(0);
        } else {
            showError("No questions available");
        }
    }
    
    private void displayQuestion(int index) {
        if (index >= 0 && index < questions.size()) {
            Question question = questions.get(index);
            tvQuestion.setText(question.getTitle());
            
            // Update progress text
            tvProgress.setText(getString(R.string.question_progress, index + 1, questions.size()));
            
            // Update button states
            btnPrev.setEnabled(index > 0);
            btnNext.setEnabled(index < questions.size() - 1);
            btnSubmit.setVisibility(index == questions.size() - 1 ? View.VISIBLE : View.GONE);
            
            // Reset button selection if no answer yet
            Boolean answer = index < answers.size() ? answers.get(index) : null;
            if (answer != null) {
                btnYes.setSelected(answer);
                btnNo.setSelected(!answer);
            } else {
                btnYes.setSelected(false);
                btnNo.setSelected(false);
            }
        }
    }
    
    private void setupButtonListeners() {
        btnYes.setOnClickListener(v -> {
            answers.set(currentQuestionIndex, true);
            btnYes.setSelected(true);
            btnNo.setSelected(false);
            
            // Auto-advance to next question if not the last one
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
            }
        });
        
        btnNo.setOnClickListener(v -> {
            answers.set(currentQuestionIndex, false);
            btnYes.setSelected(false);
            btnNo.setSelected(true);
            
            // Auto-advance to next question if not the last one
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
            }
        });
        
        btnPrev.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayQuestion(currentQuestionIndex);
            }
        });
        
        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
            }
        });
        
        btnSubmit.setOnClickListener(v -> {
            // Check if all questions are answered
            boolean allAnswered = true;
            for (Boolean answer : answers) {
                if (answer == null) {
                    allAnswered = false;
                    break;
                }
            }
            
            if (allAnswered) {
                submitQuiz();
            } else {
                Toast.makeText(this, R.string.please_select_answer, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void submitQuiz() {
        // Calculate score based on answers
        int score = 0;
        for (Boolean answer : answers) {
            if (answer != null && answer) {
                score++;
            }
        }
        
        // Show result activity
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("QUIZ_ID", quizId);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL_QUESTIONS", questions.size());
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
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
        
        // Reset to importance rating phase by default
        isRatingPhase = true;
        
        // Check if we already have an importance rating for this question
        Integer existingRating = questionImportance.get(question.getId());
        if (existingRating != null) {
            // If we already rated this question, we can skip to the answer phase
            isRatingPhase = false;
        }
        
        updateQuestionPhaseUI();
        
        // Update navigation buttons
        btnPrev.setVisibility(index > 0 ? View.VISIBLE : View.INVISIBLE);
        if (index == questions.size() - 1) {
            btnNext.setText(R.string.finish);
        } else {
            btnNext.setText(R.string.next);
        }
    }
    
    private void updateQuestionPhaseUI() {
        Question question = questions.get(currentQuestionIndex);
        
        if (isRatingPhase) {
            // Show importance rating UI, hide answer UI
            layoutImportanceRating.setVisibility(View.VISIBLE);
            tvImportancePrompt.setVisibility(View.VISIBLE);
            radioOptions.setVisibility(View.GONE);
            layoutYesNo.setVisibility(View.GONE);
            
            // Reset rating button selection
            Integer existingRating = questionImportance.get(question.getId());
            for (int i = 0; i < ratingButtons.length; i++) {
                ratingButtons[i].setSelected(existingRating != null && existingRating == i + 1);
            }
        } else {
            // Show answer UI, hide importance rating UI
            layoutImportanceRating.setVisibility(View.GONE);
            tvImportancePrompt.setVisibility(View.GONE);
            
            if ("multiple_choice".equals(question.getType())) {
                setupMultipleChoiceQuestion(question);
            } else if ("yes_no_question".equals(question.getType())) {
                setupYesNoQuestion();
            }
            
            // Restore previous answers if any
            restorePreviousAnswers(question);
        }
    }
    
    private void setImportanceRating(int rating) {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        Question question = questions.get(currentQuestionIndex);
        
        // Update UI
        for (int i = 0; i < ratingButtons.length; i++) {
            ratingButtons[i].setSelected(i + 1 == rating);
        }
        
        // Store rating
        questionImportance.put(question.getId(), rating);
        
        // Move to answer phase
        isRatingPhase = false;
        updateQuestionPhaseUI();
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
                        
                        // Auto-advance to next question if not the last one
                        if (currentQuestionIndex < questions.size() - 1) {
                            currentQuestionIndex++;
                            showQuestion(currentQuestionIndex);
                        }
                    }
                });
                
                radioOptions.addView(radioButton);
            }
        }
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
        
        // Auto-advance to next question if not the last one
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
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
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        if (isRatingPhase) {
            // If we're in rating phase, move to answer phase
            if (!questionImportance.containsKey(currentQuestion.getId())) {
                Toast.makeText(this, "Please rate the importance of this question", Toast.LENGTH_SHORT).show();
                return;
            }
            
            isRatingPhase = false;
            updateQuestionPhaseUI();
        } else {
            // If we're in answer phase, check if question is answered
            if (!userAnswers.containsKey(currentQuestion.getId())) {
                Toast.makeText(this, "Please answer the question before continuing", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentQuestionIndex < questions.size() - 1) {
                // Move to next question
                showQuestion(currentQuestionIndex + 1);
            } else {
                // Last question - finish quiz
                calculateScore();
            }
        }
    }
    
    private void goToPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            // If in answer phase and no answer yet, go back to rating phase
            if (!isRatingPhase && !userAnswers.containsKey(questions.get(currentQuestionIndex).getId())) {
                isRatingPhase = true;
                updateQuestionPhaseUI();
            } else {
                // Otherwise go back to previous question
                showQuestion(currentQuestionIndex - 1);
            }
        } else if (!isRatingPhase) {
            // If on first question and in answer phase, go back to rating phase
            isRatingPhase = true;
            updateQuestionPhaseUI();
        }
    }
    
    private void calculateScore() {
        double totalScore = 0;
        double maxPossibleScore = 0;
        double weightedSum = 0;
        double totalWeight = 0;
        
        for (Question question : questions) {
            String questionId = question.getId();
            Integer importance = questionImportance.get(questionId);
            if (importance == null) importance = 3; // Default to middle importance
            
            double weight = importance; // Use importance as weight (1-5)
            totalWeight += weight;
            
            String userAnswer = userAnswers.get(questionId);
            if (userAnswer == null) {
                continue; // Unanswered question
            }
            
            double questionScore = 0;
            
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
                                questionScore = percentage;
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
                            questionScore = 100;
                        }
                        break;
                    }
                }
            }
            
            // Apply weight to question score
            weightedSum += questionScore * weight;
            maxPossibleScore += 100 * weight;
        }
        
        // Calculate final score as a percentage
        double finalScore = (weightedSum / maxPossibleScore) * 100;
        
        // Show result dialog
        showResultDialog(finalScore);
    }
    
    private void showResultDialog(double finalScore) {
        int roundedScore = (int) Math.round(finalScore);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Complete");
        builder.setMessage("Your score: " + roundedScore + "%");
        
        if (currentUser != null) {
            // Save score to Firestore
            saveScore(roundedScore);
            
            builder.setPositiveButton("View My Scores", (dialog, which) -> {
                Intent intent = new Intent(TakeQuizActivity.this, ScoresActivity.class);
                startActivity(intent);
                finish();
            });
        }
        
        builder.setNegativeButton("Done", (dialog, which) -> finish());
        
        builder.setCancelable(false);
        builder.show();
    }
    
    private void saveScore(int score) {
        if (currentUser == null) {
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
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Use getOnBackPressedDispatcher() to handle back press
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupYesNoQuestion() {
        // Show yes/no buttons, hide radio buttons
        radioOptions.setVisibility(View.GONE);
        layoutYesNo.setVisibility(View.VISIBLE);
        
        // Reset button state
        btnYes.setSelected(false);
        btnNo.setSelected(false);
    }
}