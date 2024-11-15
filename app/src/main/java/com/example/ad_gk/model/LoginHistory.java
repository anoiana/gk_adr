package com.example.ad_gk.model;

public class LoginHistory {
    private String userId;       // ID người dùng
    private String loginTime;    // Thời gian đăng nhập
    private String loginId;   // Thời gian đăng xuất

    // Constructor mặc định cần thiết cho Firestore
    public LoginHistory() {}

    // Constructor có tham số
    public LoginHistory(String loginId, String loginTime, String userId) {
        this.loginId = loginId;
        this.loginTime = loginTime;
        this.userId = userId;

    }

    // Getter và Setter cho các trường
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLoginTime() { return loginTime; }
    public void setLoginTime(String loginTime) { this.loginTime = loginTime; }

    public String getloginId() { return loginId; }
    public void setloginId(String logoutTime) { this.loginId = logoutTime; }
}
