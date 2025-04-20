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

    private final MutableLiveData<String> quizStatus = new MutableLiveData<>("Not started");
    private final MutableLiveData<Quiz> currentQuiz = new MutableLiveData<>();
    private final MutableLiveData<List<Quiz>> quizList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Question>> questionsList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Question> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    private final DecisionRepository repository;

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

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
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
}
