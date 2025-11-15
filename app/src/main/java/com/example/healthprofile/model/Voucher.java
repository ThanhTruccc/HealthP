package com.example.healthprofile.model;

import java.io.Serializable;

public class Voucher implements Serializable {
    private int id;
    private String title;
    private String description;
    private int pointsRequired;
    private int discountPercent;
    private String category; // "examination", "test_package", "consultation"
    private int imageResource;
    private boolean isAvailable;

    public Voucher() {
    }

    public Voucher(String title, String description, int pointsRequired,
                   int discountPercent, String category, int imageResource) {
        this.title = title;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.discountPercent = discountPercent;
        this.category = category;
        this.imageResource = imageResource;
        this.isAvailable = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}