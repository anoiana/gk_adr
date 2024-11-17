package com.example.ad_gk.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.LoginActivity;
import com.example.ad_gk.activity.MainActivity;
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1; // Mã yêu cầu để chọn ảnh
    private static final int MAX_WIDTH = 400; // Kích thước tối đa theo chiều rộng
    private static final int MAX_HEIGHT = 400; // Kích thước tối đa theo chiều cao

    private TextView textViewName, textViewAge, textViewPhoneNumber, textViewStatus, textViewRole, textViewLoginHistory;
    private ImageView imageViewProfilePicture, imageViewChangeProfilePicture, imgLogout;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Nhận userId từ Bundle
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }



        // Ánh xạ các thành phần giao diện người dùng
        initializeUI(view);

        // Sự kiện thay đổi ảnh đại diện
        imageViewChangeProfilePicture.setOnClickListener(v -> openImagePicker());

        // Sự kiện đăng xuất
        imgLogout.setOnClickListener(v -> logout());

        // Lấy dữ liệu người dùng từ Firestore
        if (userId != null) {
            loadUserData(userId);
        } else {
            Toast.makeText(getActivity(), "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void initializeUI(View view) {
        textViewName = view.findViewById(R.id.textViewName);
        textViewAge = view.findViewById(R.id.textViewAge);
        textViewPhoneNumber = view.findViewById(R.id.textViewPhoneNumber);
        textViewStatus = view.findViewById(R.id.textViewStatus);
        textViewRole = view.findViewById(R.id.textViewRole);
        textViewLoginHistory = view.findViewById(R.id.textViewLoginHistory);
        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        imageViewChangeProfilePicture = view.findViewById(R.id.imageViewEditProfile);
        imgLogout = view.findViewById(R.id.buttonLogout);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            try {
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                // Giới hạn kích thước ảnh
                Bitmap resizedImage = rotateImageIfNeeded(resizeImage(selectedImage));
                imageViewProfilePicture.setImageBitmap(resizedImage); // Hiển thị ảnh đã chọn và đã thay đổi kích thước
                encodeImageToBase64(resizedImage); // Encode ảnh đã thay đổi kích thước
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Lỗi khi chọn ảnh", Toast.LENGTH_SHORT).show();
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

    private void encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        updateProfilePictureInFirestore(encodedImage);
    }

    private void updateProfilePictureInFirestore(String encodedImage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);

        docRef.update("profilePicture", encodedImage)
                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Lỗi khi cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show());
    }

    private void displayLoginHistory(List<String> historyLogin) {
        StringBuilder historyText = new StringBuilder();
        new Thread(() -> {
            for (String login : historyLogin) {
                getActivity().runOnUiThread(() -> {
                    historyText.append(login).append("\n");
                    textViewLoginHistory.setText(historyText.toString());
                });
            }
        }).start();
    }

    private void loadUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);

        // Lắng nghe thay đổi dữ liệu trong thời gian thực
        docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(getActivity(), "Lỗi khi tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    updateUI(user);
                }
            } else {
                Toast.makeText(getActivity(), "Không tìm thấy người dùng này", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUI(User user) {
        // Thêm dữ liệu vào Intent
        textViewName.setText(user.getName());
        textViewAge.setText(String.valueOf(user.getAge()));
        textViewPhoneNumber.setText(user.getPhoneNumber());
        textViewStatus.setText(user.getStatus());
        textViewRole.setText(user.getRole());

        // Hiển thị lịch sử đăng nhập
        if (user.getHistoryLogin() != null && !user.getHistoryLogin().isEmpty()) {
            displayLoginHistory(user.getHistoryLogin());
        } else {
            textViewLoginHistory.setText("Không có lịch sử đăng nhập.");
        }

        // Hiển thị ảnh đại diện
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            Bitmap bitmap = decodeBase64(user.getProfilePicture());
            if (bitmap != null) {
                imageViewProfilePicture.setImageBitmap(bitmap);
            } else {
                imageViewProfilePicture.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            imageViewProfilePicture.setImageResource(R.drawable.ic_placeholder);
        }
    }


    private Bitmap decodeBase64(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void logout() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
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
