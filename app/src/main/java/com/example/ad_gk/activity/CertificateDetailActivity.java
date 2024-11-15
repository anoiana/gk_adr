package com.example.ad_gk.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ad_gk.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class CertificateDetailActivity extends AppCompatActivity {

    private TextView tvCertificateId, tvCertificateName, tvIssueDate, tvIssuer, tvStudentIds;
    private FirebaseFirestore db;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_detail);

        // Khởi tạo các TextView và Button
        tvCertificateId = findViewById(R.id.tvCertificateId);
        tvCertificateName = findViewById(R.id.tvCertificateName);
        tvIssueDate = findViewById(R.id.tvIssueDate);
        tvIssuer = findViewById(R.id.tvIssuer);
        tvStudentIds = findViewById(R.id.tvStudentIds);
        btnBack = findViewById(R.id.btnBack);

        // Xử lý sự kiện nút "Trở về"
        btnBack.setOnClickListener(v -> finish());

        // Khởi tạo Firestore và load chi tiết chứng chỉ
        db = FirebaseFirestore.getInstance();
        String certificateId = getIntent().getStringExtra("certificateId");
        loadCertificateDetails(certificateId);
    }

    // Hàm load chi tiết chứng chỉ từ Firestore
    private void loadCertificateDetails(String certificateId) {
        db.collection("certificates").document(certificateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Lấy dữ liệu từ Firestore và hiển thị lên UI
                        String name = documentSnapshot.getString("name");
                        String issueDate = documentSnapshot.getString("issueDate");
                        String issuer = documentSnapshot.getString("issuer");
                        List<String> studentIds = (List<String>) documentSnapshot.get("studentIds");

                        tvCertificateId.setText(certificateId);
                        tvCertificateName.setText(name);
                        tvIssueDate.setText(issueDate);
                        tvIssuer.setText(issuer);

                        // Hiển thị danh sách studentIds và load tên sinh viên
                        if (studentIds != null && !studentIds.isEmpty()) {
                            StringBuilder studentNamesText = new StringBuilder();

                            for (String studentId : studentIds) {
                                // Truy vấn Firestore để lấy tên sinh viên theo studentId
                                db.collection("students").document(studentId)
                                        .get()
                                        .addOnSuccessListener(studentSnapshot -> {
                                            if (studentSnapshot.exists()) {
                                                String studentName = studentSnapshot.getString("name");
                                                studentNamesText.append(studentName).append(" (").append(studentId).append(")\n");
                                            } else {
                                                studentNamesText.append("Unknown student (").append(studentId).append(")\n");
                                            }
                                            tvStudentIds.setText(studentNamesText.toString().trim());
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Failed to load student details", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            tvStudentIds.setText("No students");
                        }
                    } else {
                        Toast.makeText(this, "Certificate not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load certificate details", Toast.LENGTH_SHORT).show());
    }
}
