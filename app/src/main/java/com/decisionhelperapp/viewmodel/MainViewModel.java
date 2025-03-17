package com.decisionhelperapp.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.repository.DecisionRepository;

// ViewModel for Main activity
public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";

    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<String> currentUserName = new MutableLiveData<>("Guest");
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    private DecisionRepository repository;

    public MainViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    public LiveData<User> getCurrentUserObject() {
        return currentUser;
    }

    public LiveData<String> getCurrentUser() {
        return currentUserName;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setCurrentUser(User user) {
        currentUser.setValue(user);
        if (user != null) {
            currentUserName.setValue(user.getName());
        } else {
            currentUserName.setValue("Guest");
        }
    }

    public void updateUser(String userName) {
        currentUserName.setValue(userName);
    }
    
    public void loadUserData(String userId) {
        isLoading.setValue(true);
        repository.getUserById(userId, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User user) {
                if (user != null) {
                    setCurrentUser(user);
                } else {
                    Log.e(TAG, "User with ID " + userId + " not found in database");
                    errorMessage.setValue("User not found");
                    currentUser.setValue(null);
                }
                isLoading.setValue(false);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to retrieve user data", e);
                errorMessage.setValue("Failed to load user data: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
}
