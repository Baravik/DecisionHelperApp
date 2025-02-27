package com.decisionhelperapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ViewModel for Admin activity
public class AdminViewModel extends ViewModel {

    private final MutableLiveData<String> status = new MutableLiveData<>("Ready");

    public LiveData<String> getStatus() {
        return status;
    }

    public void updateStatus(String newStatus) {
        status.setValue(newStatus);
    }
}
