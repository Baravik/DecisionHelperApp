package com.decisionhelperapp.database;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.decisionhelperapp.models.QuizQuestions;
import com.decisionhelperapp.models.Question;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class QuizQuestionsDAO {
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "QuizQuestions";

    public QuizQuestionsDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuizQuestions(final QuizQuestionsCallback callback) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<QuizQuestions> quizQuestionsList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizQuestions quizQuestions = document.toObject(QuizQuestions.class);
                        quizQuestions.setId(document.getId());
                        quizQuestionsList.add(quizQuestions);
                    }
                    callback.onCallback(quizQuestionsList);
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    // Get questions for a specific quiz
    public void getQuestionsByQuizId(String quizId, final QuizQuestionsCallback callback) {
        db.collection(COLLECTION_NAME)
            .whereEqualTo("quizId", quizId)
            .orderBy("order") // Order by the order field
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<QuizQuestions> quizQuestionsList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizQuestions quizQuestions = document.toObject(QuizQuestions.class);
                        quizQuestions.setId(document.getId());
                        quizQuestionsList.add(quizQuestions);
                    }
                    callback.onCallback(quizQuestionsList);
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    // Get a specific quiz question relationship
    public void getQuizQuestionsById(String id, final SingleQuizQuestionsCallback callback) {
        db.collection(COLLECTION_NAME)
            .document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    QuizQuestions quizQuestions = documentSnapshot.toObject(QuizQuestions.class);
                    quizQuestions.setId(documentSnapshot.getId());
                    callback.onCallback(quizQuestions);
                } else {
                    callback.onCallback(null);
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Add a new quiz-questions relationship
    public void addQuizQuestions(QuizQuestions quizQuestions, final ActionCallback callback) {
        // Let Firestore generate an ID if none is provided
        if (quizQuestions.getId() == null || quizQuestions.getId().isEmpty()) {
            db.collection(COLLECTION_NAME)
                .add(quizQuestions)
                .addOnSuccessListener(documentReference -> {
                    quizQuestions.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e));
        } else {
            // Use the provided ID
            db.collection(COLLECTION_NAME)
                .document(quizQuestions.getId())
                .set(quizQuestions)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
        }
    }

    // Update a quiz-questions relationship
    public void updateQuizQuestions(QuizQuestions quizQuestions, final ActionCallback callback) {
        if (quizQuestions.getId() == null || quizQuestions.getId().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("QuizQuestions ID cannot be null or empty for update operation"));
            return;
        }

        db.collection(COLLECTION_NAME)
            .document(quizQuestions.getId())
            .set(quizQuestions)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Delete a quiz-questions relationship
    public void deleteQuizQuestions(String id, final ActionCallback callback) {
        db.collection(COLLECTION_NAME)
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Added to match repository method signature
    public void getQuestionsForQuiz(String quizId, final QuestionsCallback callback) {
        getQuestionsByQuizId(quizId, new QuizQuestionsCallback() {
            @Override
            public void onCallback(List<QuizQuestions> quizQuestionsList) {
                // This would typically involve a separate query to fetch questions
                // based on the question IDs in the quizQuestionsList
                List<String> questionIds = new ArrayList<>();
                for (QuizQuestions quizQuestion : quizQuestionsList) {
                    questionIds.add(quizQuestion.getQuestionId());
                }
                
                // Now fetch all those questions and return them
                fetchQuestionsByIds(questionIds, callback);
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    
    // Helper method to fetch questions by IDs
    private void fetchQuestionsByIds(List<String> questionIds, final QuestionsCallback callback) {
        if (questionIds.isEmpty()) {
            callback.onCallback(new ArrayList<>());
            return;
        }
        
        // Firestore has limitations on 'in' queries, so for large lists we might need to batch
        db.collection("Question")
            .whereIn("id", questionIds)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Question> questions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Question question = document.toObject(Question.class);
                            questions.add(question);
                        }
                        callback.onCallback(questions);
                    } else {
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    // Added to match repository method signature
    public void addQuizQuestion(QuizQuestions quizQuestion, final ActionCallback callback) {
        addQuizQuestions(quizQuestion, callback);
    }

    public interface QuizQuestionsCallback {
        void onCallback(List<QuizQuestions> quizQuestionsList);
        void onFailure(Exception e);
    }

    public interface SingleQuizQuestionsCallback {
        void onCallback(QuizQuestions quizQuestions);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface QuestionsCallback {
        void onCallback(List<Question> questions);
        void onFailure(Exception e);
    }
}