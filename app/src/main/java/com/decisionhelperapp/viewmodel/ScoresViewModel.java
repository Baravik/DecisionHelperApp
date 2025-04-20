package com.decisionhelperapp.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.ScoresDAO;
import com.decisionhelperapp.models.Scores;
import com.decisionhelperapp.repository.DecisionRepository;
import java.util.List;

public class ScoresViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Scores>> scoresList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> actionStatus = new MutableLiveData<>();
    private final DecisionRepository repository;

    public ScoresViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    public LiveData<List<Scores>> getScoresList() {
        return scoresList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getActionStatus() {
        return actionStatus;
    }

    public void loadScores() {
        isLoading.setValue(true);
        repository.getAllScores(new ScoresDAO.ScoresCallback() {
            @Override
            public void onCallback(List<Scores> scores) {
                scoresList.setValue(scores);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load scores: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    // Delete a score from the database
    public void deleteScore(String scoreId) {
        if (scoreId == null || scoreId.isEmpty()) {
            errorMessage.setValue("Invalid score ID");
            return;
        }

        isLoading.setValue(true);
        repository.deleteScore(scoreId, new ScoresDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                actionStatus.setValue("Score deleted successfully");
                // Reload scores to refresh the list
                loadScores();
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to delete score: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
}