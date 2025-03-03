package com.decisionhelperapp.database;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.decisionhelperapp.models.Question;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;

public class QuestionDAO {
    private FirebaseFirestore db;

    public QuestionDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuestions(final QuestionCallback callback) {
        db.collection("Question").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Question> questionList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Question question = document.toObject(Question.class);
                        questionList.add(question);
                    }
                    callback.onCallback(questionList);
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    public void insertQuestion(Question question) {
        db.collection("Question").add(question)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    // Document added successfully. Optionally handle the documentReference.
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle the error case
                }
            });
    }

    public interface QuestionCallback {
        void onCallback(List<Question> questionList);
        void onFailure(Exception e);
    }
}
