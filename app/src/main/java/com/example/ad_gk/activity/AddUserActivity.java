package com.example.ad_gk.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddUserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewProfilePicture;
    private EditText editTextName, editTextAge, editTextPhoneNumber;
    private Spinner spinnerStatus, spinnerRole;
    private Uri imageUri; // Lưu đường dẫn ảnh đã chọn
    private static final int MAX_WIDTH = 400; // Kích thước tối đa theo chiều rộng
    private static final int MAX_HEIGHT = 400; // Kích thước tối đa theo chiều cao

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerRole = findViewById(R.id.spinnerRole);
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);

        // Cài đặt các Spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.role_options, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Nút chọn ảnh
        ImageView buttonSelectProfilePicture = findViewById(R.id.buttonSelectProfilePicture);
        buttonSelectProfilePicture.setOnClickListener(v -> openGallery());

        // Nút lưu người dùng
        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> saveUserToFirestore());
    }

    // Mở Gallery để chọn ảnh
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            if (imageUri != null) {
                try {
                    // Log để kiểm tra Uri
                    Bitmap bitmap = getBitmapFromUri(imageUri);
                    imageViewProfilePicture.setImageBitmap(bitmap); // Hiển thị ảnh đã chọn
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Đường dẫn ảnh bị null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Chuyển ảnh từ Uri thành Bitmap
    private Bitmap getBitmapFromUri(Uri imageUri) throws IOException {
        return rotateImageIfNeeded(resizeImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri)));
    }

    // Chuyển Bitmap thành Base64 String
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveUserToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy tất cả các user để tìm userId cao nhất
        db.collection("users")
                .orderBy("userId", Query.Direction.DESCENDING)  // Sắp xếp theo userId giảm dần
                .limit(1)  // Chỉ lấy 1 userId cao nhất
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Kiểm tra nếu không có tài liệu nào (tức là chưa có user trong Firestore)
                        if (task.getResult().isEmpty()) {
                            // Nếu không có user, bắt đầu với userId "U00001"
                            createNewUser("U00001");
                        } else {
                            // Nếu có userId, lấy userId cao nhất và tăng lên 1
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String lastUserId = document.getString("userId");

                            // Nếu đã có userId, tăng số lên 1; nếu chưa có userId, bắt đầu từ "U00001"
                            String userId;
                            if (lastUserId != null) {
                                int lastUserIdNumber = Integer.parseInt(lastUserId.substring(1));  // Lấy phần số từ "U00001"
                                userId = "U" + String.format("%05d", lastUserIdNumber + 1);  // Tăng số và tạo userId mới
                            } else {
                                userId = "U00001";  // Nếu chưa có user, bắt đầu từ "U00001"
                            }

                            // Tạo người dùng mới
                            createNewUser(userId);
                        }
                    } else {
                        Toast.makeText(this, "Lấy dữ liệu người dùng thất bại: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void createNewUser(String userId) {
        // Lấy dữ liệu người dùng
        String name = editTextName.getText().toString().trim();
        String ageText = editTextAge.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String role = spinnerRole.getSelectedItem().toString();

        // Kiểm tra các trường thông tin
        if (name.isEmpty()) {
            editTextName.setError("Vui lòng nhập tên");
            editTextName.requestFocus();
            return;
        }

        if (ageText.isEmpty()) {
            editTextAge.setError("Vui lòng nhập tuổi");
            editTextAge.requestFocus();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0) {
                editTextAge.setError("Tuổi phải lớn hơn 0");
                editTextAge.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editTextAge.setError("Tuổi phải là một số hợp lệ");
            editTextAge.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Vui lòng nhập số điện thoại");
            editTextPhoneNumber.requestFocus();
            return;
        }

        if (phoneNumber.length() < 10 || phoneNumber.length() > 11) {
            editTextPhoneNumber.setError("Số điện thoại phải từ 10 đến 11 chữ số");
            editTextPhoneNumber.requestFocus();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển ảnh thành Base64 String
        String profilePictureBase64 = "";
        try {
            Bitmap bitmap = getBitmapFromUri(imageUri);
            profilePictureBase64 = convertBitmapToBase64(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi chuyển ảnh thành Base64", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng User
        User user = new User(userId, name, age, phoneNumber, status, role, profilePictureBase64);

        // Thêm dữ liệu vào Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Người dùng đã được thêm thành công!", Toast.LENGTH_SHORT).show();

                    // Trả kết quả về ListUserFragment
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi thêm người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
