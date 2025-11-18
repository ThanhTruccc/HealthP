package com.example.healthprofile.model;

public class Exercise {
    private int id;
    private String userEmail;
    private String exerciseType;
    private int durationMinutes;
    private double caloriesBurned;
    private String date;
    private String notes;
    private long createdAt;

    public Exercise() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public double getCaloriesBurned() {
        return caloriesBurned;
    }

    public String getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}