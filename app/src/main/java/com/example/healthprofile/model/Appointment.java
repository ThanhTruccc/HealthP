package com.example.healthprofile.model;

public class Appointment {
    private int id;
    private String userEmail;  // ← THÊM FIELD NÀY
    private String doctorName;
    private String patientName;
    private String phone;
    private String date;
    private String time;
    private String reason;
    private String status; // pending, confirmed, completed, cancelled
    private long timestamp;
    private int fee;

    // Constructor rỗng
    public Appointment() {
    }

    // Constructor đầy đủ
    public Appointment(String userEmail, String doctorName, String patientName, String phone,
                       String date, String time, String reason, String status, int fee) {
        this.userEmail = userEmail;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
        this.fee = fee;
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

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    // Helper methods
    public String getFormattedDate() {
        return date;
    }

    public String getFormattedTime() {
        return time;
    }

    public String getStatusText() {
        switch (status) {
            case "pending":
                return "Chờ xác nhận";
            case "confirmed":
                return "Đã xác nhận";
            case "completed":
                return "Hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "pending":
                return android.graphics.Color.parseColor("#FFA726"); // Orange
            case "confirmed":
                return android.graphics.Color.parseColor("#66BB6A"); // Green
            case "completed":
                return android.graphics.Color.parseColor("#42A5F5"); // Blue
            case "cancelled":
                return android.graphics.Color.parseColor("#EF5350"); // Red
            default:
                return android.graphics.Color.GRAY;
        }
    }

    public String getFeeFormatted() {
        return String.format("%,d VNĐ", fee);
    }
}