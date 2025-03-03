package com.decisionhelperapp.database;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.decisionhelperapp.models.Scores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class ScoresDAO {
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "Scores";

    public ScoresDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllScores(final ScoresCallback callback) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Scores> scoresList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Scores scores = document.toObject(Scores.class);
                        scoresList.add(scores);
                    }
                    callback.onCallback(scoresList);
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    public void getScoreById(String id, final SingleScoreCallback callback) {
        db.collection(COLLECTION_NAME).document(id).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Scores score = documentSnapshot.toObject(Scores.class);
                    callback.onCallback(score);
                } else {
                    callback.onCallback(null);
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void addScore(Scores score, final ActionCallback callback) {
        // If the ID is not set, let Firestore generate it
        if (score.getId() == null || score.getId().isEmpty()) {
            db.collection(COLLECTION_NAME)
                .add(score)
                .addOnSuccessListener(documentReference -> {
                    // Update the score object with the generated ID
                    score.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
        } else {
            // If ID is provided, use it as the document ID
            db.collection(COLLECTION_NAME)
                .document(score.getId())
                .set(score)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
        }
    }

    public void updateScore(Scores score, final ActionCallback callback) {
        if (score.getId() == null || score.getId().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Score ID cannot be null or empty for update operation"));
            return;
        }

        db.collection(COLLECTION_NAME)
            .document(score.getId())
            .set(score)  // Using set instead of update to completely replace the document
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteScore(String id, final ActionCallback callback) {
        db.collection(COLLECTION_NAME)
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    public interface ScoresCallback {
        void onCallback(List<Scores> scoresList);
        void onFailure(Exception e);
    }

    public interface SingleScoreCallback {
        void onCallback(Scores score);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}