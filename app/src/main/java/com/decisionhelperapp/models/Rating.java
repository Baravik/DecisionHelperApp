package com.decisionhelperapp.models;

public class Rating {

    private int id;
    private int value;
    private String comment;

    public Rating() { }

    public Rating(int id, int value, String comment) {
        this.id = id;
        this.value = value;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
