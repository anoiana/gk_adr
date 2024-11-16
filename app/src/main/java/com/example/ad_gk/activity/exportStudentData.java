package com.example.ad_gk.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.example.ad_gk.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.CSVWriter;
import android.os.Environment; // Import lớp Environment

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class exportStudentData extends AppCompatActivity {

    private Button btnExoportToCsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_export_student_data);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        List<Map<String, Object>> dataList = new ArrayList<>();
        btnExoportToCsv = findViewById(R.id.btn_export_to_csv);
        btnExoportToCsv.setOnClickListener(v -> exportStudentsToCSV(dataList));
    }

    private FirebaseFirestore db;

    // Constructor để khởi tạo FirebaseFirestore instance
    public exportStudentData() {
        db = FirebaseFirestore.getInstance();
    }

    // Phương thức để xuất dữ liệu sinh viên từ Firestore ra file CSV
    public void exportStudentsToCSV(final List<Map<String, Object>> dataList) {
        // Lấy tất cả sinh viên từ Firestore
        db.collection("students")
                .get()  // Lấy toàn bộ tài liệu
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        if (querySnapshot != null) {
                            // Duyệt qua tất cả các tài liệu và thêm vào dataList
                            for (DocumentSnapshot document : querySnapshot) {
                                Map<String, Object> studentData = new HashMap<>();
                                studentData.put("name", document.getString("name"));
                                studentData.put("age", document.getLong("age"));
                                studentData.put("phoneNumber", document.getString("phoneNumber"));
                                studentData.put("gender", document.getString("gender"));
                                studentData.put("email", document.getString("email"));
                                studentData.put("address", document.getString("address"));

                                // Thêm dữ liệu sinh viên vào dataList
                                dataList.add(studentData);
                            }

                            // Sau khi lấy xong dữ liệu từ Firebase, gọi hàm exportToCSV
                            exportToCSV(dataList); // Gọi hàm exportToCSV để xuất dữ liệu ra file CSV
                        }
                    } else {
                        // Xử lý lỗi nếu không lấy được dữ liệu
                        Log.e("FirebaseHelper", "Error getting documents: ", task.getException());
                    }
                });
    }

    // Phương thức để xuất dữ liệu từ dataList ra CSV

    public void exportToCSV(List<Map<String, Object>> dataList) {
        // Hiển thị Toast khi bắt đầu xuất dữ liệu
        try {
            File appExternalDir = getExternalFilesDir(null);
            // Tạo đối tượng File cho file CSV
            File file = new File(appExternalDir , "output2.csv");

            // Đảm bảo thư mục đã tồn tại, nếu chưa thì tạo nó
            if (!appExternalDir .exists()) {
                appExternalDir .mkdirs();
            }

            // Tạo đối tượng FileWriter đ   ể ghi vào file
            FileWriter fileWriter = new FileWriter(file);

            // Tạo đối tượng CSVWriter để ghi vào file CSV
            CSVWriter csvWriter = new CSVWriter(fileWriter);

            // Ghi tiêu đề cột vào CSV
            csvWriter.writeNext(new String[]{"name", "age", "phoneNumber", "gender", "email", "address"});

            // Ghi dữ liệu từ dataList vào file CSV
            for (Map<String, Object> data : dataList) {
                String[] row = new String[data.size()];
                int i = 0;
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    row[i++] = entry.getValue().toString();  // Chuyển giá trị từ Object thành String
                }
                // Ghi dòng dữ liệu vào file CSV
                csvWriter.writeNext(row);
                Toast.makeText(this, "xuat file csv!", Toast.LENGTH_SHORT).show();


            }

            // Đảm bảo đóng file sau khi hoàn tất
            csvWriter.close();
            finish();
            Toast.makeText(this, "CSV file exported successfully!", Toast.LENGTH_SHORT).show();
            Log.d("FirebaseHelper", "CSV file exported successfully!");

        } catch (IOException e) {
            // Xử lý lỗi nếu có
            Log.e("FirebaseHelper", "Error exporting CSV: ", e);
            Toast.makeText(this, "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }
}
