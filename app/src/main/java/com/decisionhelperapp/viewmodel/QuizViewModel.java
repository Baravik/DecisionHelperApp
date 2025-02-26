package com.decisionhelperapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ViewModel for Quiz activity
public class QuizViewModel extends ViewModel {

    private MutableLiveData<String> quizStatus = new MutableLiveData<>("Not started");

    public LiveData<String> getQuizStatus() {
        return quizStatus;
    }

    public void updateQuizStatus(String status) {
        quizStatus.setValue(status);
    }
}
