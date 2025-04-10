package com.decisionhelperapp.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.QuestionDAO;
import com.decisionhelperapp.database.QuizDAO;
import com.decisionhelperapp.database.QuizQuestionsDAO;
import com.decisionhelperapp.models.Question;
import com.decisionhelperapp.models.Quiz;
import com.decisionhelperapp.models.QuizQuestions;
import com.decisionhelperapp.repository.DecisionRepository;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateQuizViewModel extends AndroidViewModel {
    private static final String TAG = "CreateQuizViewModel";

    private MutableLiveData<String> quizStatus = new MutableLiveData<>("Ready");
    private MutableLiveData<List<Question>> questionsList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Question> selectedQuestion = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> quizSaved = new MutableLiveData<>(false);
    private MutableLiveData<Quiz> createdQuiz = new MutableLiveData<>();
    private MutableLiveData<Integer> currentEditingQuestionPosition = new MutableLiveData<>(-1);
    
    private DecisionRepository repository;
    private StorageReference storageRef;
    private FirebaseFirestore db;

    public CreateQuizViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();
    }

    // LiveData getters
    public LiveData<String> getQuizStatus() {
        return quizStatus;
    }
    
    public LiveData<List<Question>> getQuestionsList() {
        return questionsList;
    }
    
    public LiveData<Question> getSelectedQuestion() {
        return selectedQuestion;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<Boolean> getQuizSaved() {
        return quizSaved;
    }
    
    public LiveData<Quiz> getCreatedQuiz() {
        return createdQuiz;
    }
    
    public LiveData<Integer> getCurrentEditingQuestionPosition() {
        return currentEditingQuestionPosition;
    }
    
    // Set current editing question
    public void setCurrentEditingQuestionPosition(int position) {
        currentEditingQuestionPosition.setValue(position);
    }
    
    // Add a new question
    public void addNewQuestion() {
        Question newQuestion = new Question();
        newQuestion.setId(UUID.randomUUID().toString());
        newQuestion.setTitle("");  // Will be filled in by user
        newQuestion.setType("multiple_choice");  // Default type
        
        // Add default options with equal percentages
        String defaultDesc = "option:Option 1\n" +
                "percentage:50\n" +
                "option:Option 2\n" +
                "percentage:50\n";
        newQuestion.setDescription(defaultDesc);
        
        newQuestion.setScore(0);  // Default score
        
        List<Question> currentList = questionsList.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(newQuestion);
        questionsList.setValue(currentList);
        quizStatus.setValue("Added new question");
    }
    
    // Delete a question
    public void deleteQuestion(int position) {
        List<Question> currentList = questionsList.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            questionsList.setValue(currentList);
            quizStatus.setValue("Question deleted");
        }
    }
    
    // Remove image from question
    public void removeImageFromQuestion(int position) {
        List<Question> currentList = questionsList.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            Question question = currentList.get(position);
            question.setDescription(question.getDescription().replace("has_image:true", ""));
            questionsList.setValue(currentList);
            quizStatus.setValue("Image removed");
        }
    }
    
    // Upload image for question
    public void uploadImage(Uri imageUri) {
        isLoading.setValue(true);
        
        String imageName = "question_images/" + UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(imageName);
        
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Integer position = currentEditingQuestionPosition.getValue();
                        if (position != null && position != -1) {
                            List<Question> currentList = questionsList.getValue();
                            if (currentList != null && position >= 0 && position < currentList.size()) {
                                Question question = currentList.get(position);
                                
                                // Parse the current description to keep all the options/percentages information
                                StringBuilder updatedDesc = new StringBuilder();
                                
                                // Add image info
                                updatedDesc.append("has_image:true\n");
                                updatedDesc.append("image_url:").append(uri.toString()).append("\n");
                                
                                // Preserve existing option and percentage data
                                String currentDesc = question.getDescription();
                                for (String line : currentDesc.split("\n")) {
                                    if (line.startsWith("option:") || line.startsWith("percentage:") || 
                                        line.startsWith("yes_full_score:")) {
                                        updatedDesc.append(line).append("\n");
                                    }
                                }
                                
                                question.setDescription(updatedDesc.toString().trim());
                                questionsList.setValue(currentList);
                                quizStatus.setValue("Image uploaded");
                            }
                        }
                        isLoading.setValue(false);
                    });
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to upload image: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }
    
    // Validate the quiz before saving or previewing
    public boolean validateQuiz(String quizName, String quizDescription) {
        if (quizName.isEmpty()) {
            errorMessage.setValue("Quiz name is required");
            return true;
        }
        
        if (quizDescription.isEmpty()) {
            errorMessage.setValue("Description is required");
            return true;
        }
        
        List<Question> currentList = questionsList.getValue();
        if (currentList == null || currentList.isEmpty()) {
            errorMessage.setValue("Add at least one question");
            return true;
        }
        
        // Validate each question
        for (int i = 0; i < currentList.size(); i++) {
            Question question = currentList.get(i);
            if (question.getTitle().trim().isEmpty()) {
                errorMessage.setValue("Question " + (i + 1) + " text is empty");
                return true;
            }
            
            if (question.getType().equals("multiple_choice")) {
                // Check if the question has at least 2 answer options
                String description = question.getDescription();
                if (!description.contains("option:")) {
                    errorMessage.setValue("Multiple choice question " + (i + 1) + 
                            " needs at least 2 options");
                    return true;
                }
                
                // Count options and check percentages
                int optionCount = 0;
                boolean hasPercentages = false;
                int totalPercentage = 0;
                
                for (String part : description.split("\n")) {
                    if (part.startsWith("option:")) {
                        optionCount++;
                    } else if (part.startsWith("percentage:")) {
                        hasPercentages = true;
                        try {
                            totalPercentage += Integer.parseInt(part.substring("percentage:".length()));
                        } catch (NumberFormatException e) {
                            // Ignore parsing errors
                        }
                    }
                }
                
                if (optionCount < 2) {
                    errorMessage.setValue("Multiple choice question " + (i + 1) + 
                            " needs at least 2 options");
                    return true;
                }
                
                // Check if percentages are defined properly
                if (!hasPercentages) {
                    errorMessage.setValue("Question " + (i + 1) + 
                            " is missing percentages for options");
                    return true;
                }
                
                // Check if total percentage isn't 0%
                if (totalPercentage == 0) {
                    errorMessage.setValue("Question " + (i + 1) + 
                            " needs at least one option with a non-zero percentage");
                    return true;
                }
            }
            else if (question.getType().equals("yes_no_question")) {
                // Check if the yes/no score is defined
                String description = question.getDescription();
                if (!description.contains("yes_full_score:")) {
                    errorMessage.setValue("Yes/No question " + (i + 1) + 
                            " needs to define which answer gives 100% score");
                    return true;
                }
            }
            else {
                // Invalid question type
                errorMessage.setValue("Question " + (i + 1) + 
                        " has an invalid type");
                return true;
            }
        }
        
        return false;
    }
    
    // Save the quiz
    public void saveQuiz(String description, String name, String userId, boolean isPublic) {
        if (validateQuiz(name, description)) {
            return;
        }

        isLoading.setValue(true);
        
        // Create quiz document
        String quizId = UUID.randomUUID().toString();
        Quiz quiz = new Quiz(
                quizId,
                description,
                name,
                userId,
                0,  // Default score
                "",  // Completed at will be empty for new quiz
                isPublic  // Include isPublic status directly in the Quiz object
        );
        
        // Get questions
        List<Question> currentList = questionsList.getValue();
        if (currentList == null) {
            errorMessage.setValue("No questions to save");
            isLoading.setValue(false);
            return;
        }
        
        // Save questions using repository
        List<String> questionIds = new ArrayList<>();
        int questionCount = currentList.size();
        final int[] savedCount = {0};
        
        for (Question question : currentList) {
            String questionId = question.getId();
            questionIds.add(questionId);
            
            repository.addQuestion(question, new QuestionDAO.ActionCallback() {
                @Override
                public void onSuccess() {
                    savedCount[0]++;
                    if (savedCount[0] == questionCount) {
                        // All questions saved, now save quiz
                        saveQuizAfterQuestions(quiz, questionIds, isPublic);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    errorMessage.setValue("Failed to save question: " + e.getMessage());
                    isLoading.setValue(false);
                }
            });
        }
    }
    
    private void saveQuizAfterQuestions(Quiz quiz, List<String> questionIds, boolean isPublic) {
        // Create QuizQuestions relationship
        QuizQuestions quizQuestions = new QuizQuestions(questionIds);
        
        // Save quiz - The isPublic flag is already part of the Quiz object
        repository.addQuiz(quiz, new QuizDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                // Save quiz questions relationship
                repository.addQuizQuestion(quizQuestions, quiz.getId(), new QuizQuestionsDAO.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        // Quiz is saved successfully with all its data
                        createdQuiz.setValue(quiz);
                        quizSaved.setValue(true);
                        isLoading.setValue(false);
                        quizStatus.setValue("Quiz saved successfully");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        errorMessage.setValue("Failed to link questions to quiz: " + e.getMessage());
                        isLoading.setValue(false);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to save quiz: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

}