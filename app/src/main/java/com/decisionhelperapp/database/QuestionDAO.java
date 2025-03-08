package com.decisionhelperapp.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.QuizQuestions;

import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "questions";

    public QuestionDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuestions(final QuestionCallback callback) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
    
    public void getQuestionsByQuizId(String quizId, final QuestionListCallback callback) {
        db.collection("QuizQuestions")
            .document(quizId)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            QuizQuestions quizQuestions = document.toObject(QuizQuestions.class);
                            if (quizQuestions != null && quizQuestions.getQuestionsId() != null) {
                                List<String> questionIds = quizQuestions.getQuestionsId();
                                
                                if (questionIds.isEmpty()) {
                                    // No questions found for this quiz
                                    callback.onCallback(new ArrayList<>());
                                    return;
                                }
                                
                                // Now fetch the actual question objects
                                final List<Question> questions = new ArrayList<>();
                                final int[] completedQueries = {0};
                                
                                for (String questionId : questionIds) {
                                    db.collection(COLLECTION_NAME)
                                        .document(questionId)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                Question question = documentSnapshot.toObject(Question.class);
                                                question.setId(documentSnapshot.getId());
                                                questions.add(question);
                                            }
                                            
                                            completedQueries[0]++;
                                            if (completedQueries[0] == questionIds.size()) {
                                                callback.onCallback(questions);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            completedQueries[0]++;
                                            if (completedQueries[0] == questionIds.size()) {
                                                callback.onCallback(questions);
                                            }
                                        });
                                }
                            } else {
                                callback.onCallback(new ArrayList<>());
                            }
                        } else {
                            callback.onCallback(new ArrayList<>());
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                }
            });
    }

    public void insertQuestion(Question question) {
        db.collection(COLLECTION_NAME).add(question)
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
    
    public interface QuestionListCallback {
        void onCallback(List<Question> questionList);
        void onFailure(Exception e);
    }
}
