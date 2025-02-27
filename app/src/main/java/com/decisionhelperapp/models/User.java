package com.decisionhelperapp.models;

public class User {
    private int userId;
    private String email;
    private String name;

    public User(int userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}