package com.example.ad_gk.activity;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

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

        // Lắng nghe sự kiện nhấn nút Lưu Chứng Chỉ
        buttonSaveCertificate.setOnClickListener(v -> saveCertificateToFirestore());

        // Thiết lập Edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false); // Không cho phép định dạng ngày tự động điều chỉnh
        try {
            sdf.parse(date); // Cố gắng parse ngày
            return true; // Hợp lệ nếu không ném ra ngoại lệ
        } catch (ParseException e) {
            return false; // Không hợp lệ
        }
    }

    private void saveCertificateToFirestore() {
        // Lấy dữ liệu từ các trường nhập liệu
        String certificateName = editTextCertificateName.getText().toString().trim();
        String issueDate = editTextIssueDate.getText().toString().trim();
        String issuer = editTextIssuer.getText().toString().trim();

        // Kiểm tra các trường không được để trống
        if (certificateName.isEmpty()) {
            editTextCertificateName.setError("Vui lòng nhập tên chứng chỉ");
            editTextCertificateName.requestFocus();
            return;
        }

        if (!isValidDate(issueDate)) {
            editTextIssueDate.setError("Ngày cấp không đúng định dạng dd/MM/yyyy");
            editTextIssueDate.requestFocus();
            return;
        }

        if (issuer.isEmpty()) {
            editTextIssuer.setError("Vui lòng nhập đơn vị cấp");
            editTextIssuer.requestFocus();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy toàn bộ danh sách chứng chỉ từ Firestore
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
                        Log.e("CertificateSave", "Lỗi khi phân tích certificateId: " + certId, e);
                    }
                }

                // Tạo certificateId mới bằng cách tăng maxId lên 1
                String newCertificateId = String.format("C%05d", maxId + 1);

                // Tạo danh sách studentIds trống
                ArrayList<String> studentIds = new ArrayList<>();

                // Tạo và lưu chứng chỉ
                Certificate certificate = new Certificate(newCertificateId, certificateName, issueDate, issuer, studentIds);
                db.collection("certificates").document(newCertificateId)
                        .set(certificate)
                        .addOnSuccessListener(aVoid -> {
                            // Thông báo thêm chứng chỉ thành công
                            Toast.makeText(this, "Thêm chứng chỉ thành công!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);  // Set kết quả để thông báo rằng chứng chỉ đã được thêm
                            finish(); // Đóng Activity và quay lại ListCertificateFragment
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Thêm chứng chỉ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Không thể tải danh sách chứng chỉ: " + (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
