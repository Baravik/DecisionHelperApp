package com.decisionhelperapp.database;

import static com.decisionhelperapp.database.DatabaseHelper.Table_QuizUser;
import static com.decisionhelperapp.database.DatabaseHelper.Table_Quizzes;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.decisionhelperapp.models.QuizUser;
import com.decisionhelperapp.models.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuizUserDAO {
    private final FirebaseFirestore db;

    public QuizUserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuizUsers(final QuizUserCallback callback) {
        db.collection(Table_QuizUser).get().addOnCompleteListener(task -> {
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
        });
    }

    // Get quiz attempts by user ID
    public void getQuizUsersByUserId(String userId, final QuizUserCallback callback) {
        db.collection(Table_QuizUser)
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
        db.collection(Table_QuizUser)
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
        db.collection(Table_QuizUser)
            .document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    QuizUser quizUser = documentSnapshot.toObject(QuizUser.class);
                    Objects.requireNonNull(quizUser).setId(documentSnapshot.getId());
                    callback.onCallback(quizUser);
                } else {
                    callback.onCallback(null);
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    // Add a new quiz attempt
    public void addQuizUser(QuizUser quizUser, final ActionCallback callback) {
        // Let Firebase generate an ID if none is provided
        if (quizUser.getId() == null || quizUser.getId().isEmpty()) {
            db.collection(Table_QuizUser)
                .add(quizUser)
                .addOnSuccessListener(documentReference -> {
                    quizUser.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
        } else {
            // Use the provided ID
            db.collection(Table_QuizUser)
                .document(quizUser.getId())
                .set(quizUser)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
        }
    }

    // Update a quiz attempt
    public void updateQuizUser(QuizUser quizUser, final ActionCallback callback) {
        if (quizUser.getId() == null || quizUser.getId().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("QuizUser ID cannot be null or empty for update operation"));
            return;
        }

        db.collection(Table_QuizUser)
            .document(quizUser.getId())
            .set(quizUser)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
    }

    // Delete a quiz attempt
    public void deleteQuizUser(String id, final ActionCallback callback) {
        db.collection(Table_QuizUser)
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
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
                db.collection(Table_Quizzes)
                    .whereIn("id", quizIds)
                    .get()
                    .addOnCompleteListener(task -> {
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