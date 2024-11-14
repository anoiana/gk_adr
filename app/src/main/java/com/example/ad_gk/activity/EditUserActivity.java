package com.example.ad_gk.activity;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ad_gk.R;
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class EditUserActivity extends AppCompatActivity {

    private EditText editTextName, editTextAge, editTextPhoneNumber;
    private Spinner spinnerStatus, spinnerRole;
    private ImageView imageViewProfilePicture;
    private Button buttonSave, buttonChangeProfilePicture;
    private String userId;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        // Ánh xạ các view
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerRole = findViewById(R.id.spinnerRole);
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);
        buttonSave = findViewById(R.id.buttonSave);
        buttonChangeProfilePicture = findViewById(R.id.buttonSelectProfilePicture);

        // Lấy userId từ Intent
        userId = getIntent().getStringExtra("userId");

        // Tải thông tin người dùng từ Firestore
        loadUserData(userId);

        // Sự kiện lưu thông tin người dùng
        buttonSave.setOnClickListener(v -> {
            String updatedUserName = editTextName.getText().toString();
            String updatedUserAge = editTextAge.getText().toString();
            String updatedUserPhoneNumber = editTextPhoneNumber.getText().toString();
            String updatedUserStatus = spinnerStatus.getSelectedItem().toString();
            String updatedUserRole = spinnerRole.getSelectedItem().toString();

            // Cập nhật dữ liệu xuống Firestore
            if (selectedImageUri != null) {
                uploadProfilePicture(selectedImageUri, updatedUserName, updatedUserAge, updatedUserPhoneNumber, updatedUserStatus, updatedUserRole);
            } else {
                updateUserInFirestore(userId, updatedUserName, updatedUserAge, updatedUserPhoneNumber, updatedUserStatus, updatedUserRole, null);
            }
        });

        // Chọn ảnh mới
        buttonChangeProfilePicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);  // 1 là request code cho việc chọn ảnh
        });
    }

    private void loadUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Hiển thị dữ liệu lên các trường nhập liệu
                            editTextName.setText(user.getName());
                            editTextAge.setText(String.valueOf(user.getAge())); // Chuyển thành chuỗi
                            editTextPhoneNumber.setText(user.getPhoneNumber());
                            setUpSpinners(user.getStatus(), user.getRole());

                            // Hiển thị ảnh profile nếu có
                            if (user.getProfilePicture() != null) {
                                Glide.with(this).load(user.getProfilePicture()).into(imageViewProfilePicture);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditUserActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setUpSpinners(String userStatus, String userRole) {
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.role_options, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Kiểm tra và cài đặt vị trí cho spinnerStatus
        if (userStatus != null && !userStatus.isEmpty()) {
            int statusPosition = statusAdapter.getPosition(userStatus);
            if (statusPosition != -1) {
                spinnerStatus.setSelection(statusPosition);
            } else {
                // Nếu không tìm thấy, có thể chọn giá trị mặc định
                spinnerStatus.setSelection(0);
            }
        }

        // Kiểm tra và cài đặt vị trí cho spinnerRole
        if (userRole != null && !userRole.isEmpty()) {
            int rolePosition = roleAdapter.getPosition(userRole);
            if (rolePosition != -1) {
                spinnerRole.setSelection(rolePosition);
            } else {
                // Nếu không tìm thấy, có thể chọn giá trị mặc định
                spinnerRole.setSelection(0);
            }
        }
    }


    private void uploadProfilePicture(Uri imageUri, String updatedUserName, String updatedUserAge, String updatedUserPhoneNumber, String updatedUserStatus, String updatedUserRole) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            updateUserInFirestore(userId, updatedUserName, updatedUserAge, updatedUserPhoneNumber, updatedUserStatus, updatedUserRole, uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInFirestore(String userId, String updatedUserName, String updatedUserAge, String updatedUserPhoneNumber, String updatedUserStatus, String updatedUserRole, String profilePictureUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .update("name", updatedUserName,
                        "age", updatedUserAge,
                        "phoneNumber", updatedUserPhoneNumber,
                        "status", updatedUserStatus,
                        "role", updatedUserRole,
                        "profilePicture", profilePictureUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                    finish();  // Quay lại Activity trước đó
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating user", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imageViewProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
