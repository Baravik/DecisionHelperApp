package com.decisionhelperapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.adapters.QuestionAdapter;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.models.QuizQuestions;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.OnBackPressedCallback;

public class CreateQuizActivity extends BaseActivity {

    private TextInputEditText editQuizName;
    private TextInputEditText editQuizDescription;
    private RecyclerView recyclerQuestions;
    private QuestionAdapter questionAdapter;
    private CircularProgressIndicator progressIndicator;
    private SwitchMaterial switchPublic;

    private final List<Question> questionsList = new ArrayList<>();
    private final List<String> categories = Arrays.asList("General", "Business", "Education", "Health", "Technology", "Lifestyle");
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageReference storageRef;

    private int currentEditingQuestionPosition = -1;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.create_quiz_title);
        }
        
        // Initialize views
        editQuizName = findViewById(R.id.edit_quiz_name);
        editQuizDescription = findViewById(R.id.edit_quiz_description);
        recyclerQuestions = findViewById(R.id.recycler_questions);
        progressIndicator = findViewById(R.id.progress_indicator);
        switchPublic = findViewById(R.id.switch_public);
        
        // Set up category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        ((Spinner) findViewById(R.id.spinner_category)).setAdapter(categoryAdapter);
        
        // Set up questions recycler view
        questionAdapter = new QuestionAdapter(this, questionsList, new QuestionAdapter.QuestionAdapterListener() {
            @Override
            public void onQuestionDeleted(int position) {
                questionsList.remove(position);
                questionAdapter.notifyItemRemoved(position);
                questionAdapter.notifyItemRangeChanged(position, questionsList.size());
            }
            
            @Override
            public void onImageRequested(int position) {
                currentEditingQuestionPosition = position;
                openGallery();
            }

            @Override
            public void onImageRemoved(int position) {
                Question question = questionsList.get(position);
                question.setDescription(question.getDescription().replace("has_image:true", ""));
                questionAdapter.notifyItemChanged(position);
            }
        });
        recyclerQuestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerQuestions.setAdapter(questionAdapter);
        
        // Set up button listeners
        findViewById(R.id.btn_add_question).setOnClickListener(v -> addNewQuestion());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveQuiz());
        findViewById(R.id.btn_preview).setOnClickListener(v -> previewQuiz());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (currentEditingQuestionPosition != -1) {
                            Uri selectedImageUri = result.getData().getData();
                            uploadImage(selectedImageUri);
                        }
                    }
                });

        // Replace deprecated onBackPressed() by using onBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!Objects.requireNonNull(editQuizName.getText()).toString().isEmpty() ||
                        !Objects.requireNonNull(editQuizDescription.getText()).toString().isEmpty() ||
                        !questionsList.isEmpty()) {
                    new AlertDialog.Builder(CreateQuizActivity.this)
                            .setTitle("Discard Changes")
                            .setMessage("Are you sure you want to discard your changes?")
                            .setPositiveButton("Discard", (dialog, which) -> {
                                setEnabled(false);
                                CreateQuizActivity.this.getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    setEnabled(false);
                    CreateQuizActivity.this.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void addNewQuestion() {
        Question newQuestion = new Question();
        newQuestion.setId(UUID.randomUUID().toString());
        newQuestion.setTitle("");  // Will be filled in by user
        newQuestion.setType("multiple_choice");  // Default type
        
        // Add default options with equal percentages
        String defaultDesc = """
                option:
                percentage:50
                option:
                percentage:50
                """;
        newQuestion.setDescription(defaultDesc);
        
        newQuestion.setScore(0);  // Default score
        
        questionsList.add(newQuestion);
        questionAdapter.notifyItemInserted(questionsList.size() - 1);
        
        // Scroll to the new question
        recyclerQuestions.smoothScrollToPosition(questionsList.size() - 1);
    }

    private void saveQuiz() {
        if (validateQuiz()) {
            return;
        }

        showLoading(true);
        
        // Create quiz document
        String quizId = UUID.randomUUID().toString();
        Quiz quiz = new Quiz(
                quizId,
                (String) ((Spinner) findViewById(R.id.spinner_category)).getSelectedItem(),
                Objects.requireNonNull(editQuizDescription.getText()).toString().trim(),
                Objects.requireNonNull(editQuizName.getText()).toString().trim(),
                currentUser.getUid(),
                0,  // Default score
                ""  // Completed at will be empty for new quiz
        );
        
        // Save questions
        List<String> questionIds = new ArrayList<>();
        for (Question question : questionsList) {
            String questionId = question.getId();
            questionIds.add(questionId);
            db.collection("questions").document(questionId).set(question);
        }
        
        // Create QuizQuestions relationship
        QuizQuestions quizQuestions = new QuizQuestions(questionIds, quizId, 0);
        
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("isPublic", switchPublic.isChecked());

        // Save to Firestore
        db.collection("quizzes").document(quizId).set(quiz)
                .addOnSuccessListener(aVoid -> db.collection("quizQuestions").document(quizId).set(quizQuestions)
                        .addOnSuccessListener(aVoid1 -> db.collection("quizMetadata").document(quizId).set(quizData)
                                .addOnSuccessListener(aVoid2 -> {
                                    showLoading(false);
                                    Toast.makeText(CreateQuizActivity.this,
                                            R.string.quiz_saved, Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(this::handleError))
                        .addOnFailureListener(this::handleError))
                .addOnFailureListener(this::handleError);
    }

    private void previewQuiz() {
        if (validateQuiz()) {
            return;
        }
        
        // Create temporary quiz for preview
        String previewQuizId = "preview_" + UUID.randomUUID().toString();
        Quiz previewQuiz = new Quiz(
                previewQuizId,
                (String) ((Spinner)findViewById(R.id.spinner_category)).getSelectedItem(),
                Objects.requireNonNull(editQuizDescription.getText()).toString().trim(),
                Objects.requireNonNull(editQuizName.getText()).toString().trim(),
                currentUser.getUid(),
                0,
                ""
        );
        
        // Open QuizActivity with the preview quiz
        Intent intent = new Intent(this, TakeQuizActivity.class);
        intent.putExtra("QUIZ_ID", previewQuizId);
        intent.putExtra("QUIZ_TITLE", previewQuiz.getCustomTitle());
        intent.putExtra("IS_PREVIEW", true);
        intent.putParcelableArrayListExtra("QUESTIONS", new ArrayList<>(questionsList));
        startActivity(intent);
    }
    
    private boolean validateQuiz() {
        String quizName = Objects.requireNonNull(editQuizName.getText()).toString().trim();
        String quizDescription = Objects.requireNonNull(editQuizDescription.getText()).toString().trim();
        
        if (quizName.isEmpty()) {
            editQuizName.setError("Quiz name is required");
            return true;
        }
        
        if (quizDescription.isEmpty()) {
            editQuizDescription.setError("Description is required");
            return true;
        }
        
        if (questionsList.isEmpty()) {
            Toast.makeText(this, "Add at least one question", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        // Validate each question
        for (int i = 0; i < questionsList.size(); i++) {
            Question question = questionsList.get(i);
            if (question.getTitle().trim().isEmpty()) {
                Toast.makeText(this, "Question " + (i + 1) + " text is empty", 
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            
            if (question.getType().equals("multiple_choice")) {
                // Check if the question has at least 2 answer options
                String description = question.getDescription();
                if (!description.contains("option:")) {
                    Toast.makeText(this, "Multiple choice question " + (i + 1) + 
                            " needs at least 2 options", Toast.LENGTH_SHORT).show();
                    return true;
                }
                
                // Count options and check percentages
                int optionCount = 0;
                boolean hasPercentages = false;
                int totalPercentage = 0;
                
                for (String part : description.split("\n")) {
                    if (part.startsWith("option:")) {
                        optionCount++;
                    } else if (part.startsWith("percentage:")) {
                        hasPercentages = true;
                        try {
                            totalPercentage += Integer.parseInt(part.substring("percentage:".length()));
                        } catch (NumberFormatException e) {
                            // Ignore parsing errors
                        }
                    }
                }
                
                if (optionCount < 2) {
                    Toast.makeText(this, "Multiple choice question " + (i + 1) + 
                            " needs at least 2 options", Toast.LENGTH_SHORT).show();
                    return true;
                }
                
                // Check if percentages are defined properly
                if (!hasPercentages) {
                    Toast.makeText(this, "Question " + (i + 1) + 
                            " is missing percentages for options", Toast.LENGTH_SHORT).show();
                    return true;
                }
                
                // You might want to warn if total percentage isn't 100%, but we'll be flexible here
                // and just check if any percentage is defined
                if (totalPercentage == 0) {
                    Toast.makeText(this, "Question " + (i + 1) + 
                            " needs at least one option with a non-zero percentage", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            else if (question.getType().equals("yes_no_question")) {
                // Check if the yes/no score is defined
                String description = question.getDescription();
                if (!description.contains("yes_full_score:")) {
                    Toast.makeText(this, "Yes/No question " + (i + 1) + 
                            " needs to define which answer gives 100% score", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            else {
                // Invalid question type
                Toast.makeText(this, "Question " + (i + 1) + 
                        " has an invalid type", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        
        return false;
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImage(Uri imageUri) {
        showLoading(true);
        
        String imageName = "question_images/" + UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(imageName);
        
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (currentEditingQuestionPosition != -1) {
                        Question question = questionsList.get(currentEditingQuestionPosition);

                        // Parse the current description to keep all the options/percentages information
                        StringBuilder updatedDesc = new StringBuilder();

                        // Add image info
                        updatedDesc.append("has_image:true\n");
                        updatedDesc.append("image_url:").append(uri.toString()).append("\n");

                        // Preserve existing option and percentage data
                        String currentDesc = question.getDescription();
                        for (String line : currentDesc.split("\n")) {
                            if (line.startsWith("option:") || line.startsWith("percentage:") ||
                                line.startsWith("yes_full_score:")) {
                                updatedDesc.append(line).append("\n");
                            }
                        }

                        question.setDescription(updatedDesc.toString().trim());
                        questionAdapter.notifyItemChanged(currentEditingQuestionPosition);
                        showLoading(false);
                    }
                }))
                .addOnFailureListener(this::handleError);
    }
    
    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void handleError(Exception e) {
        showLoading(false);
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}