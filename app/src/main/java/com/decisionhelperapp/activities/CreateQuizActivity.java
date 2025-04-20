package com.decisionhelperapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.adapters.QuestionAdapter;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.viewmodel.CreateQuizViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateQuizActivity extends BaseActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    
    private TextInputEditText editQuizName;
    private TextInputEditText editQuizDescription;
    private RecyclerView recyclerQuestions;
    private QuestionAdapter questionAdapter;
    private CircularProgressIndicator progressIndicator;
    private SwitchMaterial switchPublic;

    private FirebaseUser currentUser;
    private CreateQuizViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);
        
        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CreateQuizViewModel.class);
        
        // Set up toolbar
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
        
        // Set up observers
        setupObservers();
        
        // Set up questions recycler view
        setupQuestionAdapter();
        
        // Set up button listeners
        findViewById(R.id.btn_add_question).setOnClickListener(v -> viewModel.addNewQuestion());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveQuiz());

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!Objects.requireNonNull(editQuizName.getText()).toString().isEmpty() ||
                        !Objects.requireNonNull(editQuizDescription.getText()).toString().isEmpty() ||
                        (viewModel.getQuestionsList().getValue() != null && !viewModel.getQuestionsList().getValue().isEmpty())) {

                    new AlertDialog.Builder(CreateQuizActivity.this)
                            .setTitle("Discard Changes")
                            .setMessage("Are you sure you want to discard your changes?")
                            .setPositiveButton("Discard", (dialog, which) -> finish())
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    finish();
                }
            }
        });
    }
    
    private void setupObservers() {
        // Observe questions list
        viewModel.getQuestionsList().observe(this, questions -> {
            if (questionAdapter != null) {
                questionAdapter.updateQuestions(questions);
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> 
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        
        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe quiz saved state
        viewModel.getQuizSaved().observe(this, isSaved -> {
            if (isSaved) {
                Toast.makeText(this, R.string.quiz_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void setupQuestionAdapter() {
        // Create adapter with questions from ViewModel
        List<Question> questions = viewModel.getQuestionsList().getValue();
        if (questions == null) {
            questions = new ArrayList<>();
        }
        
        questionAdapter = new QuestionAdapter(this, questions, position -> viewModel.deleteQuestion(position));
        
        recyclerQuestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerQuestions.setAdapter(questionAdapter);
    }

    private void saveQuiz() {
        // Get data from views
        String quizName = Objects.requireNonNull(editQuizName.getText()).toString().trim();
        String quizDescription = Objects.requireNonNull(editQuizDescription.getText()).toString().trim();
        boolean isPublic = switchPublic.isChecked();
        
        // Save quiz using ViewModel
        viewModel.saveQuiz(
            quizDescription,
            quizName,
            currentUser.getUid(),
            isPublic
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            viewModel.uploadImage(selectedImageUri);
        }
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