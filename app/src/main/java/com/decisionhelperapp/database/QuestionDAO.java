package com.decisionhelperapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.decisionhelperapp.models.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    private DatabaseHelper dbHelper;

    public QuestionDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Inserts a new Question into the database
    public long insertQuestion(Question question) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("question_text", question.getQuestionText());
        // Add other fields as necessary
        long id = db.insert("questions", null, values);
        db.close();
        return id;
    }

    // Retrieves a Question by its ID
    public Question getQuestionById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("questions", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Question question = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                question = new Question();
                question.setId(cursor.getLong(cursor.getColumnIndex("id")));
                question.setQuestionText(cursor.getString(cursor.getColumnIndex("question_text")));
                // Set additional fields as needed
            }
            cursor.close();
        }
        db.close();
        return question;
    }

    // Retrieves all Questions from the database
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM questions", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(cursor.getLong(cursor.getColumnIndex("id")));
                question.setQuestionText(cursor.getString(cursor.getColumnIndex("question_text")));
                // Set additional fields as needed
                questions.add(question);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return questions;
    }

    // Updates an existing Question
    public int updateQuestion(Question question) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("question_text", question.getQuestionText());
        // Add other fields to update as necessary
        int rows = db.update("questions", values, "id = ?", new String[]{String.valueOf(question.getId())});
        db.close();
        return rows;
    }

    // Deletes a Question by its ID
    public int deleteQuestion(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("questions", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
}
