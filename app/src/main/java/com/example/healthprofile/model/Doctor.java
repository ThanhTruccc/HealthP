package com.example.healthprofile.model;

import java.io.Serializable;

public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String degree;
    private String specialty;
    private String workplace; // Thêm trường nơi làm việc
    private float rating;
    private int experience;
    private int imageResource;
    private String imagePath;

    public Doctor() {
    }

    public Doctor(int id, String name, String degree, String specialty, String workplace,
                  float rating, int experience, int imageResource) {
        this.id = id;
        this.name = name;
        this.degree = degree;
        this.specialty = specialty;
        this.workplace = workplace;
        this.rating = rating;
        this.experience = experience;
        this.imageResource = imageResource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getExperienceText() {
        return experience + " năm kinh nghiệm";
    }

    public String getRatingText() {
        return String.format("%.1f", rating);
    }
}