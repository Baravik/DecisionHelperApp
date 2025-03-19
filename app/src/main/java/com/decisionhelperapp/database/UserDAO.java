package com.decisionhelperapp.database;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.decisionhelperapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "Users";

    public UserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllUsers(final UserCallback callback) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<User> userList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    userList.add(user);
                }
                callback.onCallback(userList);
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    public void getUserById(String id, final SingleUserCallback callback) {
        db.collection(COLLECTION_NAME).document(id).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    callback.onCallback(user);
                } else {
                    callback.onCallback(null);
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    public void getUserByEmail(String email, final SingleUserCallback callback) {
        db.collection(COLLECTION_NAME)
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    User user = task.getResult().getDocuments().get(0).toObject(User.class);
                    callback.onCallback(user);
                } else if (task.isSuccessful()) {
                    // No user found with that email
                    callback.onCallback(null);
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public void addUser(User user, final ActionCallback callback) {
        if (user.getId() == null || user.getId().isEmpty()) {
            // Let Firestore generate an ID
            db.collection(COLLECTION_NAME)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    user.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
        } else {
            // Use the provided ID
            db.collection(COLLECTION_NAME)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
        }
    }

    public void updateUser(User user, final ActionCallback callback) {
        if (user.getId() == null || user.getId().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("User ID cannot be null or empty for update operation"));
            return;
        }

        db.collection(COLLECTION_NAME)
            .document(user.getId())
            .set(user)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
    }

    public void deleteUser(String id, final ActionCallback callback) {
        db.collection(COLLECTION_NAME)
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
    }

    public interface UserCallback {
        void onCallback(List<User> userList);
        void onFailure(Exception e);
    }

    public interface SingleUserCallback {
        void onCallback(User user);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}