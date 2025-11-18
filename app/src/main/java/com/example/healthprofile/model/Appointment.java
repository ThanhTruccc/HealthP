package com.example.healthprofile.model;

public class Appointment {
    private int id;
    private String patientEmail; // Email của bệnh nhân

    private String patientName;      // Tên bệnh nhân
    private int doctorId;            // ID của bác sĩ
    private String doctorName;       // Tên bác sĩ
    private String phone;            // Số điện thoại
    private String appointmentDate;  // Ngày hẹn (yyyy-MM-dd)
    private String appointmentTime;  // Giờ hẹn (HH:mm)
    private String reason;           // Lý do khám
    private String status;           // pending, confirmed, completed, cancelled
    private String notes;            // Ghi chú từ bác sĩ
    private long timestamp;          // Thời gian tạo
    private int fee;                 // Phí khám

    // Constructor rỗng
    public Appointment() {
    }

    // Constructor đầy đủ
    public Appointment(String patientEmail, String patientName, int doctorId, String doctorName,
                       String phone, String appointmentDate, String appointmentTime,
                       String reason, String status, int fee) {
        this.patientEmail = patientEmail;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.phone = phone;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = status;
        this.notes = "";
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

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    // Helper methods - Backward compatibility
    @Deprecated
    public String getDate() {
        return appointmentDate;
    }

    @Deprecated
    public void setDate(String date) {
        this.appointmentDate = date;
    }

    @Deprecated
    public String getTime() {
        return appointmentTime;
    }

    @Deprecated
    public void setTime(String time) {
        this.appointmentTime = time;
    }

    // Formatting methods
    public String getFormattedDateTime() {
        return appointmentDate + " - " + appointmentTime;
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