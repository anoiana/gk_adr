package com.example.ad_gk.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ad_gk.R;
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDetailActivity extends AppCompatActivity {

    private TextView textViewName, textViewAge, textViewPhoneNumber, textViewStatus, textViewRole;
    private ImageView imageViewProfilePicture;
    private String userId;
    Button btnBack;
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
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

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


                            // Dùng Glide để tải ảnh đại diện
                            Glide.with(this)
                                    .load(user.getProfilePicture()) // Đường dẫn ảnh
                                    .into(imageViewProfilePicture);
                        }
                    } else {
                        Toast.makeText(UserDetailActivity.this, "No such user found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserDetailActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                });
    }
}
