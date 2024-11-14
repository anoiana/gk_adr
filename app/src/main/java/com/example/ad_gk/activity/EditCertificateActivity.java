package com.example.ad_gk.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ad_gk.R;
import com.example.ad_gk.model.Certificate;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EditCertificateActivity extends AppCompatActivity {

    private EditText editTextCertificateName, editTextIssueDate, editTextIssuer;
    private Button buttonSaveCertificate;
    private String certificateId; // ID của chứng chỉ cần chỉnh sửa
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_certificate);

        // Khởi tạo các view
        editTextCertificateName = findViewById(R.id.editTextCertificateName);
        editTextIssueDate = findViewById(R.id.editTextIssueDate);
        editTextIssuer = findViewById(R.id.editTextIssuer);
        buttonSaveCertificate = findViewById(R.id.buttonUpdateCertificate);

        // Khởi tạo Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Lấy certificateId từ intent
        certificateId = getIntent().getStringExtra("certificateId");

        // Tải dữ liệu chứng chỉ
        loadCertificateData();

        // Xử lý sự kiện nút lưu
        buttonSaveCertificate.setOnClickListener(v -> updateCertificateInFirestore());
    }

    private void loadCertificateData() {
        // Lấy dữ liệu chứng chỉ từ Firestore
        db.collection("certificates").document(certificateId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Certificate certificate = documentSnapshot.toObject(Certificate.class);
                        if (certificate != null) {
                            editTextCertificateName.setText(certificate.getName());
                            editTextIssueDate.setText(certificate.getIssueDate());
                            editTextIssuer.setText(certificate.getIssuer());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải dữ liệu chứng chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateCertificateInFirestore() {
        // Lấy dữ liệu từ các view
        String name = editTextCertificateName.getText().toString().trim();
        String issueDate = editTextIssueDate.getText().toString().trim();
        String issuer = editTextIssuer.getText().toString().trim();

        // Kiểm tra xem tất cả các trường có đầy đủ thông tin không
        if (name.isEmpty() || issueDate.isEmpty() || issuer.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy studentIds hiện tại từ Firestore trước khi cập nhật
        db.collection("certificates").document(certificateId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Certificate certificate = documentSnapshot.toObject(Certificate.class);
                        if (certificate != null) {
                            // Lấy studentIds hiện tại từ chứng chỉ
                            List<String> studentIds = certificate.getStudentIds(); // Dùng List<String> thay vì ArrayList

                            // Tạo đối tượng Certificate mới với dữ liệu đã nhập và giữ nguyên studentIds
                            Certificate updatedCertificate = new Certificate(certificateId, name, issueDate, issuer, studentIds);

                            // Cập nhật chứng chỉ trong Firestore
                            db.collection("certificates").document(certificateId)
                                    .set(updatedCertificate) // Cập nhật tài liệu với dữ liệu mới
                                    .addOnSuccessListener(aVoid -> {
                                        // Hiển thị thông báo thành công và quay lại Activity trước đó
                                        Toast.makeText(this, "Cập nhật chứng chỉ thành công!", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish(); // Quay lại màn hình trước (ListCertificateFragment)
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật chứng chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lấy studentIds: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
