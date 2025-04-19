package com.decisionhelperapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "decision_helper.db";
    protected static final String Table_Question = "questions";
    protected static final String Table_Quizzes = "quizzes";
    protected static final String Table_QuizQuestions = "quizQuestions";
    protected static final String Table_QuizUser = "quizUser";



    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createQuestionsTable = "CREATE TABLE " + Table_Question +" (" +
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
        String createQuizTable = "CREATE TABLE "+Table_Quizzes+" (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_name TEXT" +
                ");";
        db.execSQL(createQuizTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Table_Question);
        db.execSQL("DROP TABLE IF EXISTS results");
        // Drop quiz table as well
        db.execSQL("DROP TABLE IF EXISTS " + Table_Quizzes);
        onCreate(db);
    }

}