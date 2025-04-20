package com.decisionhelperapp.database;

import static com.decisionhelperapp.database.DatabaseHelper.Table_Quizzes;

import com.decisionhelperapp.models.Quiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
    private final FirebaseFirestore db;

    public QuizDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuizzes(final QuizCallback callback) {
        db.collection(Table_Quizzes).get().addOnCompleteListener(task -> {
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
        db.collection(Table_Quizzes).document(quizId)
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
        // If the quiz doesn't have an ID yet, let Firebase generate one
        if (quiz.getId() == null || quiz.getId().isEmpty()) {
            DocumentReference docRef = db.collection(Table_Quizzes).document();
            quiz.setId(docRef.getId());
        }
        
        db.collection(Table_Quizzes).document(quiz.getId())
            .set(quiz)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public void deleteQuiz(String quizId, final ActionCallback quizDeletedSuccessfully) {
        db.collection(Table_Quizzes).document(quizId)
            .delete()
            .addOnSuccessListener(aVoid -> quizDeletedSuccessfully.onSuccess())
            .addOnFailureListener(quizDeletedSuccessfully::onFailure);
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