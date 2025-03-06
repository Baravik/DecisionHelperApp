package com.decisionhelperapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.decisionhelperapp.models.User;

// ViewModel for Main activity
public class MainViewModel extends ViewModel {

    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<String> currentUserName = new MutableLiveData<>("Guest");

    public LiveData<User> getCurrentUserObject() {
        return currentUser;
    }

    public LiveData<String> getCurrentUser() {
        return currentUserName;
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
}
