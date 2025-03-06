package com.decisionhelperapp.database;

import com.decisionhelperapp.models.Quiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
    private FirebaseFirestore db;

    public QuizDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuizzes(final QuizCallback callback) {
        db.collection("Quiz").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
            }
        });
    }

    public interface QuizCallback {
        void onCallback(List<Quiz> quizList);
        void onFailure(Exception e);
    }
}