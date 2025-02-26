package com.decisionhelperapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "decision_helper.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createQuestionsTable = "CREATE TABLE questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question_text TEXT, " +
                "quiz_name TEXT" +
                ");";
        db.execSQL(createQuestionsTable);

        String createResultsTable = "CREATE TABLE results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_name TEXT, " +
                "score INTEGER" +
                ");";
        db.execSQL(createResultsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS questions");
        db.execSQL("DROP TABLE IF EXISTS results");
        onCreate(db);
    }

    // Insert a new question into the 'questions' table.
    public long addQuestion(String questionText, String quizName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("question_text", questionText);
        values.put("quiz_name", quizName);
        long id = db.insert("questions", null, values);
        db.close();
        return id;
    }

    // Retrieve all questions for a given quiz.
    public Cursor getQuestionsForQuiz(String quizName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                "questions",       // Table name
                null,              // All columns
                "quiz_name = ?",   // Where clause
                new String[]{quizName}, // Where args
                null,              // groupBy
                null,              // having
                null               // orderBy
        );
    }

    // Insert a new result into the 'results' table.
    public long addResult(String quizName, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quiz_name", quizName);
        values.put("score", score);
        long id = db.insert("results", null, values);
        db.close();
        return id;
    }
}