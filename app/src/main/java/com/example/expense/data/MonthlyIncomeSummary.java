package com.example.expense.data;

import java.util.Map;

public class MonthlyIncomeSummary {
    private int month;
    private int year;
    private double totalAmount;
    private Map<String, Double> categories;

    // Default constructor required for Firestore
    public MonthlyIncomeSummary() {
    }

    public MonthlyIncomeSummary(int month, int year, double totalAmount, Map<String, Double> categories) {
        this.month = month;
        this.year = year;
        this.totalAmount = totalAmount;
        this.categories = categories;
    }

    // Getters
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public double getTotalAmount() { return totalAmount; }
    public Map<String, Double> getCategories() { return categories; }

    // Setters
    public void setMonth(int month) { this.month = month; }
    public void setYear(int year) { this.year = year; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setCategories(Map<String, Double> categories) { this.categories = categories; }
}