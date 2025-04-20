package com.decisionhelperapp.database;

import static com.decisionhelperapp.database.DatabaseHelper.Table_Question;
import static com.decisionhelperapp.database.DatabaseHelper.Table_QuizQuestions;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.decisionhelperapp.models.QuizQuestions;
import com.decisionhelperapp.models.Question;
import java.util.ArrayList;
import java.util.List;

public class QuizQuestionsDAO {
    private final FirebaseFirestore db;

    public QuizQuestionsDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // Get questions for a specific quiz - simplified to use document ID
    public void getQuestionsByQuizId(String quizId, final QuizQuestionsCallback callback) {
        // Since we're now using quizId as the document ID, we can get it directly
        db.collection(Table_QuizQuestions)
            .document(quizId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                List<QuizQuestions> quizQuestionsList = new ArrayList<>();
                if (documentSnapshot.exists()) {
                    QuizQuestions quizQuestions = documentSnapshot.toObject(QuizQuestions.class);
                    if (quizQuestions != null) {
                        quizQuestionsList.add(quizQuestions);
                    }
                }
                callback.onCallback(quizQuestionsList);
            })
            .addOnFailureListener(callback::onFailure);
    }

    // Add a new quiz-questions relationship
    public void addQuizQuestions(QuizQuestions quizQuestions, String quizId, final ActionCallback callback) {
        if (quizId == null || quizId.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("QuizId cannot be null or empty"));
            return;
        }
        
        // Use the quizId as the document ID directly
        db.collection(Table_QuizQuestions)
            .document(quizId)
            .set(quizQuestions)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
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
                    questionIds = quizQuestion.getQuestionsId();
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
        
        // Firebase has limitations on 'in' queries, so for large lists we might need to batch
        db.collection(Table_Question)
            .whereIn("id", questionIds)
            .get()
            .addOnCompleteListener(task -> {
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
            });
    }
    
    // Added to match repository method signature
    public void addQuizQuestion(QuizQuestions quizQuestion, String quizID, final ActionCallback callback) {
        addQuizQuestions(quizQuestion, quizID, callback);
    }

    public void deleteQuizQuestions(String quizId, final ActionCallback quizDeletedSuccessfully) {
        db.collection(Table_QuizQuestions)
            .document(quizId)
            .delete()
            .addOnSuccessListener(aVoid -> quizDeletedSuccessfully.onSuccess())
            .addOnFailureListener(quizDeletedSuccessfully::onFailure);
    }

    public interface QuizQuestionsCallback {
        void onCallback(List<QuizQuestions> quizQuestionsList);
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