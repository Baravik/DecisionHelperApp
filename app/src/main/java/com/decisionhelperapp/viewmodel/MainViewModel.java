package com.decisionhelperapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ViewModel for Main activity
public class MainViewModel extends ViewModel {

    private MutableLiveData<String> currentUser = new MutableLiveData<>("Guest");

    public LiveData<String> getCurrentUser() {
        return currentUser;
    }

    public void updateUser(String user) {
        currentUser.setValue(user);
    }
}
