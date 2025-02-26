import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

package com.decisionhelperapp.database;


public class RatingDAO {

    private static final String TABLE_RATING = "rating";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_COMMENT = "comment";

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public RatingDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Insert a new rating into the database.
    public long insertRating(Rating rating) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, rating.getValue());
        values.put(COLUMN_COMMENT, rating.getComment());
        return db.insert(TABLE_RATING, null, values);
    }

    // Retrieve all ratings from the database.
    public List<Rating> getAllRatings() {
        List<Rating> ratings = new ArrayList<>();
        Cursor cursor = db.query(TABLE_RATING, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Rating rating = new Rating();
                rating.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                rating.setValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_VALUE)));
                rating.setComment(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)));
                ratings.add(rating);
            }
            cursor.close();
        }
        return ratings;
    }

    // Update an existing rating.
    public int updateRating(Rating rating) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, rating.getValue());
        values.put(COLUMN_COMMENT, rating.getComment());
        return db.update(TABLE_RATING, values, COLUMN_ID + " = ?", new String[]{String.valueOf(rating.getId())});
    }

    // Delete a rating by its ID.
    public int deleteRating(long id) {
        return db.delete(TABLE_RATING, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // SQLiteOpenHelper for managing the database.
    private static class DBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "decision_helper.db";
        private static final int DATABASE_VERSION = 1;

        private static final String CREATE_TABLE_RATING =
                "CREATE TABLE " + TABLE_RATING + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_VALUE + " REAL, " +
                COLUMN_COMMENT + " TEXT" +
                ")";

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_RATING);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATING);
            onCreate(db);
        }
    }

    // A simple model class to represent a Rating.
    public static class Rating {
        private long id;
        private float value;
        private String comment;

        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public float getValue() {
            return value;
        }
        public void setValue(float value) {
            this.value = value;
        }
        public String getComment() {
            return comment;
        }
        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}