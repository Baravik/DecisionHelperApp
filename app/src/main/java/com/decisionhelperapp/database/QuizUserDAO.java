package com.decisionhelperapp.database;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.decisionhelperapp.models.QuizUser;
import com.decisionhelperapp.models.Quiz;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class QuizUserDAO {
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "QuizUser";

    public QuizUserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuizUsers(final QuizUserCallback callback) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<QuizUser> quizUserList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizUser quizUser = document.toObject(QuizUser.class);
                        // Ensure the ID is set
                        quizUser.setId(document.getId());
                        quizUserList.add(quizUser);
                    }
                    callback.onCallback(quizUserList);
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    // Get quiz attempts by user ID
    public void getQuizUsersByUserId(String userId, final QuizUserCallback callback) {
        db.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<QuizUser> quizUserList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizUser quizUser = document.toObject(QuizUser.class);
                        quizUser.setId(document.getId());
                        quizUserList.add(quizUser);
                    }
                    callback.onCallback(quizUserList);
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    // Get all attempts for a specific quiz
    public void getQuizUsersByQuizId(String quizId, final QuizUserCallback callback) {
        db.collection(COLLECTION_NAME)
            .whereEqualTo("quizId", quizId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<QuizUser> quizUserList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizUser quizUser = document.toObject(QuizUser.class);
                        quizUser.setId(document.getId());
                        quizUserList.add(quizUser);
                    }
                    callback.onCallback(quizUserList);
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    // Get a specific quiz attempt
    public void getQuizUserById(String id, final SingleQuizUserCallback callback) {
        db.collection(COLLECTION_NAME)
            .document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    QuizUser quizUser = documentSnapshot.toObject(QuizUser.class);
                    quizUser.setId(documentSnapshot.getId());
                    callback.onCallback(quizUser);
                } else {
                    callback.onCallback(null);
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Add a new quiz attempt
    public void addQuizUser(QuizUser quizUser, final ActionCallback callback) {
        // Let Firestore generate an ID if none is provided
        if (quizUser.getId() == null || quizUser.getId().isEmpty()) {
            db.collection(COLLECTION_NAME)
                .add(quizUser)
                .addOnSuccessListener(documentReference -> {
                    quizUser.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
        } else {
            // Use the provided ID
            db.collection(COLLECTION_NAME)
                .document(quizUser.getId())
                .set(quizUser)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
        }
    }

    // Update a quiz attempt
    public void updateQuizUser(QuizUser quizUser, final ActionCallback callback) {
        if (quizUser.getId() == null || quizUser.getId().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("QuizUser ID cannot be null or empty for update operation"));
            return;
        }

        db.collection(COLLECTION_NAME)
            .document(quizUser.getId())
            .set(quizUser)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Delete a quiz attempt
    public void deleteQuizUser(String id, final ActionCallback callback) {
        db.collection(COLLECTION_NAME)
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Get all quizzes taken by a user
    public void getQuizzesForUser(String userId, final QuizCallback callback) {
        getQuizUsersByUserId(userId, new QuizUserCallback() {
            @Override
            public void onCallback(List<QuizUser> quizUserList) {
                // Extract quiz IDs from quizUserList
                List<String> quizIds = new ArrayList<>();
                for (QuizUser quizUser : quizUserList) {
                    quizIds.add(quizUser.getQuizId());
                }
                
                if (quizIds.isEmpty()) {
                    callback.onCallback(new ArrayList<>());
                    return;
                }
                
                // Fetch quizzes by their IDs
                db.collection("Quiz")
                    .whereIn("id", quizIds)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public interface QuizUserCallback {
        void onCallback(List<QuizUser> quizUserList);
        void onFailure(Exception e);
    }

    public interface SingleQuizUserCallback {
        void onCallback(QuizUser quizUser);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface QuizCallback {
        void onCallback(List<Quiz> quizList);
        void onFailure(Exception e);
    }
}