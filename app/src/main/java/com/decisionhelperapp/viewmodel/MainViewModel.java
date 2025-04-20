package com.decisionhelperapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.repository.DecisionRepository;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserName = new MutableLiveData<>("Guest");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final DecisionRepository repository;

    public MainViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    public LiveData<User> getCurrentUserObject() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoading() {
        return isLoading;
    }

    public void loadUserData(String userId) {
        Log.d(TAG, "🔄 Loading user data for ID: " + userId);
        isLoading.setValue(true);

        repository.getUserById(userId, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User user) {
                if (user != null) {
                    Log.d(TAG, "✅ User found: " + user.getEmail());
                    currentUser.postValue(user);
                    currentUserName.postValue(user.getName());
                } else {
                    Log.w(TAG, "❌ No user found for ID: " + userId);
                    errorMessage.postValue("User not found");
                    currentUser.postValue(null);
                    currentUserName.postValue("Guest");
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "🔥 Error retrieving user", e);
                errorMessage.postValue("Failed to load user data: " + e.getMessage());
                currentUser.postValue(null);
                currentUserName.postValue("Guest");
                isLoading.postValue(false);
            }
        });
    }
}