package com.decisionhelperapp.database;

import com.decisionhelperapp.models.Quiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "Quiz";

    public QuizDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuizzes(final QuizCallback callback) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener(task -> {
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
    
    public void getQuizById(String quizId, final SingleQuizCallback callback) {
        db.collection(COLLECTION_NAME).document(quizId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Quiz quiz = document.toObject(Quiz.class);
                        callback.onCallback(quiz);
                    } else {
                        callback.onCallback(null);
                    }
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }
    
    public void addQuiz(Quiz quiz, final ActionCallback callback) {
        // If the quiz doesn't have an ID yet, let Firestore generate one
        if (quiz.getId() == null || quiz.getId().isEmpty()) {
            DocumentReference docRef = db.collection(COLLECTION_NAME).document();
            quiz.setId(docRef.getId());
        }
        
        db.collection(COLLECTION_NAME).document(quiz.getId())
            .set(quiz)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }
    
    public void updateQuiz(Quiz quiz, final ActionCallback callback) {
        db.collection(COLLECTION_NAME).document(quiz.getId())
            .set(quiz)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }
    
    public void deleteQuiz(String quizId, final ActionCallback callback) {
        db.collection(COLLECTION_NAME).document(quizId)
            .delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public interface QuizCallback {
        void onCallback(List<Quiz> quizList);
        void onFailure(Exception e);
    }
    
    public interface SingleQuizCallback {
        void onCallback(Quiz quiz);
        void onFailure(Exception e);
    }
    
    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}