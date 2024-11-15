package com.example.ad_gk.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ad_gk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class StudentDetailActivity extends AppCompatActivity {

    private TextView tvStudentName, tvStudentAge, tvStudentPhone, tvStudentGender, tvStudentEmail, tvStudentAddress, tvStudentCertificates;
    private String studentId;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        // Lấy studentId từ Intent
        studentId = getIntent().getStringExtra("studentId");
        back = findViewById(R.id.btn_back_detail);

        // Khởi tạo các TextView
        tvStudentName = findViewById(R.id.tv_studentName_detail);
        tvStudentAge = findViewById(R.id.tv_studentAge_detail);
        tvStudentPhone = findViewById(R.id.tv_studentPhone_detail);
        tvStudentGender = findViewById(R.id.tv_studentGender_detail);
        tvStudentEmail = findViewById(R.id.tv_studentEmail_detail);
        tvStudentAddress = findViewById(R.id.tv_studentAddress_detail);
        tvStudentCertificates = findViewById(R.id.tv_studentCertificates_detail);

        // Load dữ liệu từ Firestore
        loadStudentData(studentId);

        // Xử lý sự kiện khi nhấn nút "back"
        back.setOnClickListener(v -> finish());
    }

    private void loadStudentData(String studentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference studentRef = db.collection("students").document(studentId);

        studentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String name = document.getString("name");
                    Long ageLong = document.getLong("age");
                    int age = (ageLong != null) ? ageLong.intValue() : 0;
                    String phone = document.getString("phoneNumber");
                    String gender = document.getString("gender");
                    String email = document.getString("email");
                    String address = document.getString("address");
                    List<String> certificates = (List<String>) document.get("certificates");

                    tvStudentName.setText(name != null ? name : "N/A");
                    tvStudentAge.setText("Age: " + (age > 0 ? age : "N/A"));
                    tvStudentPhone.setText("Phone: " + (phone != null ? phone : "N/A"));
                    tvStudentGender.setText("Gender: " + (gender != null ? gender : "N/A"));
                    tvStudentEmail.setText("Email: " + (email != null ? email : "N/A"));
                    tvStudentAddress.setText("Address: " + (address != null ? address : "N/A"));

                    // Load tên của chứng chỉ
                    loadCertificateNames(certificates);
                } else {
                    Log.e("StudentDetail", "No such document");
                }
            } else {
                Log.e("StudentDetail", "get failed with ", task.getException());
            }
        });
    }

    private void loadCertificateNames(List<String> certificateIds) {
        if (certificateIds == null || certificateIds.isEmpty()) {
            tvStudentCertificates.setText("Certificates: No certificates");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        StringBuilder certificatesBuilder = new StringBuilder("Certificates: ");

        for (String certId : certificateIds) {
            DocumentReference certRef = db.collection("certificates").document(certId);
            certRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    String certName = task.getResult().getString("name");
                    certificatesBuilder.append(certName).append(", ");
                    tvStudentCertificates.setText(certificatesBuilder.toString().replaceAll(", $", ""));
                }
            });
        }
    }

}
