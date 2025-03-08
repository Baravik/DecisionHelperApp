package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.database.QuestionDAO;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.Question.Answer;
import com.decisionhelperapp.models.Rating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionActivity extends AppCompatActivity {

    private TextView questionTitleText;
    private TextView questionDescriptionText;
    private RadioGroup answersRadioGroup;
    private SeekBar importanceSeekBar;
    private TextView importanceValueText;
    private Button nextButton;
    private TextView questionCounterText;
    
    private String quizId;
    private String quizTitle;
    private String userId;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private Map<String, Answer> selectedAnswers = new HashMap<>();
    private Map<String, Integer> importanceRatings = new HashMap<>();
    private Map<String, Integer> calculatedScores = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // Get quiz ID and user ID from intent
        quizId = getIntent().getStringExtra("QUIZ_ID");
        quizTitle = getIntent().getStringExtra("QUIZ_TITLE");
        userId = getIntent().getStringExtra("USER_ID");

        if (quizId == null) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        questionTitleText = findViewById(R.id.question_title);
        questionDescriptionText = findViewById(R.id.question_description);
        answersRadioGroup = findViewById(R.id.answers_radio_group);
        importanceSeekBar = findViewById(R.id.importance_seekbar);
        importanceValueText = findViewById(R.id.importance_value);
        nextButton = findViewById(R.id.next_button);
        questionCounterText = findViewById(R.id.question_counter);

        // Set title to match quiz title
        setTitle(quizTitle);

        // Set up importance seekbar listener
        importanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                importanceValueText.setText(String.valueOf(progress + 1));  // Display 1-5 instead of 0-4
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Set up next button listener
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (answersRadioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(QuestionActivity.this, R.string.please_select_answer, Toast.LENGTH_SHORT).show();
                    return;
                }

                saveCurrentQuestionResponses();

                if (currentQuestionIndex < questions.size() - 1) {
                    // More questions to answer
                    currentQuestionIndex++;
                    displayQuestion();
                } else {
                    // End of questionnaire - calculate final score
                    calculateFinalScore();
                }
            }
        });

        // Load questions for this quiz
        loadQuestions();
    }

    private void loadQuestions() {
        QuestionDAO questionDAO = new QuestionDAO();
        questionDAO.getQuestionsByQuizId(quizId, new QuestionDAO.QuestionListCallback() {
            @Override
            public void onCallback(List<Question> questionList) {
                if (questionList != null && !questionList.isEmpty()) {
                    questions = questionList;
                    displayQuestion();
                } else {
                    Toast.makeText(QuestionActivity.this, 
                            R.string.no_questionnaires, 
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(QuestionActivity.this, 
                        getString(R.string.error), 
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayQuestion() {
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // Update counter text
        questionCounterText.setText(getString(R.string.question_progress, currentQuestionIndex + 1, questions.size()));
        
        // Set question title and description
        questionTitleText.setText(currentQuestion.getTitle());
        questionDescriptionText.setText(currentQuestion.getDescription());
        
        // Make sure answers are loaded from the description
        currentQuestion.loadAnswersFromDescription();
        
        // Clear previous radio buttons
        answersRadioGroup.removeAllViews();
        
        // Add radio button for each answer
        List<Answer> answers = currentQuestion.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            Answer answer = answers.get(i);
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(answer.getText());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(i); // Store the index of the answer
            answersRadioGroup.addView(radioButton);
            
            // Check if this answer was previously selected
            if (selectedAnswers.containsKey(currentQuestion.getId()) && 
                selectedAnswers.get(currentQuestion.getId()).getText().equals(answer.getText())) {
                radioButton.setChecked(true);
            }
        }
        
        // Set importance rating if previously set
        if (importanceRatings.containsKey(currentQuestion.getId())) {
            int rating = importanceRatings.get(currentQuestion.getId());
            importanceSeekBar.setProgress(rating - 1); // Convert back from 1-5 to 0-4
        } else {
            importanceSeekBar.setProgress(2); // Default to 3 (middle value)
        }
        
        // Update button text for last question
        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setText(R.string.finish);
        } else {
            nextButton.setText(R.string.next);
        }
    }

    private void saveCurrentQuestionResponses() {
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // Save selected answer
        int selectedId = answersRadioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            int answerIndex = (int) selectedRadioButton.getTag();
            Answer selectedAnswer = currentQuestion.getAnswers().get(answerIndex);
            selectedAnswers.put(currentQuestion.getId(), selectedAnswer);
            
            // Calculate partial score for this question
            int score = (selectedAnswer.getPercentage() * currentQuestion.getScore()) / 100;
            calculatedScores.put(currentQuestion.getId(), score);
        }
        
        // Save importance rating (1-5)
        int importanceRating = importanceSeekBar.getProgress() + 1;
        importanceRatings.put(currentQuestion.getId(), importanceRating);
    }

    private void calculateFinalScore() {
        int totalWeightedScore = 0;
        int totalWeight = 0;
        
        for (String questionId : calculatedScores.keySet()) {
            int score = calculatedScores.get(questionId);
            int weight = importanceRatings.get(questionId);
            
            totalWeightedScore += (score * weight);
            totalWeight += weight;
        }
        
        int finalScore = totalWeight > 0 ? (totalWeightedScore / totalWeight) : 0;
        
        // Save the rating to the database
        Rating rating = new Rating(userId, quizId, finalScore);
        
        // Navigate to results screen
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SCORE", finalScore);
        intent.putExtra("QUIZ_TITLE", quizTitle);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("RATING", rating);
        startActivity(intent);
        finish();
    }
}