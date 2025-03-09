package com.decisionhelperapp.database;

import static com.decisionhelperapp.database.DatabaseHelper.COLLECTION_QUIZZES;

import com.decisionhelperapp.models.Quiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
    private final FirebaseFirestore db;

    public QuizDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuizzes(final QuizCallback callback) {
        db.collection(COLLECTION_QUIZZES).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Quiz> quizList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Quiz quiz = document.toObject(Quiz.class);
                    quizList.add(quiz);
                }
                callback.onCallback(quizList);
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    public interface QuizCallback {
        void onCallback(List<Quiz> quizList);
        void onFailure(Exception e);
    }
}