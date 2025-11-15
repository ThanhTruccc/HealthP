package com.example.healthprofile.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BMIRecord {
    private int id;
    private String gender;
    private int age;
    private float height;
    private float weight;
    private float bmi;
    private String category;
    private long timestamp;

    public BMIRecord() {
        this.timestamp = System.currentTimeMillis();
    }

    public BMIRecord(String gender, int age, float height, float weight) {
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.bmi = calculateBMI(height, weight);
        this.category = getBMICategory(this.bmi);
        this.timestamp = System.currentTimeMillis();
    }

    private float calculateBMI(float height, float weight) {
        float heightInMeters = height / 100;
        return weight / (heightInMeters * heightInMeters);
    }

    private String getBMICategory(float bmi) {
        if (bmi < 18.5) {
            return "Thiếu cân";
        } else if (bmi >= 18.5 && bmi < 25) {
            return "Bình thường";
        } else if (bmi >= 25 && bmi < 30) {
            return "Thừa cân";
        } else if (bmi >= 30 && bmi < 35) {
            return "Béo phì độ I";
        } else {
            return "Béo phì độ II";
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getBmi() {
        return bmi;
    }

    public void setBmi(float bmi) {
        this.bmi = bmi;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getBmiFormatted() {
        return String.format(Locale.getDefault(), "%.1f", bmi);
    }

    public String getHeightFormatted() {
        return String.format(Locale.getDefault(), "%.0f cm", height);
    }

    public String getWeightFormatted() {
        return String.format(Locale.getDefault(), "%.1f kg", weight);
    }
}
