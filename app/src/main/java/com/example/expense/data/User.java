package com.example.expense.data;

import com.google.firebase.Timestamp;

public class User {
    private String userId;
    private String name;
    private String email;
    private Timestamp createdAt;

    // Default constructor required for Firebase
    public User() {
    }

    // Constructor with parameters
    public User(String userId, String name, String email, Timestamp createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}