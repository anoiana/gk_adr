package com.example.ad_gk.model;

import java.util.List;

public class Certificate {
    private String certificateId; // ID chứng chỉ (unique)
    private String name;          // Tên chứng chỉ
    private String issueDate;     // Ngày cấp chứng chỉ
    private String issuer;        // Cơ quan cấp chứng chỉ
    private List<String> studentIds; // Danh sách mã sinh viên (mối quan hệ nhiều-nhiều)

    // Constructor mặc định cần thiết cho Firestore
    public Certificate() {}

    // Constructor có tham số
    public Certificate(String certificateId, String name, String issueDate, String issuer, List<String> studentIds) {
        this.certificateId = certificateId;
        this.name = name;
        this.issueDate = issueDate;
        this.issuer = issuer;
        this.studentIds = studentIds;
    }

    // Getter và Setter cho các trường
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public List<String> getStudentIds() { return studentIds; }
    public void setStudentIds(List<String> studentIds) { this.studentIds = studentIds; }
}
