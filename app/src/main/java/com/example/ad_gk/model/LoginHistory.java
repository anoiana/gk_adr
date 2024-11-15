package com.example.ad_gk.model;

public class LoginHistory {
    private String userId;       // ID người dùng
    private String loginTime;    // Thời gian đăng nhập
    private String logoutTime;   // Thời gian đăng xuất
    private String deviceInfo;   // Thông tin thiết bị (nếu cần)

    // Constructor mặc định cần thiết cho Firestore
    public LoginHistory() {}

    // Constructor có tham số
    public LoginHistory(String userId, String loginTime, String logoutTime, String deviceInfo) {
        this.userId = userId;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.deviceInfo = deviceInfo;
    }

    // Getter và Setter cho các trường
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLoginTime() { return loginTime; }
    public void setLoginTime(String loginTime) { this.loginTime = loginTime; }

    public String getLogoutTime() { return logoutTime; }
    public void setLogoutTime(String logoutTime) { this.logoutTime = logoutTime; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
}
