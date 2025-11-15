package com.example.healthprofile.model;

import java.io.Serializable;

public class HealthRecord implements Serializable {
    private int id;
    private int userId;
    private String date;
    private float weight;
    private float height;
    private String bloodPressure;
    private float temperature;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String doctorName;
    private String notes;

    public HealthRecord() {
    }

    public HealthRecord(int userId, String date, float weight, float height,
                        String bloodPressure, float temperature, String symptoms,
                        String diagnosis, String treatment, String doctorName, String notes) {
        this.userId = userId;
        this.date = date;
        this.weight = weight;
        this.height = height;
        this.bloodPressure = bloodPressure;
        this.temperature = temperature;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.doctorName = doctorName;
        this.notes = notes;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public float getBMI() {
        if (height > 0) {
            return weight / ((height / 100) * (height / 100));
        }
        return 0;
    }
}
