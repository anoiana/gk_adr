package com.example.ad_gk.activity;

import android.app.Activity;
import android.content.Intent;
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
import com.example.ad_gk.R;
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AddUserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewProfilePicture;
    private EditText editTextName, editTextAge, editTextPhoneNumber;
    private Spinner spinnerStatus, spinnerRole;
    private Uri imageUri; // Lưu đường dẫn ảnh đã chọn

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

        // Set up các Spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.role_options, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Nút chọn ảnh
        Button buttonSelectProfilePicture = findViewById(R.id.buttonSelectProfilePicture);
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
            imageViewProfilePicture.setImageURI(imageUri); // Hiển thị ảnh đã chọn
        }
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
                        Toast.makeText(this, "Failed to get user data: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewUser(String userId) {
        // Lấy dữ liệu người dùng
        String name = editTextName.getText().toString();
        int age = Integer.parseInt(editTextAge.getText().toString());
        String phoneNumber = editTextPhoneNumber.getText().toString();
        String status = spinnerStatus.getSelectedItem().toString();
        String role = spinnerRole.getSelectedItem().toString();
        String profilePicture = (imageUri != null) ? imageUri.toString() : "";

        // Tạo đối tượng User
        User user = new User(userId, name, age, phoneNumber, status, role, profilePicture);

        // Thêm dữ liệu vào Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show();

                    // Trả kết quả về ListUserFragment
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }




}
