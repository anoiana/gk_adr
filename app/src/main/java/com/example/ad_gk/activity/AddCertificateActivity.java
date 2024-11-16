package com.example.ad_gk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ad_gk.R;
import com.example.ad_gk.model.Certificate;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;  // Import thêm ArrayList để sử dụng danh sách trống

public class AddCertificateActivity extends AppCompatActivity {

    private EditText editTextCertificateName, editTextIssueDate, editTextIssuer;
    private Button buttonSaveCertificate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_certificate);

        // Khởi tạo các view
        editTextCertificateName = findViewById(R.id.editTextCertificateName);
        editTextIssueDate = findViewById(R.id.editTextIssueDate);
        editTextIssuer = findViewById(R.id.editTextIssuer);
        buttonSaveCertificate = findViewById(R.id.buttonSaveCertificate);

        // Lắng nghe sự kiện nhấn nút Save Certificate
        buttonSaveCertificate.setOnClickListener(v -> saveCertificateToFirestore());

        // Edge-to-edge setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
//    private void saveCertificateToFirestoreFile()
    private void saveCertificateToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy toàn bộ certificate từ Firestore
        db.collection("certificates").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int maxId = 0;

                // Duyệt qua các tài liệu để tìm certificateId lớn nhất
                for (DocumentSnapshot document : task.getResult()) {
                    String certId = document.getId();
                    try {
                        // Lấy phần số từ certificateId (bỏ qua ký tự đầu tiên "C")
                        int numericId = Integer.parseInt(certId.substring(1));
                        if (numericId > maxId) {
                            maxId = numericId;
                        }
                    } catch (NumberFormatException e) {
                        Log.e("CertificateSave", "Error parsing certificateId: " + certId, e);
                    }
                }

                // Tạo certificateId mới bằng cách tăng maxId lên 1
                String newCertificateId = String.format("C%05d", maxId + 1);

                // Lấy dữ liệu chứng chỉ từ các trường nhập liệu
                String certificateName = editTextCertificateName.getText().toString();
                String issueDate = editTextIssueDate.getText().toString();
                String issuer = editTextIssuer.getText().toString();

                // Tạo danh sách studentIds trống
                ArrayList<String> studentIds = new ArrayList<>();

                // Tạo và lưu chứng chỉ
                Certificate certificate = new Certificate(newCertificateId, certificateName, issueDate, issuer, studentIds);
                db.collection("certificates").document(newCertificateId)
                        .set(certificate)
                        .addOnSuccessListener(aVoid -> {
                            // Thông báo thêm chứng chỉ thành công
                            Toast.makeText(this, "Certificate added successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);  // Set result to notify that the certificate is added
                            finish(); // Finish the activity and return to ListCertificateFragment
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Failed to retrieve certificates: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
