package com.example.ad_gk.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ad_gk.R;
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditUserActivity extends AppCompatActivity {

    private EditText editTextName, editTextAge, editTextPhoneNumber;
    private Spinner spinnerStatus, spinnerRole;
    private ImageView imageViewProfilePicture, buttonChangeProfilePicture;
    private Button buttonSave;
    private String userId;
    private String profilePictureBase64; // Biến để lưu ảnh cũ (Base64)
    private Uri selectedImageUri;
    private static final int MAX_WIDTH = 300; // Kích thước tối đa theo chiều rộng
    private static final int MAX_HEIGHT = 300; // Kích thước tối đa theo chiều cao

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

            // Convert age từ String sang int
            int updatedUserAge = 0;
            try {
                updatedUserAge = Integer.parseInt(editTextAge.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(EditUserActivity.this, "Vui lòng nhập tuổi hợp lệ", Toast.LENGTH_SHORT).show();
                return;  // Nếu không phải số hợp lệ, không tiếp tục
            }

            String updatedUserPhoneNumber = editTextPhoneNumber.getText().toString();
            String updatedUserStatus = spinnerStatus.getSelectedItem().toString();
            String updatedUserRole = spinnerRole.getSelectedItem().toString();

            // Kiểm tra xem người dùng có chọn ảnh mới không
            if (selectedImageUri != null) {
                // Nếu có ảnh mới, chuyển ảnh đó thành Base64 và lưu
                convertImageToBase64AndSave(selectedImageUri, updatedUserName, updatedUserAge, updatedUserPhoneNumber, updatedUserStatus, updatedUserRole);
            } else {
                // Nếu không có ảnh mới, sử dụng ảnh cũ đã lưu trong Firestore
                updateUserInFirestore(userId, updatedUserName, updatedUserAge, updatedUserPhoneNumber, updatedUserStatus, updatedUserRole, profilePictureBase64);
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
                            editTextAge.setText(String.valueOf(user.getAge()));  // Sửa thành String.valueOf(user.getAge()) để hiển thị
                            editTextPhoneNumber.setText(user.getPhoneNumber());
                            setUpSpinners(user.getStatus(), user.getRole());

                            // Lưu ảnh cũ để sử dụng sau này
                            profilePictureBase64 = user.getProfilePicture();  // Lưu ảnh cũ vào biến

                            // Hiển thị ảnh profile nếu có
                            if (profilePictureBase64 != null) {
                                // Chuyển đổi Base64 về Bitmap và hiển thị
                                byte[] decodedString = Base64.decode(profilePictureBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                imageViewProfilePicture.setImageBitmap(decodedByte);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditUserActivity.this, "Lỗi khi tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
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
                spinnerStatus.setSelection(0);  // Giá trị mặc định
            }
        }

        // Kiểm tra và cài đặt vị trí cho spinnerRole
        if (userRole != null && !userRole.isEmpty()) {
            int rolePosition = roleAdapter.getPosition(userRole);
            if (rolePosition != -1) {
                spinnerRole.setSelection(rolePosition);
            } else {
                spinnerRole.setSelection(0);  // Giá trị mặc định
            }
        }
    }

    private void convertImageToBase64AndSave(Uri imageUri, String updatedUserName, int updatedUserAge, String updatedUserPhoneNumber, String updatedUserStatus, String updatedUserRole) {
        try {
            // Chuyển đổi ảnh thành Bitmap
            Bitmap bitmap = rotateImageIfNeeded(resizeImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri)));
            // Chuyển đổi Bitmap thành Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Sử dụng JPEG để nén
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Cập nhật Firestore
            updateUserInFirestore(userId, updatedUserName, updatedUserAge, updatedUserPhoneNumber, updatedUserStatus, updatedUserRole, encodedImage);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi chuyển đổi ảnh thành Base64", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserInFirestore(String userId, String updatedUserName, int updatedUserAge, String updatedUserPhoneNumber, String updatedUserStatus, String updatedUserRole, String profilePictureBase64) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Nếu ảnh mới không có (profilePictureBase64 == null), sẽ lưu giá trị null cho ảnh cũ
        db.collection("users").document(userId)
                .update("name", updatedUserName,
                        "age", updatedUserAge,
                        "phoneNumber", updatedUserPhoneNumber,
                        "status", updatedUserStatus,
                        "role", updatedUserRole,
                        "profilePicture", profilePictureBase64)  // Lưu ảnh cũ hoặc Base64 của ảnh mới
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
                    finish();  // Quay lại Activity trước đó
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi cập nhật người dùng", Toast.LENGTH_SHORT).show();
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

    private Bitmap resizeImage(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Tính tỷ lệ thay đổi kích thước
        float aspectRatio = (float) width / height;
        int newWidth = MAX_WIDTH;
        int newHeight = MAX_HEIGHT;

        if (width > height) {
            newHeight = (int) (MAX_WIDTH / aspectRatio);
        } else if (width < height) {
            newWidth = (int) (MAX_HEIGHT * aspectRatio);
        }

        // Thay đổi kích thước ảnh theo tỷ lệ mới
        return Bitmap.createScaledBitmap(image, newWidth, newHeight, false);
    }

    private Bitmap rotateImageIfNeeded(Bitmap bitmap) {
        if (bitmap.getWidth() > bitmap.getHeight()) {
            // Xoay ảnh nếu chiều ngang lớn hơn chiều dọc
            Matrix matrix = new Matrix();
            matrix.postRotate(270);  // Xoay 270 độ
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;  // Nếu ảnh đã đứng, không cần xoay
    }
}
