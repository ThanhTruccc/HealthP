package com.example.healthprofile.model;

public class Challenge {
    private int id;
    private String title;
    private String description;
    private long startDate;      // Timestamp for new version
    private long endDate;        // Timestamp for new version
    private String startDateStr; // String for admin version
    private String endDateStr;   // String for admin version
    private int durationDays;
    private int participants;
    private int rewardPoints;
    private int imageResource;
    private String status;       // "active", "upcoming", "completed"
    private int statusInt;       // 0=upcoming, 1=active, 2=completed (for admin)
    private boolean isJoined;
    private String imagePath;

    public Challenge() {
    }

    public Challenge(String title, int participants, String duration, int daysLeft, int imageResource) {
        this.title = title;
        this.participants = participants;
        this.durationDays = daysLeft;
        this.imageResource = imageResource;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getStartDateStr() {
        return startDateStr;
    }

    public void setStartDate(String startDateStr) {
        this.startDateStr = startDateStr;
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public void setEndDate(String endDateStr) {
        this.endDateStr = endDateStr;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusInt() {
        return statusInt;
    }

    public void setStatus(int statusInt) {
        this.statusInt = statusInt;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Tính số ngày còn lại (dùng cho user app)
     */
    public int getDaysLeft() {
        if (endDate == 0) return 0;

        long currentTime = System.currentTimeMillis();
        long timeLeft = endDate - currentTime;

        if (timeLeft < 0) {
            return 0;
        }

        return (int) (timeLeft / (24 * 60 * 60 * 1000L));
    }

    /**
     * Text hiển thị số người tham gia
     */
    public String getParticipantsText() {
        return participants + " người tham gia";
    }

    /**
     * Text hiển thị thời gian (dùng cho user app)
     */
    public String getDurationText() {
        int daysLeft = getDaysLeft();
        if (daysLeft == 0) {
            return durationDays + " ngày (Đã kết thúc)";
        }
        return durationDays + " ngày (Còn " + daysLeft + " ngày)";
    }

    /**
     * Text hiển thị phần thưởng
     */
    public String getRewardText() {
        return "+" + rewardPoints + " điểm";
    }

    /**
     * Kiểm tra thử thách còn hoạt động không (user app)
     */
    public boolean isActive() {
        return getDaysLeft() > 0 && "active".equals(status);
    }

    /**
     * Text hiển thị trạng thái (dùng cho admin)
     */
    public String getStatusText() {
        switch (statusInt) {
            case 0: return "Sắp diễn ra";
            case 1: return "Đang diễn ra";
            case 2: return "Đã kết thúc";
            default: return "Không xác định";
        }
    }

    /**
     * Duration text cho admin (dùng String dates)
     */
    public String getDuration() {
        if (startDateStr != null && endDateStr != null) {
            return startDateStr + " - " + endDateStr;
        }
        return "Chưa cập nhật";
    }
}