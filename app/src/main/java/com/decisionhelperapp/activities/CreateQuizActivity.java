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
import androidx.annotation.Nullable;
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
import java.util.UUID;

public class CreateQuizActivity extends BaseActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    
    private TextInputEditText editQuizName;
    private TextInputEditText editQuizDescription;
    private RecyclerView recyclerQuestions;
    private QuestionAdapter questionAdapter;
    private CircularProgressIndicator progressIndicator;
    private SwitchMaterial switchPublic;

    private List<Question> questionsList = new ArrayList<>();
    private List<String> categories = Arrays.asList("General", "Business", "Education", "Health", "Technology", "Lifestyle");
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private int currentEditingQuestionPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
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
    }

    private void addNewQuestion() {
        Question newQuestion = new Question();
        newQuestion.setId(UUID.randomUUID().toString());
        newQuestion.setTitle("");  // Will be filled in by user
        newQuestion.setType("multiple_choice");  // Default type
        newQuestion.setDescription("");  // Will be filled in by user
        newQuestion.setScore(0);  // Default score
        
        questionsList.add(newQuestion);
        questionAdapter.notifyItemInserted(questionsList.size() - 1);
        
        // Scroll to the new question
        recyclerQuestions.smoothScrollToPosition(questionsList.size() - 1);
    }

    private void saveQuiz() {
        if (!validateQuiz()) {
            return;
        }

        showLoading(true);
        
        // Create quiz document
        String quizId = UUID.randomUUID().toString();
        Quiz quiz = new Quiz(
                quizId,
                (String) ((Spinner) findViewById(R.id.spinner_category)).getSelectedItem(),
                editQuizDescription.getText().toString().trim(),
                editQuizName.getText().toString().trim(),
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
                .addOnSuccessListener(aVoid -> {
                    db.collection("quizQuestions").document(quizId).set(quizQuestions)
                            .addOnSuccessListener(aVoid1 -> {
                                db.collection("quizMetadata").document(quizId).set(quizData)
                                        .addOnSuccessListener(aVoid2 -> {
                                            showLoading(false);
                                            Toast.makeText(CreateQuizActivity.this, 
                                                    R.string.quiz_saved, Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> handleError(e));
                            })
                            .addOnFailureListener(e -> handleError(e));
                })
                .addOnFailureListener(e -> handleError(e));
    }

    private void previewQuiz() {
        if (!validateQuiz()) {
            return;
        }
        
        // Create temporary quiz for preview
        String previewQuizId = "preview_" + UUID.randomUUID().toString();
        Quiz previewQuiz = new Quiz(
                previewQuizId,
                (String) ((Spinner)findViewById(R.id.spinner_category)).getSelectedItem(),
                editQuizDescription.getText().toString().trim(),
                editQuizName.getText().toString().trim(),
                currentUser.getUid(),
                0,
                ""
        );
        
        // Open QuizActivity with the preview quiz
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("QUIZ_ID", previewQuizId);
        intent.putExtra("QUIZ_TITLE", previewQuiz.getCustomTitle());
        intent.putExtra("IS_PREVIEW", true);
        intent.putExtra("QUESTIONS", new ArrayList<>(questionsList));
        startActivity(intent);
    }
    
    private boolean validateQuiz() {
        String quizName = editQuizName.getText().toString().trim();
        String quizDescription = editQuizDescription.getText().toString().trim();
        
        if (quizName.isEmpty()) {
            editQuizName.setError("Quiz name is required");
            return false;
        }
        
        if (quizDescription.isEmpty()) {
            editQuizDescription.setError("Description is required");
            return false;
        }
        
        if (questionsList.isEmpty()) {
            Toast.makeText(this, "Add at least one question", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate each question
        for (int i = 0; i < questionsList.size(); i++) {
            Question question = questionsList.get(i);
            if (question.getTitle().trim().isEmpty()) {
                Toast.makeText(this, "Question " + (i + 1) + " text is empty", 
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (question.getType().equals("multiple_choice")) {
                // Check if the question has at least 2 answer options
                String description = question.getDescription();
                if (!description.contains("option:")) {
                    Toast.makeText(this, "Multiple choice question " + (i + 1) + 
                            " needs at least 2 options", Toast.LENGTH_SHORT).show();
                    return false;
                }
                
                // Count options
                int optionCount = 0;
                for (String part : description.split("\n")) {
                    if (part.startsWith("option:")) {
                        optionCount++;
                    }
                }
                
                if (optionCount < 2) {
                    Toast.makeText(this, "Multiple choice question " + (i + 1) + 
                            " needs at least 2 options", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            if (currentEditingQuestionPosition != -1) {
                Uri selectedImageUri = data.getData();
                uploadImage(selectedImageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        showLoading(true);
        
        String imageName = "question_images/" + UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(imageName);
        
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (currentEditingQuestionPosition != -1) {
                            Question question = questionsList.get(currentEditingQuestionPosition);
                            
                            // Add image info to question description
                            String currentDesc = question.getDescription();
                            String updatedDesc = currentDesc + "\nhas_image:true" + "\nimage_url:" + uri.toString();
                            question.setDescription(updatedDesc);
                            
                            questionAdapter.notifyItemChanged(currentEditingQuestionPosition);
                            showLoading(false);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    handleError(e);
                });
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
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (!editQuizName.getText().toString().isEmpty() || 
                !editQuizDescription.getText().toString().isEmpty() ||
                !questionsList.isEmpty()) {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes")
                    .setMessage("Are you sure you want to discard your changes?")
                    .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}