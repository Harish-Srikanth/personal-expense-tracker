package com.example.expense.data;

public class Expense {
    private String id;
    private String userId;
    private String title;
    private double amount;
    private long date;
    private String category;
    private String notes;
    private long createdAt;

    // Default constructor required for Firestore
    public Expense() {
    }

    public Expense(String id, String userId, String title, double amount, long date, 
                   String category, String notes, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public long getDate() { return date; }
    public String getCategory() { return category; }
    public String getNotes() { return notes; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(long date) { this.date = date; }
    public void setCategory(String category) { this.category = category; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}