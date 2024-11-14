package com.example.ad_gk.model;

import java.util.List;

public class Student {
    private String studentId;  // ID sinh viên (unique)
    private String name;       // Tên sinh viên
    private int age;           // Tuổi sinh viên
    private String phoneNumber; // Số điện thoại sinh viên
    private String gender;     // Giới tính sinh viên
    private String email;      // Email sinh viên
    private String address;    // Địa chỉ sinh viên
    private List<String> certificates; // Danh sách chứng chỉ sinh viên

    // Constructor mặc định cần thiết cho Firestore
    public Student() {}

    // Constructor có tham số
    public Student(String studentId, String name, int age, String phoneNumber, String gender, String email, String address, List<String> certificates) {
        this.studentId = studentId;
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.email = email;
        this.address = address;
        this.certificates = certificates;
    }

    // Getter và Setter cho các trường
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<String> getCertificates() { return certificates; }
    public void setCertificates(List<String> certificates) { this.certificates = certificates; }
}

