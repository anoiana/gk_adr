package com.example.ad_gk.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import com.example.ad_gk.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class importStudentData extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Button btnImportStudentData;
    private Button btnDowloadStudentData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_sutdent_data);
        btnImportStudentData = findViewById(R.id.btn_import_student_data);
        btnDowloadStudentData = findViewById(R.id.btn_dowload_student_data);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        btnImportStudentData.setOnClickListener(v -> chooseFile());
        btnDowloadStudentData.setOnClickListener(v -> {
            Intent intent = new Intent(this, exportStudentData.class);
            startActivityForResult(intent, 1); // Gọi Activity và đợi kết quả
        });
    }

    // Phương thức chọn file
    public void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Cho phép chọn tất cả các loại file
        startActivityForResult(Intent.createChooser(intent, "Select a File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData(); // Lấy URI của file được chọn
            String filePath = getFilePath(fileUri);

            if (filePath != null) {
                Log.d("FilePath", "Selected file path: " + filePath);

                // Xử lý file dựa trên loại
                 if (filePath.endsWith(".csv")) {
                    readCsvFile(fileUri);
                } else {
                    Toast.makeText(this, "File type not supported", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Hàm lấy đường dẫn file từ URI
    private String getFilePath(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                filePath = cursor.getString(index);
                cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }



    // Hàm đọc file CSV
    private void readCsvFile(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            reader.readLine(); // Đọc dòng đầu tiên nếu là header

            List<Task<Void>> tasks = new ArrayList<>(); // Danh sách các tác vụ cần chờ hoàn thành

            while ((line = reader.readLine()) != null && !line.isEmpty()) {

                // Phân tách mỗi dòng dữ liệu bằng dấu phẩy
                String[] data = line.split(",");
                if (data.length >= 6) {  // Đảm bảo tệp có đủ dữ liệu cho mỗi sinh viên
                    String name = data[0].trim();
                    int age = Integer.parseInt(data[1].trim()); // Chuyển đổi tuổi
                    String phoneNumber = data[2].trim();
                    String gender = data[3].trim();
                    String email = data[4].trim();
                    String address = data[5].trim();

                    // Lưu sinh viên vào Firestore và thêm Task vào danh sách
                    Task<Void> task = saveStudentToFirestoreFile(name, age, phoneNumber, gender, email, address);
                    tasks.add(task);
                } else {
                    Log.w("CSV Warning", "Dữ liệu không đầy đủ trong dòng: " + line);
                }
            }

            // Chờ tất cả các tác vụ Firebase hoàn thành
            Tasks.whenAllSuccess(tasks).addOnSuccessListener(aVoid -> {
                Log.d("Firestore", "Tất cả sinh viên đã được ghi vào Firestore.");
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Có lỗi xảy ra khi ghi dữ liệu vào Firestore.", e);
            });

        } catch (IOException e) {
            Log.e("Error", "Error reading file", e);
        }
    }

    private Task<Void> saveStudentToFirestoreFile(String name, int age, String phoneNumber, String gender, String email, String address) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> studentData = new HashMap<>();
        studentData.put("name", name);
        studentData.put("age", age);
        studentData.put("phoneNumber", phoneNumber);
        studentData.put("gender", gender);
        studentData.put("email", email);
        studentData.put("address", address);

        // Thêm sinh viên vào Firestore và trả về task
        return db.collection("students")
                .add(studentData)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();  // Nếu có lỗi, throw ra
                                    }

                    return null;  // Thành công
                });
    }

}
