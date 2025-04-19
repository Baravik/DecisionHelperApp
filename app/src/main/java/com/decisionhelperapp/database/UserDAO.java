package com.decisionhelperapp.database;

import android.util.Log;

import com.decisionhelperapp.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String TAG = "UserDAO";
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "Users";

    public UserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // Retrieve all users in the collection
    public void getAllUsers(final UserCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            userList.add(user);
                        }
                        callback.onCallback(userList);
                    } else {
                        Log.e(TAG, "❌ Failed to get all users", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    // Retrieve a single user by Firestore document ID (must match Firebase UID)
    public void getUserById(String id, final SingleUserCallback callback) {
        Log.d(TAG, "🔍 Fetching user by ID: " + id);
        db.collection(COLLECTION_NAME).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        assert user != null;
                        Log.d(TAG, "✅ User found: " + user.getEmail());
                        callback.onCallback(user);
                    } else {
                        Log.w(TAG, "⚠️ User not found with ID: " + id);
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to fetch user by ID", e);
                    callback.onFailure(e);
                });
    }

    // Retrieve user by email (e.g. for Google Sign-In fallback)
    public void getUserByEmail(String email, final SingleUserCallback callback) {
        Log.d(TAG, "🔍 Fetching user by email: " + email);
        db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        User user = task.getResult().getDocuments().get(0).toObject(User.class);
                        assert user != null;
                        Log.d(TAG, "✅ User found by email: " + user.getEmail());
                        callback.onCallback(user);
                    } else if (task.isSuccessful()) {
                        Log.w(TAG, "⚠️ No user found with email: " + email);
                        callback.onCallback(null);
                    } else {
                        Log.e(TAG, "❌ Failed to fetch user by email", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    // Add user with predefined UID (Firebase UID must be used!)
    public void addUser(User user, final ActionCallback callback) {
        if (user.getId() == null || user.getId().isEmpty()) {
            Log.e(TAG, "❌ addUser() failed: user ID is null or empty. Aborting.");
            callback.onFailure(new IllegalArgumentException("User ID must not be null or empty"));
            return;
        }

        Log.d(TAG, "⬆️ Adding user with ID: " + user.getId());
        db.collection(COLLECTION_NAME)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User added successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to add user", e);
                    callback.onFailure(e);
                });
    }

    // Update an existing user document
    public void updateUser(User user, final ActionCallback callback) {
        if (user.getId() == null || user.getId().isEmpty()) {
            Log.e(TAG, "❌ updateUser() failed: missing ID");
            callback.onFailure(new IllegalArgumentException("User ID cannot be null or empty for update operation"));
            return;
        }

        Log.d(TAG, "✏️ Updating user with ID: " + user.getId());
        db.collection(COLLECTION_NAME)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User updated successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to update user", e);
                    callback.onFailure(e);
                });
    }

    // Delete user by ID
    public void deleteUser(String id, final ActionCallback callback) {
        Log.d(TAG, "🗑️ Deleting user with ID: " + id);
        db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User deleted");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to delete user", e);
                    callback.onFailure(e);
                });
    }

    // Callback interfaces
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