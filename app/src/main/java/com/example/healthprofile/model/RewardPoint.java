package com.example.healthprofile.model;

import java.io.Serializable;

public class RewardPoint implements Serializable {
    private int id;
    private String userEmail;
    private int points;
    private String actionn; // "save_profile", "book_appointment", "redeem_voucher"
    private int pointsChange; // +10, -50, etc.
    private long timestamp;
    private String description;

    public RewardPoint() {
    }

    public RewardPoint(String userEmail, int points, String actionn,
                       int pointsChange, String description) {
        this.userEmail = userEmail;
        this.points = points;
        this.actionn = actionn;
        this.pointsChange = pointsChange;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getActionn() {
        return actionn;
    }

    public void setActionn(String actionn) {
        this.actionn = actionn;
    }

    public int getPointsChange() {
        return pointsChange;
    }

    public void setPointsChange(int pointsChange) {
        this.pointsChange = pointsChange;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}