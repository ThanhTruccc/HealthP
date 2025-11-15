package com.example.healthprofile.model;

import java.io.Serializable;

public class HealthProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String userEmail; // Liên kết với user
    private String fullName;
    private int age;
    private String gender; // "Nam", "Nữ", "Khác"
    private float height; // cm
    private float weight; // kg
    private String bloodType; // A, B, AB, O
    private String rhFactor; // +, -
    private String allergies; // Dị ứng
    private String chronicDiseases; // Bệnh mãn tính
    private String medications; // Thuốc đang dùng
    private String emergencyContact; // SĐT người thân
    private String emergencyContactName; // Tên người thân
    private String notes; // Ghi chú thêm
    private long lastUpdated;

    public HealthProfile() {
        this.lastUpdated = System.currentTimeMillis();
    }

    // Constructor đầy đủ
    public HealthProfile(String userEmail, String fullName, int age, String gender,
                         float height, float weight, String bloodType) {
        this.userEmail = userEmail;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.bloodType = bloodType;
        this.lastUpdated = System.currentTimeMillis();
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getRhFactor() {
        return rhFactor;
    }

    public void setRhFactor(String rhFactor) {
        this.rhFactor = rhFactor;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getChronicDiseases() {
        return chronicDiseases;
    }

    public void setChronicDiseases(String chronicDiseases) {
        this.chronicDiseases = chronicDiseases;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Helper methods
    public float getBMI() {
        if (height > 0 && weight > 0) {
            float heightInMeters = height / 100;
            return weight / (heightInMeters * heightInMeters);
        }
        return 0;
    }

    public String getBMICategory() {
        float bmi = getBMI();
        if (bmi < 18.5) return "Thiếu cân";
        else if (bmi < 25) return "Bình thường";
        else if (bmi < 30) return "Thừa cân";
        else return "Béo phì";
    }

    public String getFullBloodType() {
        if (bloodType != null && rhFactor != null) {
            return bloodType + rhFactor;
        }
        return bloodType != null ? bloodType : "Chưa cập nhật";
    }
}