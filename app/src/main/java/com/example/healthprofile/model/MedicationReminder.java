package com.example.healthprofile.model;

import java.io.Serializable;

public class MedicationReminder implements Serializable {
    private int id;
    private String userEmail;
    private String medicationName;
    private String dosage;              // Liều lượng (vd: 500mg, 2 viên)
    private String frequency;           // Tần suất (daily, twice_daily, three_times_daily, weekly)
    private String time1;               // Thời gian 1 (HH:mm)
    private String time2;               // Thời gian 2 (HH:mm)
    private String time3;               // Thời gian 3 (HH:mm)
    private String startDate;           // Ngày bắt đầu (dd/MM/yyyy)
    private String endDate;             // Ngày kết thúc (dd/MM/yyyy)
    private String notes;               // Ghi chú
    private boolean isActive;           // Đang hoạt động
    private long createdAt;
    private long updatedAt;

    public MedicationReminder() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isActive = true;
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

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getTime1() {
        return time1;
    }

    public void setTime1(String time1) {
        this.time1 = time1;
    }

    public String getTime2() {
        return time2;
    }

    public void setTime2(String time2) {
        this.time2 = time2;
    }

    public String getTime3() {
        return time3;
    }

    public void setTime3(String time3) {
        this.time3 = time3;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public String getFrequencyText() {
        switch (frequency) {
            case "daily":
                return "Mỗi ngày";
            case "twice_daily":
                return "2 lần/ngày";
            case "three_times_daily":
                return "3 lần/ngày";
            case "weekly":
                return "Hàng tuần";
            default:
                return "Không xác định";
        }
    }

    public String getTimeSchedule() {
        StringBuilder schedule = new StringBuilder();
        if (time1 != null && !time1.isEmpty()) {
            schedule.append(time1);
        }
        if (time2 != null && !time2.isEmpty()) {
            if (schedule.length() > 0) schedule.append(", ");
            schedule.append(time2);
        }
        if (time3 != null && !time3.isEmpty()) {
            if (schedule.length() > 0) schedule.append(", ");
            schedule.append(time3);
        }
        return schedule.toString();
    }

    public String getDuration() {
        if (startDate != null && endDate != null) {
            return startDate + " - " + endDate;
        }
        return "Chưa xác định";
    }
}