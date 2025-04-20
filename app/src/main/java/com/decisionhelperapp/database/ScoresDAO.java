package com.decisionhelperapp.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.decisionhelperapp.models.Scores;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScoresDAO {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "userQuizScores";

    public ScoresDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllScores(final ScoresCallback callback) {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Scores> scoresList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Scores score = document.toObject(Scores.class);
                            // Crucial fix: Set the document ID to the score object
                            score.setId(document.getId());
                            scoresList.add(score);
                        }
                        callback.onCallback(scoresList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void deleteScore(String scoreId, final ActionCallback scoreDeletedSuccessfully) {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Log the score ID for debugging
        System.out.println("Attempting to delete score with ID: " + scoreId);
        
        db.collection(COLLECTION_NAME)
                .document(scoreId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Score successfully deleted!");
                    scoreDeletedSuccessfully.onSuccess();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error deleting score: " + e.getMessage());
                    scoreDeletedSuccessfully.onFailure(e);
                });
    }

    public interface ScoresCallback {
        void onCallback(List<Scores> scoresList);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}