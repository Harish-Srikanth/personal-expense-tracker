package com.example.expense.data;

public class CategorySummary {
    private String name;
    private double amount;
    private int color;

    // Default constructor required for Firestore
    public CategorySummary() {
    }

    public CategorySummary(String name, double amount) {
        this.name = name;
        this.amount = amount;
        this.color = 0; // Default value
    }

    public CategorySummary(String name, double amount, int color) {
        this.name = name;
        this.amount = amount;
        this.color = color;
    }

    // Getters
    public String getName() { return name; }
    public double getAmount() { return amount; }
    public int getColor() { return color; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setColor(int color) { this.color = color; }
}