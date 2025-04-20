package com.decisionhelperapp.database;

import static com.decisionhelperapp.database.DatabaseHelper.Table_Question;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.decisionhelperapp.models.Question;

public class QuestionDAO {
    private final FirebaseFirestore db;

    public QuestionDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void addQuestion(Question question, final ActionCallback callback) {
        // If the question doesn't have an ID yet, let Firestore generate one
        if (question.getId() == null || question.getId().isEmpty()) {
            DocumentReference docRef = db.collection(Table_Question).document();
            question.setId(docRef.getId());
        }
        
        db.collection(Table_Question).document(question.getId())
            .set(question)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
