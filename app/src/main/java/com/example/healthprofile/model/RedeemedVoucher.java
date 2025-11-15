package com.example.healthprofile.model;

import java.io.Serializable;

public class RedeemedVoucher implements Serializable {
    private int id;
    private String userEmail;
    private int voucherId;
    private String voucherTitle;
    private int pointsUsed;
    private long redeemedDate;
    private String status; // "active", "used", "expired"
    private String voucherCode;

    public RedeemedVoucher() {
    }

    public RedeemedVoucher(String userEmail, int voucherId, String voucherTitle,
                           int pointsUsed, String voucherCode) {
        this.userEmail = userEmail;
        this.voucherId = voucherId;
        this.voucherTitle = voucherTitle;
        this.pointsUsed = pointsUsed;
        this.voucherCode = voucherCode;
        this.redeemedDate = System.currentTimeMillis();
        this.status = "active";
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

    public int getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherTitle() {
        return voucherTitle;
    }

    public void setVoucherTitle(String voucherTitle) {
        this.voucherTitle = voucherTitle;
    }

    public int getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(int pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    public long getRedeemedDate() {
        return redeemedDate;
    }

    public void setRedeemedDate(long redeemedDate) {
        this.redeemedDate = redeemedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}