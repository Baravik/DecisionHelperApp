package com.decisionhelperapp.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.database.QuizQuestionsDAO;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.repository.DecisionRepository;

import java.util.ArrayList;
import java.util.List;

// Enhanced ViewModel for Quiz activity following MVVM pattern
public class QuizViewModel extends AndroidViewModel {

    private MutableLiveData<String> quizStatus = new MutableLiveData<>("Not started");
    private MutableLiveData<Quiz> currentQuiz = new MutableLiveData<>();
    private MutableLiveData<List<Quiz>> quizList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Question>> questionsList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Question> currentQuestion = new MutableLiveData<>();
    private MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    private DecisionRepository repository;

    public QuizViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    // LiveData getters
    public LiveData<String> getQuizStatus() {
        return quizStatus;
    }
    
    public LiveData<Quiz> getCurrentQuiz() {
        return currentQuiz;
    }
    
    public LiveData<List<Quiz>> getQuizList() {
        return quizList;
    }
    
    public LiveData<List<Question>> getQuestionsList() {
        return questionsList;
    }
    
    public LiveData<Question> getCurrentQuestion() {
        return currentQuestion;
    }
    
    public LiveData<Integer> getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    // Update quiz status
    public void updateQuizStatus(String status) {
        quizStatus.setValue(status);
    }
    
    // Load all quizzes
    public void loadAllQuizzes() {
        isLoading.setValue(true);
        repository.getAllQuizzes(new QuizDAO.QuizCallback() {
            @Override
            public void onCallback(List<Quiz> quizzes) {
                quizList.setValue(quizzes);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load quizzes: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
    
    // Load quiz by ID
    public void loadQuizById(String quizId) {
        isLoading.setValue(true);
        repository.getQuizById(quizId, new QuizDAO.SingleQuizCallback() {
            @Override
            public void onCallback(Quiz quiz) {
                currentQuiz.setValue(quiz);
                quizStatus.setValue("Quiz loaded");
                
                // After loading the quiz, load its questions
                loadQuestionsForQuiz(quizId);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load quiz: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
    
    // Load questions for a quiz
    public void loadQuestionsForQuiz(String quizId) {
        isLoading.setValue(true);
        repository.getQuestionsForQuiz(quizId, new QuizQuestionsDAO.QuestionsCallback() {
            @Override
            public void onCallback(List<Question> questions) {
                questionsList.setValue(questions);
                if (questions != null && !questions.isEmpty()) {
                    currentQuestion.setValue(questions.get(0));
                    currentQuestionIndex.setValue(0);
                    quizStatus.setValue("Ready to start");
                } else {
                    quizStatus.setValue("No questions found");
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load questions: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
    
    // Add a new quiz
    public void addQuiz(Quiz quiz) {
        isLoading.setValue(true);
        repository.addQuiz(quiz, new QuizDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                quizStatus.setValue("Quiz added successfully");
                currentQuiz.setValue(quiz);
                loadAllQuizzes(); // Refresh the quiz list
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to add quiz: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
    
    // Update a quiz
    public void updateQuiz(Quiz quiz) {
        isLoading.setValue(true);
        repository.updateQuiz(quiz, new QuizDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                quizStatus.setValue("Quiz updated successfully");
                currentQuiz.setValue(quiz);
                loadAllQuizzes(); // Refresh the quiz list
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to update quiz: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
    
    // Delete a quiz
    public void deleteQuiz(String quizId) {
        isLoading.setValue(true);
        repository.deleteQuiz(quizId, new QuizDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                quizStatus.setValue("Quiz deleted successfully");
                currentQuiz.setValue(null);
                loadAllQuizzes(); // Refresh the quiz list
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to delete quiz: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    // Move to next question
    public void nextQuestion() {
        List<Question> questions = questionsList.getValue();
        Integer currentIndex = currentQuestionIndex.getValue();
        
        if (questions == null || currentIndex == null || questions.isEmpty()) {
            return;
        }
        
        int nextIndex = currentIndex + 1;
        if (nextIndex < questions.size()) {
            currentQuestionIndex.setValue(nextIndex);
            currentQuestion.setValue(questions.get(nextIndex));
        }
    }
    
    // Move to previous question
    public void previousQuestion() {
        List<Question> questions = questionsList.getValue();
        Integer currentIndex = currentQuestionIndex.getValue();
        
        if (questions == null || currentIndex == null || questions.isEmpty()) {
            return;
        }
        
        int prevIndex = currentIndex - 1;
        if (prevIndex >= 0) {
            currentQuestionIndex.setValue(prevIndex);
            currentQuestion.setValue(questions.get(prevIndex));
        }
    }
}
