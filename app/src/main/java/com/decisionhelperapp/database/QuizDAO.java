import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

package com.decisionhelperapp.database;



// Data Access Object for Quiz entity
@Dao
public interface QuizDAO {

    // Insert a new Quiz record and return its new rowId
    @Insert
    long insertQuiz(Quiz quiz);

    // Update an existing Quiz record and return number of rows updated
    @Update
    int updateQuiz(Quiz quiz);

    // Delete a Quiz record and return number of rows deleted
    @Delete
    int deleteQuiz(Quiz quiz);

    // Retrieve all Quiz records from the Quiz table
    @Query("SELECT * FROM Quiz")
    List<Quiz> getAllQuizzes();

    // Find a specific Quiz record by its id
    @Query("SELECT * FROM Quiz WHERE id = :id")
    Quiz getQuizById(int id);
}