package com.example.ad_gk.activity;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ad_gk.R;
import com.example.ad_gk.model.LoginHistory;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText edtRole, edtUserId;
    private Button btnLogin;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ các view từ layout
        edtRole = findViewById(R.id.edtEmai); // Sử dụng trường này để nhập role
        edtUserId = findViewById(R.id.edtPassword); // Sử dụng trường này để nhập userId
        btnLogin = findViewById(R.id.bntLogin);

        // Khởi tạo Firestore
        firestore = FirebaseFirestore.getInstance();

        // Thiết lập sự kiện click cho nút Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy thông tin từ EditText
                String role = edtRole.getText().toString().trim();
                String userId = edtUserId.getText().toString().trim();

                // Kiểm tra thông tin nhập vào
                if (TextUtils.isEmpty(role)) {
                    edtRole.setError("Role is required");
                    return;
                }

                if (TextUtils.isEmpty(userId)) {
                    edtUserId.setError("User ID is required");
                    return;
                }

                // Gọi hàm xác thực
                authenticateUser(role, userId);
            }
        });
    }

    private void authenticateUser(String role, String userId) {
        firestore.collection("users")
                .whereEqualTo("role", role)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Lưu thông tin đăng nhập
                            saveLoginHistory(userId);

                            // Chuyển sang MainActivity và truyền role + userId
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userRole", document.getString("role")); // Truyền role
                            intent.putExtra("userId", userId); // Truyền userId
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid role or user ID", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveLoginHistory(String userId) {
        // Tham chiếu đến bảng loginHistory
        firestore.collection("loginHistory")
                .orderBy("loginId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String newLoginId; // Mặc định là L00001 nếu chưa có bản ghi nào

                    // Tự động tạo loginId mới nếu đã có dữ liệu
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot latestDocument = queryDocumentSnapshots.getDocuments().get(0);
                        String latestLoginId = latestDocument.getString("loginId");

                        if (latestLoginId != null && latestLoginId.startsWith("L")) {
                            int latestId = Integer.parseInt(latestLoginId.substring(1)); // Lấy phần số sau 'L'
                            newLoginId = String.format("L%05d", latestId + 1); // Tăng thêm 1 và định dạng lại
                        } else {
                            newLoginId = "L00001";
                        }
                    } else {
                        newLoginId = "L00001";
                    }

                    // Lấy thời gian hiện tại (epoch) và chuyển đổi thành ngày tháng năm và giờ hiện tại
                    long currentTimeMillis = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    String currentTime = sdf.format(new Date(currentTimeMillis)); // Chuyển đổi epoch time thành định dạng ngày giờ

                    // Lưu thông tin vào Firestore với documentId trùng với loginId
                    LoginHistory loginHistory = new LoginHistory(newLoginId, currentTime, userId);

                    firestore.collection("loginHistory")
                            .document(newLoginId) // Đặt documentId trùng với loginId
                            .set(loginHistory) // Sử dụng set() để lưu dữ liệu
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(LoginActivity.this, "Login history saved with documentId: " + newLoginId, Toast.LENGTH_SHORT).show();

                                // Cập nhật lịch sử đăng nhập vào bảng "users"
                                updateUserLoginHistory(userId, currentTime);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(LoginActivity.this, "Failed to save login history: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "Error checking login history: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateUserLoginHistory(String userId, String loginTime) {
        // Tham chiếu đến bảng users và cập nhật lịch sử đăng nhập của người dùng
        firestore.collection("users")
                .document(userId)
                .update("historyLogin", com.google.firebase.firestore.FieldValue.arrayUnion(loginTime)) // Lưu chỉ loginTime vào mảng
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginActivity.this, "User login history updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Failed to update user login history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
