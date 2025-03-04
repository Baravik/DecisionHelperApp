package com.decisionhelperapp.models;

import java.util.Date;

public class User {
    private String email;
    private String name;
    private String id;
    private String username;
    private String passwordHash;
    private String profilePictureUrl;
    private boolean viaGmail;
    private Date creationDate;
    private Date lastLoginDate;
    private Date lastUpdated;

    // Default constructor for Firestore
    public User() {
    }

    // Constructor for regular user registration
    public User(String email, String name, String id, String passwordHash) {
        this.email = email;
        this.name = name;
        this.id = id;
        this.passwordHash = passwordHash;
        this.viaGmail = false;
        this.creationDate = new Date();
        this.lastLoginDate = new Date();
        this.lastUpdated = new Date();
    }

    // Constructor for Google sign-in
    public User(String email, String name, String id, String profilePictureUrl, boolean viaGmail) {
        this.email = email;
        this.name = name;
        this.id = id;
        this.profilePictureUrl = profilePictureUrl;
        this.viaGmail = viaGmail;
        this.creationDate = new Date();
        this.lastLoginDate = new Date();
        this.lastUpdated = new Date();
    }

    // Constructor for guest user
    public User(String id) {
        this.name = "Guest";
        this.id = id;
        this.viaGmail = false;
        this.creationDate = new Date();
        this.lastLoginDate = new Date();
        this.lastUpdated = new Date();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public boolean isViaGmail() {
        return viaGmail;
    }

    public void setViaGmail(boolean viaGmail) {
        this.viaGmail = viaGmail;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}