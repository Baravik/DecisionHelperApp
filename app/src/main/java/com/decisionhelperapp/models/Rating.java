package com.decisionhelperapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Rating implements Parcelable {
    private String id;
    private String userId;
    private String quizId;
    private int score;
    private Date timestamp;
    private String comment; // Optional comment

    public Rating() {
        this.timestamp = new Date(); // Set default timestamp to now
    }

    public Rating(String userId, String quizId, int score) {
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.timestamp = new Date();
    }

    public Rating(String userId, String quizId, int score, String comment) {
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.comment = comment;
        this.timestamp = new Date();
    }

    // Parcelable implementation
    protected Rating(Parcel in) {
        id = in.readString();
        userId = in.readString();
        quizId = in.readString();
        score = in.readInt();
        timestamp = new Date(in.readLong());
        comment = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(quizId);
        dest.writeInt(score);
        dest.writeLong(timestamp != null ? timestamp.getTime() : 0);
        dest.writeString(comment);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Rating> CREATOR = new Creator<Rating>() {
        @Override
        public Rating createFromParcel(Parcel in) {
            return new Rating(in);
        }

        @Override
        public Rating[] newArray(int size) {
            return new Rating[size];
        }
    };

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTimestamp() {
        return timestamp.toString();
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
