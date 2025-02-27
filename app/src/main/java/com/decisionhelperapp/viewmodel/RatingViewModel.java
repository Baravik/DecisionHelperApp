package com.decisionhelperapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ViewModel for Rating activity
public class RatingViewModel extends ViewModel {

    private MutableLiveData<String> ratingStatus = new MutableLiveData<>("Not rated");

    public LiveData<String> getRatingStatus() {
        return ratingStatus;
    }

    public void updateRatingStatus(String status) {
        ratingStatus.setValue(status);
    }
}
