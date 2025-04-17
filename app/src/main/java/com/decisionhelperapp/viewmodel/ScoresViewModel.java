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
    private final MutableLiveData<Scores> selectedScore = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final DecisionRepository repository;

    public ScoresViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    public LiveData<List<Scores>> getScoresList() {
        return scoresList;
    }

    public LiveData<Scores> getSelectedScore() {
        return selectedScore;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setSelectedScore(Scores score) {
        selectedScore.setValue(score);
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

    public void getScoreById(String scoreId) {
        isLoading.setValue(true);
        repository.getScoreById(scoreId, new ScoresDAO.SingleScoreCallback() {
            @Override
            public void onCallback(Scores score) {
                selectedScore.setValue(score);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load score: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void addScore(Scores score) {
        isLoading.setValue(true);
        repository.addScore(score, new ScoresDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                // Reload the score list after adding
                loadScores();
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to add score: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void updateScore(Scores score) {
        isLoading.setValue(true);
        repository.updateScore(score, new ScoresDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                // Reload the score list after updating
                loadScores();
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to update score: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void deleteScore(String scoreId) {
        isLoading.setValue(true);
        repository.deleteScore(scoreId, new ScoresDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                // Reload the score list after deletion
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