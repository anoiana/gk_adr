package com.example.ad_gk.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.LoginActivity;
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1; // Mã yêu cầu để chọn ảnh

    private TextView textViewName, textViewAge, textViewPhoneNumber, textViewStatus, textViewRole, textViewLoginHistory;
    private ImageView imageViewProfilePicture, imageViewChangeProfilePicture, imgLogout;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Nhận userId từ Bundle
        if (getArguments() != null) {
            userId = getArguments().getString("userId"); // Nhận userId từ Bundle
        }
        // Lấy các UI elements
        textViewName = view.findViewById(R.id.textViewName);
        textViewAge = view.findViewById(R.id.textViewAge);
        textViewPhoneNumber = view.findViewById(R.id.textViewPhoneNumber);
        textViewStatus = view.findViewById(R.id.textViewStatus);
        textViewRole = view.findViewById(R.id.textViewRole);
        textViewLoginHistory = view.findViewById(R.id.textViewLoginHistory);
        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        imageViewChangeProfilePicture = view.findViewById(R.id.imageViewEditProfile);
        imgLogout = view.findViewById(R.id.buttonLogout);

        // Thiết lập sự kiện cho việc thay đổi ảnh đại diện
        imageViewChangeProfilePicture.setOnClickListener(v -> openImagePicker());
        imgLogout.setOnClickListener(v -> logout());
        // Lấy dữ liệu người dùng từ Firestore
        loadUserData(userId);

        return view;
    }

    private void logout() {
        // Xóa session hoặc token nếu có (nếu bạn sử dụng FirebaseAuth hoặc lưu trữ session)
        // Ví dụ: FirebaseAuth.getInstance().signOut(); nếu bạn sử dụng Firebase Authentication

        // Chuyển về LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Đảm bảo rằng khi quay lại không thể quay lại ProfileFragment
        startActivity(intent);
        getActivity().finish(); // Kết thúc activity hiện tại
    }

    // Hàm mở Image Picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*"); // Chỉ chọn ảnh
        startActivityForResult(intent, PICK_IMAGE_REQUEST); // Mở activity chọn ảnh
    }

    // Khi người dùng chọn ảnh, xử lý kết quả
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            // Lấy ảnh từ URI
            try {
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                imageViewProfilePicture.setImageBitmap(selectedImage); // Hiển thị ảnh đã chọn

                // Chuyển ảnh thành Base64 để lưu vào Firestore
                String encodedImage = encodeImageToBase64(selectedImage);

                // Cập nhật ảnh lên Firestore
                updateProfilePictureInFirestore(encodedImage);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error selecting image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Hàm chuyển đổi ảnh thành Base64 string
    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream); // Nén ảnh
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT); // Chuyển đổi byte array thành Base64 string
    }

    // Cập nhật ảnh đại diện lên Firestore
    private void updateProfilePictureInFirestore(String encodedImage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);

        // Cập nhật trường ảnh đại diện trong Firestore
        docRef.update("profilePicture", encodedImage)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error updating profile picture", Toast.LENGTH_SHORT).show();
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
                    getActivity().runOnUiThread(() -> {
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

    // Hàm lấy dữ liệu người dùng từ Firestore
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
                            textViewName.setText(user.getName());
                            textViewAge.setText( " "+ user.getAge());
                            textViewPhoneNumber.setText(user.getPhoneNumber());
                            textViewStatus.setText(user.getStatus());
                            textViewRole.setText(user.getRole());
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
                        Toast.makeText(getActivity(), "No such user found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error loading user data", Toast.LENGTH_SHORT).show();
                });
    }
}
