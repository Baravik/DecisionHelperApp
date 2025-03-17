package com.decisionhelperapp.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.repository.DecisionRepository;

import java.util.ArrayList;
import java.util.List;

// Enhanced ViewModel for Admin activity
public class AdminViewModel extends AndroidViewModel {

    private MutableLiveData<String> status = new MutableLiveData<>("Ready");
    private MutableLiveData<List<User>> userList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Quiz>> quizList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    private DecisionRepository repository;

    public AdminViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    // LiveData getters
    public LiveData<String> getStatus() {
        return status;
    }

    public LiveData<List<User>> getUserList() {
        return userList;
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

    // Status update
    public void updateStatus(String newStatus) {
        status.setValue(newStatus);
    }

    // User management methods
    public void loadAllUsers() {
        isLoading.setValue(true);
        repository.getAllUsers(new UserDAO.UserCallback() {
            @Override
            public void onCallback(List<User> users) {
                userList.setValue(users);
                status.setValue("Users loaded: " + users.size());
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load users: " + e.getMessage());
                status.setValue("Error loading users");
                isLoading.setValue(false);
            }
        });
    }

    public void deleteUser(String userId) {
        isLoading.setValue(true);
        repository.deleteUser(userId, new UserDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                status.setValue("User deleted successfully");
                loadAllUsers(); // Refresh the list
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to delete user: " + e.getMessage());
                status.setValue("Error deleting user");
                isLoading.setValue(false);
            }
        });
    }

    // Quiz management methods
    public void loadAllQuizzes() {
        isLoading.setValue(true);
        repository.getAllQuizzes(new QuizDAO.QuizCallback() {
            @Override
            public void onCallback(List<Quiz> quizzes) {
                quizList.setValue(quizzes);
                status.setValue("Quizzes loaded: " + quizzes.size());
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load quizzes: " + e.getMessage());
                status.setValue("Error loading quizzes");
                isLoading.setValue(false);
            }
        });
    }

    public void deleteQuiz(String quizId) {
        isLoading.setValue(true);
        repository.deleteQuiz(quizId, new QuizDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                status.setValue("Quiz deleted successfully");
                loadAllQuizzes(); // Refresh the list
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to delete quiz: " + e.getMessage());
                status.setValue("Error deleting quiz");
                isLoading.setValue(false);
            }
        });
    }

    // Combined dashboard data methods
    public void loadDashboardData() {
        isLoading.setValue(true);
        status.setValue("Loading dashboard data...");
        
        // Load users and quizzes in parallel
        loadAllUsers();
        loadAllQuizzes();
    }
}
