package com.decisionhelperapp.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.repository.DecisionRepository;

/**
 * ViewModel for Rating functionality.
 * Note: This is a placeholder since Rating functionality is no longer in development,
 * but we're maintaining the class for architecture consistency.
 */
public class RatingViewModel extends AndroidViewModel {

    private MutableLiveData<String> ratingStatus = new MutableLiveData<>("Rating functionality not available");
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    private DecisionRepository repository;

    public RatingViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    public LiveData<String> getRatingStatus() {
        return ratingStatus;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * This method is a placeholder as Rating functionality is no longer in development.
     */
    public void updateRatingStatus(String status) {
        ratingStatus.setValue(status);
    }
}
