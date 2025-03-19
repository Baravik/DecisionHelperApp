package com.decisionhelperapp.database;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.decisionhelperapp.models.Question;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "Question";

    public QuestionDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllQuestions(final QuestionCallback callback) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener(task -> {
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
        });
    }

    public void getQuestionById(String questionId, final SingleQuestionCallback callback) {
        db.collection(COLLECTION_NAME).document(questionId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Question question = document.toObject(Question.class);
                        callback.onCallback(question);
                    } else {
                        callback.onCallback(null);
                    }
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public void addQuestion(Question question, final ActionCallback callback) {
        // If the question doesn't have an ID yet, let Firestore generate one
        if (question.getId() == null || question.getId().isEmpty()) {
            DocumentReference docRef = db.collection(COLLECTION_NAME).document();
            question.setId(docRef.getId());
        }
        
        db.collection(COLLECTION_NAME).document(question.getId())
            .set(question)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public void updateQuestion(Question question, final ActionCallback callback) {
        db.collection(COLLECTION_NAME).document(question.getId())
            .set(question)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    /**
     * Updates multiple questions in a batch operation
     * @param questions List of questions to update
     * @param callback Callback to handle success or failure
     */
    public void updateQuestions(List<Question> questions, final ActionCallback callback) {
        if (questions == null || questions.isEmpty()) {
            callback.onSuccess(); // Nothing to update
            return;
        }

        // Create a batch operation
        WriteBatch batch = db.batch();
        
        // Add each question to the batch
        for (Question question : questions) {
            if (question.getId() != null && !question.getId().isEmpty()) {
                DocumentReference docRef = db.collection(COLLECTION_NAME).document(question.getId());
                batch.set(docRef, question);
            }
        }
        
        // Commit the batch
        batch.commit()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
    }

    public void deleteQuestion(String questionId, final ActionCallback callback) {
        db.collection(COLLECTION_NAME).document(questionId)
            .delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public interface QuestionCallback {
        void onCallback(List<Question> questionList);
        void onFailure(Exception e);
    }

    public interface SingleQuestionCallback {
        void onCallback(Question question);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
