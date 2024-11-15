package com.example.ad_gk.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;  // ID người dùng (unique)
    private String name;    // Tên người dùng
    private int age;        // Tuổi
    private String phoneNumber; // Số điện thoại
    private String status;  // Trạng thái người dùng (Normal/Locked)
    private String role;    // Vai trò (admin, manager, employee)
    private String profilePicture;
    private List<String> historyLogin;// Đường dẫn ảnh đại diện

    // Constructor mặc định cần thiết cho Firestore
    public User() {}

    // Constructor có tham số
    public User(String userId, String name, int age, String phoneNumber, String status, String role, String profilePicture) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.role = role;
        this.profilePicture = profilePicture;
        this.historyLogin = new ArrayList<>();
    }

    // Getter và Setter cho các trường
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public List<String> getHistoryLogin() {
        return historyLogin;
    }

    public void setHistoryLogin(List<String> historyLogin) {
        this.historyLogin = historyLogin;
    }
}

