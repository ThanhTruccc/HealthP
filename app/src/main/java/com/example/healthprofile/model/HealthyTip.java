package com.example.healthprofile.model;


import java.io.Serializable;
import java.util.Date;

public class HealthyTip implements Serializable {
    private String id;
    private String title;
    private String category;
    private String content;
    private String imageUrl;
    private String author;
    private Date publishDate;
    private int readTime; // Phút
    private int likes;
    private int views;
    private boolean isFavorite;

    // Constructor rỗng
    public HealthyTip() {
    }

    // Constructor đầy đủ
    public HealthyTip(String id, String title, String category, String content,
                     String imageUrl, String author, Date publishDate, int readTime) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.content = content;
        this.imageUrl = imageUrl;
        this.author = author;
        this.publishDate = publishDate;
        this.readTime = readTime;
        this.likes = 0;
        this.views = 0;
        this.isFavorite = false;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public int getReadTime() {
        return readTime;
    }

    public void setReadTime(int readTime) {
        this.readTime = readTime;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // Phương thức tiện ích
    public String getReadTimeText() {
        return readTime + " phút đọc";
    }

    public String getCategoryIcon() {
        switch (category) {
            case "Dinh dưỡng":
                return "🍎";
            case "Tập luyện":
                return "💪";
            case "Sức khỏe tâm thần":
                return "🧠";
            case "Phòng bệnh":
                return "🛡️";
            case "Chăm sóc da":
                return "✨";
            case "Giấc ngủ":
                return "😴";
            default:
                return "📋";
        }
    }
}
