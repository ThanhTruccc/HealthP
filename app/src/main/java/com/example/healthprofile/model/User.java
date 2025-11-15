package com.example.healthprofile.model;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String password; // Giữ nguyên
    private String fullName;
    private String email; // Sử dụng làm định danh thay cho username
    private String phone;
    private String role;

    // Constructor đầy đủ
    public User(int id, String password, String fullName,
                String email, String phone, String role) {
        this.id = id;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public User(String password, String fullName,
                String email, String phone, String role) {
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Xóa: public String getUsername() { ... }
    // Xóa: public void setUsername(String username) { ... }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }
}