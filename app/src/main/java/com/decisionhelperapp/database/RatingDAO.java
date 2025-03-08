package com.decisionhelperapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.decisionhelperapp.models.Rating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class RatingDAO {

    private FirebaseFirestore db;
    private DatabaseHelper dbHelper;
    private static final String COLLECTION_NAME = "Ratings";
    private boolean useLocalStorage;

    // Constructor for Firebase usage
    public RatingDAO() {
        db = FirebaseFirestore.getInstance();
        useLocalStorage = false;
    }

    // Constructor for local SQLite usage
    public RatingDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        useLocalStorage = true;
    }

    /**
     * Save a rating to storage (either Firestore or SQLite)
     */
    public void saveRating(Rating rating, final RatingCallback callback) {
        if (useLocalStorage) {
            saveRatingLocally(rating, callback);
        } else {
            saveRatingToFirestore(rating, callback);
        }
    }

    private void saveRatingLocally(Rating rating, final RatingCallback callback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", rating.getUserId());
        values.put("quiz_id", rating.getQuizId());
        values.put("score", rating.getScore());
        values.put("timestamp", rating.getTimestamp());

        try {
            long id = db.insert("ratings", null, values);
            if (id != -1) {
                if (callback != null) callback.onSuccess();
            } else {
                if (callback != null) callback.onFailure(new Exception("Failed to insert rating"));
            }
        } catch (Exception e) {
            if (callback != null) callback.onFailure(e);
        } finally {
            db.close();
        }
    }

    private void saveRatingToFirestore(Rating rating, final RatingCallback callback) {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("userId", rating.getUserId());
        ratingData.put("quizId", rating.getQuizId());
        ratingData.put("score", rating.getScore());
        ratingData.put("timestamp", rating.getTimestamp());

        db.collection(COLLECTION_NAME)
                .add(ratingData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    /**
     * Get all ratings for a specific user
     */
    public void getRatingsByUserId(String userId, final RatingListCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Rating> ratings = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Rating rating = document.toObject(Rating.class);
                                rating.setId(document.getId());
                                ratings.add(rating);
                            }
                            callback.onCallback(ratings);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Get all ratings for a specific quiz
     */
    public void getRatingsByQuizId(String quizId, final RatingListCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("quizId", quizId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Rating> ratings = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Rating rating = document.toObject(Rating.class);
                                rating.setId(document.getId());
                                ratings.add(rating);
                            }
                            callback.onCallback(ratings);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Get average rating score for a specific quiz
     */
    public void getAverageRatingForQuiz(String quizId, final AverageRatingCallback callback) {
        getRatingsByQuizId(quizId, new RatingListCallback() {
            @Override
            public void onCallback(List<Rating> ratings) {
                if (ratings.isEmpty()) {
                    callback.onCallback(0);
                    return;
                }

                int totalScore = 0;
                for (Rating rating : ratings) {
                    totalScore += rating.getScore();
                }
                double average = totalScore / (double) ratings.size();
                callback.onCallback(average);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Delete a rating by ID
     */
    public void deleteRating(String ratingId, final RatingCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(ratingId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public interface RatingCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface RatingListCallback {
        void onCallback(List<Rating> ratings);
        void onFailure(Exception e);
    }

    public interface AverageRatingCallback {
        void onCallback(double averageRating);
        void onFailure(Exception e);
    }
}