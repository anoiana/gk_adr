package com.example.ad_gk.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ad_gk.R;
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Base64; // Dùng android.util.Base64

import java.util.List;

public class UserDetailActivity extends AppCompatActivity {

    private TextView textViewName, textViewAge, textViewPhoneNumber, textViewStatus, textViewRole, textViewLoginHistory;
    private ImageView imageViewProfilePicture;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // Lấy thông tin User ID từ Intent
        userId = getIntent().getStringExtra("userId");

        // Lấy các UI elements
        textViewName = findViewById(R.id.textViewName);
        textViewAge = findViewById(R.id.textViewAge);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewRole = findViewById(R.id.textViewRole);
        textViewLoginHistory = findViewById(R.id.textViewLoginHistory);
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);
        // Lấy dữ liệu người dùng từ Firestore hoặc cơ sở dữ liệu và hiển thị
        loadUserData(userId);
    }

    private void loadUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);

        // Lấy thông tin người dùng từ Firestore
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Lấy User từ dữ liệu Firestore
                        User user = documentSnapshot.toObject(User.class);

                        // Cập nhật UI với thông tin người dùng
                        if (user != null) {
                            textViewName.setText("Name: " + user.getName());
                            textViewAge.setText("Age: " + user.getAge());
                            textViewPhoneNumber.setText("Phone Number: " + user.getPhoneNumber());
                            textViewStatus.setText("Status: " + user.getStatus());
                            textViewRole.setText("Role: " + user.getRole());

                            // Xử lý Login History
                            if (user.getHistoryLogin() != null && !user.getHistoryLogin().isEmpty()) {
                                displayLoginHistory(user.getHistoryLogin());
                            } else {
                                textViewLoginHistory.setText("No login history available.");
                            }

                            // Kiểm tra và xử lý ảnh đại diện
                            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                                Bitmap bitmap = decodeBase64(user.getProfilePicture());
                                if (bitmap != null) {
                                    imageViewProfilePicture.setImageBitmap(bitmap);
                                } else {
                                    imageViewProfilePicture.setImageResource(R.drawable.default_profile_picture);
                                }
                            } else {
                                imageViewProfilePicture.setImageResource(R.drawable.default_profile_picture);
                            }
                        }
                    } else {
                        Toast.makeText(UserDetailActivity.this, "No such user found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserDetailActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm xử lý hiển thị dần dần lịch sử đăng nhập
    private void displayLoginHistory(List<String> historyLogin) {
        StringBuilder historyText = new StringBuilder();

        // Duyệt qua từng phần tử của historyLogin và thêm vào TextView
        new Thread(() -> {
            for (String login : historyLogin) {
                try {
                    // Chờ một khoảng thời gian (mô phỏng tải dữ liệu từng phần)
                    Thread.sleep(500);  // Để thấy được hiệu ứng "tải" từng mục (500ms)

                    // Chạy trên UI thread để cập nhật TextView
                    runOnUiThread(() -> {
                        historyText.append(login).append("\n");
                        textViewLoginHistory.setText(historyText.toString());
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Hàm chuyển đổi Base64 thành Bitmap
    private Bitmap decodeBase64(String base64String) {
        try {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT); // Sử dụng android.util.Base64
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); // Chuyển đổi byte array thành Bitmap
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null; // Nếu có lỗi, trả về null
        }
    }
}
