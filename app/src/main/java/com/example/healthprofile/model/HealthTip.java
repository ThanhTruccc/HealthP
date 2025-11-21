package com.example.healthprofile.model;

public class HealthTip {
    private int id;
    private String title;
    private String content;
    private String category; // diet, exercise, mental_health, general, prevention
    private int iconResId;

    public HealthTip() {
    }

    public HealthTip(int id, String title, String content, String category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    // Helper methods
    public String getCategoryName() {
        switch (category) {
            case "diet":
                return "Dinh dưỡng";
            case "exercise":
                return "Vận động";
            case "mental_health":
                return "Sức khỏe tinh thần";
            case "prevention":
                return "Phòng bệnh";
            case "general":
            default:
                return "Tổng quát";
        }
    }

    public int getCategoryColor() {
        switch (category) {
            case "diet":
                return android.graphics.Color.parseColor("#FF9800"); // Orange
            case "exercise":
                return android.graphics.Color.parseColor("#4CAF50"); // Green
            case "mental_health":
                return android.graphics.Color.parseColor("#9C27B0"); // Purple
            case "prevention":
                return android.graphics.Color.parseColor("#2196F3"); // Blue
            case "general":
            default:
                return android.graphics.Color.parseColor("#607D8B"); // Grey
        }
    }

    public int getCategoryIcon() {
        switch (category) {
            case "diet":
                return android.R.drawable.ic_menu_agenda;
            case "exercise":
                return android.R.drawable.ic_menu_compass;
            case "mental_health":
                return android.R.drawable.ic_menu_help;
            case "prevention":
                return android.R.drawable.ic_menu_view;
            case "general":
            default:
                return android.R.drawable.ic_menu_info_details;
        }
    }
}