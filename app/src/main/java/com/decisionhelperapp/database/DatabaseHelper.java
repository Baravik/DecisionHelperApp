package com.decisionhelperapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.decisionhelperapp.models.Quiz; // added import

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "decision_helper.db";
    protected static final String Table_Question = "questions";
    protected static final String Table_Quizzes = "quizzes";
    protected static final String Table_QuizQuestions = "quizQuestions";
    protected static final String Table_QuizUser = "quizUser";
    public static final String Table_QuizMetadata = "quizMetadata";



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

    // Insert a new question into the 'questions' table.
    public long addQuestion(String questionText, String quizName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("question_text", questionText);
        values.put("quiz_name", quizName);
        long id = db.insert(Table_Question, null, values);
        db.close();
        return id;
    }

    // Retrieve all questions for a given quiz.
    public Cursor getQuestionsForQuiz(String quizName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                Table_Question,       // Table name
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

    // Added getQuiz method to retrieve a Quiz by id
    public Quiz getQuiz(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(
                Table_Quizzes,
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