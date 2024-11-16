package com.example.ad_gk.activity;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ad_gk.R;
import com.example.ad_gk.model.LoginHistory;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private ImageView imgTogglePassword;
    private EditText edtRole, edtUserId;
    private Button btnLogin;
    private FirebaseFirestore firestore;
    private boolean isPasswordVisible = false;
    private ProgressBar progressBar;  // Declare ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ các view từ layout
        edtRole = findViewById(R.id.edtEmai);
        edtUserId = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.bntLogin);
        progressBar = findViewById(R.id.progressBar);  // Initialize ProgressBar

        // Khởi tạo Firestore
        firestore = FirebaseFirestore.getInstance();
        imgTogglePassword = findViewById(R.id.imgTogglePassword);

        // Set up toggle password visibility
        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Thiết lập sự kiện click cho nút Login
        btnLogin.setOnClickListener(view -> {
            // Lấy thông tin từ EditText
            String role = edtRole.getText().toString().trim();
            String userId = edtUserId.getText().toString().trim();

            // Kiểm tra thông tin nhập vào
            if (TextUtils.isEmpty(role)) {
                edtRole.setError("Vai trò là bắt buộc");
                return;
            }

            if (TextUtils.isEmpty(userId)) {
                edtUserId.setError("ID người dùng là bắt buộc");
                return;
            }

            // Hiển thị ProgressBar khi bắt đầu xác thực
            showProgressBar();

            // Gọi hàm xác thực
            authenticateUser(role, userId);
        });
    }

    private void authenticateUser(String role, String userId) {
        firestore.collection("users")
                .whereEqualTo("role", role)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    // Ẩn ProgressBar khi hoàn tất xác thực
                    hideProgressBar();

                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            // Lưu thông tin đăng nhập
                            saveLoginHistory(userId);

                            // Chuyển sang MainActivity và truyền role + userId
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userRole", document.getString("role"));
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Vai trò hoặc ID người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveLoginHistory(String userId) {
        // Hiển thị ProgressBar khi lưu lịch sử đăng nhập
        showProgressBar();

        firestore.collection("loginHistory")
                .orderBy("loginId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String newLoginId;

                    // Tự động tạo loginId mới nếu đã có dữ liệu
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot latestDocument = queryDocumentSnapshots.getDocuments().get(0);
                        String latestLoginId = latestDocument.getString("loginId");

                        if (latestLoginId != null && latestLoginId.startsWith("L")) {
                            int latestId = Integer.parseInt(latestLoginId.substring(1));
                            newLoginId = String.format("L%05d", latestId + 1);
                        } else {
                            newLoginId = "L00001";
                        }
                    } else {
                        newLoginId = "L00001";
                    }

                    long currentTimeMillis = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    String currentTime = sdf.format(new Date(currentTimeMillis));

                    LoginHistory loginHistory = new LoginHistory(newLoginId, currentTime, userId);

                    firestore.collection("loginHistory")
                            .document(newLoginId)
                            .set(loginHistory)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(LoginActivity.this, "Lịch sử đăng nhập đã được lưu với documentId: " + newLoginId, Toast.LENGTH_SHORT).show();

                                // Cập nhật lịch sử đăng nhập vào bảng "users"
                                updateUserLoginHistory(userId, currentTime);
                            })
                            .addOnFailureListener(e -> {
                                hideProgressBar();
                                Toast.makeText(LoginActivity.this, "Không thể lưu lịch sử đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    Toast.makeText(LoginActivity.this, "Lỗi khi kiểm tra lịch sử đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserLoginHistory(String userId, String loginTime) {
        firestore.collection("users")
                .document(userId)
                .update("historyLogin", com.google.firebase.firestore.FieldValue.arrayUnion(loginTime))
                .addOnSuccessListener(aVoid -> {
                    hideProgressBar();
                    Toast.makeText(LoginActivity.this, "Lịch sử đăng nhập của người dùng đã được cập nhật", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    Toast.makeText(LoginActivity.this, "Không thể cập nhật lịch sử đăng nhập của người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);  // Show the ProgressBar
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);  // Hide the ProgressBar
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtUserId.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imgTogglePassword.setImageResource(R.drawable.baseline_visibility_off_24);
        } else {
            edtUserId.setTransformationMethod(null);
            imgTogglePassword.setImageResource(R.drawable.baseline_visibility_24);
        }
        isPasswordVisible = !isPasswordVisible;
    }
}
