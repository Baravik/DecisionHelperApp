package com.decisionhelperapp.models;

public class User {
    private String email;
    private String name;
    private String id;
    private String profilePictureUrl;

    // Default constructor for Firestore
    public User() {
    }

    // Constructor for regular user registration
    public User(String email, String name, String id) {
        this.email = email;
        this.name = name;
        this.id = id;
    }

    // Constructor for Google sign-in
    public User(String email, String name, String id, String profilePictureUrl) {
        this.email = email;
        this.name = name;
        this.id = id;
        this.profilePictureUrl = profilePictureUrl;

    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
}