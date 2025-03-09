package com.decisionhelperapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.decisionhelperapp.models.Quiz; // added import

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "decision_helper.db";
    public static final String COLLECTION_QUIZ_QUESTIONS = "quizQuestions";
    public static final String COLLECTION_QUESTIONS = "questions";
    public static final String COLLECTION_QUIZ_METADATA = "quizMetadata";
    public static final String COLLECTION_QUIZZES = "quizzes";
    public static final String COLLECTION_USERS = "Users";

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

        // Added quiz table creation
        String createQuizTable = "CREATE TABLE quiz (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_name TEXT" +
                ");";
        db.execSQL(createQuizTable);

        // Add ratings table creation
        String createRatingsTable = "CREATE TABLE ratings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id TEXT, " +
                "quiz_id TEXT, " +
                "score INTEGER, " +
                "timestamp INTEGER" +
                ");";
        db.execSQL(createRatingsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS questions");
        db.execSQL("DROP TABLE IF EXISTS results");
        // Drop quiz table as well
        db.execSQL("DROP TABLE IF EXISTS quiz");
        db.execSQL("DROP TABLE IF EXISTS ratings");
        onCreate(db);
    }

    // Added getQuiz method to retrieve a Quiz by id
    public Quiz getQuiz(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(
                "quiz",
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                Quiz quiz = new Quiz();
                quiz.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))));
                quiz.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("quiz_name")));
                return quiz;
            }
        }
        return null;
    }

    // Added updateQuiz method to update a Quiz
    public void updateQuiz(Quiz quiz) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Changed quiz.getName() to quiz.getQuizName() because there is no setName/getName
        values.put("quiz_name", quiz.getDescription());
        db.update("quiz", values, "id = ?", new String[]{String.valueOf(quiz.getId())});
        db.close();
    }
}