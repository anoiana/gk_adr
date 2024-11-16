package com.example.ad_gk.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ad_gk.R;
import com.example.ad_gk.model.Student;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddStudentActivity extends AppCompatActivity {

    private EditText editTextName, editTextAge, editTextPhoneNumber, editTextEmail, editTextAddress, editTextAverageScore;
    private Spinner spinnerGender;
    private LinearLayout checkboxContainer;
    private Button buttonSaveStudent;

    private List<String> certificates = new ArrayList<>(); // Danh sách chứng chỉ đã chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        // Khởi tạo các view
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextAverageScore = findViewById(R.id.editTextAverageScore); // Thêm trường điểm trung bình
        spinnerGender = findViewById(R.id.spinnerGender);
        checkboxContainer = findViewById(R.id.checkboxContainer);
        buttonSaveStudent = findViewById(R.id.buttonSaveStudent);

        // Thiết lập nội dung cho spinnerGender
        List<String> genderList = new ArrayList<>();
        genderList.add("Nam");
        genderList.add("Nữ");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderList);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Lấy danh sách chứng chỉ từ Firestore và hiển thị dưới dạng CheckBox
        loadCertificatesFromFirestore();

        // Lắng nghe sự kiện nhấn nút Save Student
        buttonSaveStudent.setOnClickListener(v -> saveStudentToFirestore());
    }

    private void loadCertificatesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("certificates")  // Giả sử bạn lưu chứng chỉ trong collection "certificates"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String certificateName = documentSnapshot.getString("name");
                        String certificateCode = documentSnapshot.getId();
                        if (certificateName != null && certificateCode != null) {
                            String certificateLabel = certificateName + " - " + certificateCode;

                            // Tạo CheckBox cho mỗi chứng chỉ và thêm vào checkboxContainer
                            CheckBox checkBox = new CheckBox(this);
                            checkBox.setText(certificateLabel);
                            checkboxContainer.addView(checkBox);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load certificates: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveStudentToFirestore() {
        // Lấy dữ liệu từ các trường nhập liệu khác
        String name = editTextName.getText().toString();
        int age = Integer.parseInt(editTextAge.getText().toString());
        String phoneNumber = editTextPhoneNumber.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();
        String email = editTextEmail.getText().toString();
        String address = editTextAddress.getText().toString();
        double averageScore = Double.parseDouble(editTextAverageScore.getText().toString()); // Đọc giá trị điểm trung bình

        // Lấy danh sách các chứng chỉ đã chọn
        certificates.clear();
        for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) checkboxContainer.getChildAt(i);
            if (checkBox.isChecked()) {
                // Lấy chuỗi văn bản của checkbox và tách phần mã
                String fullText = checkBox.getText().toString();
                String[] parts = fullText.split(" - "); // Tách chuỗi bằng dấu "-"
                if (parts.length > 1) {
                    String certificateCode = parts[1].trim(); // Phần mã là phần thứ hai trong chuỗi
                    certificates.add(certificateCode); // Thêm mã vào danh sách
                }
            }
        }

        // Truy vấn tất cả sinh viên và lấy Document ID có phần số lớn nhất
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String studentId;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        long maxNumber = 0; // Số lớn nhất ban đầu là 0
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String docId = document.getId();  // Lấy Document ID
                            if (docId.startsWith("ST")) { // Đảm bảo Document ID bắt đầu với "ST"
                                // Cắt phần số sau "ST"
                                String numberPart = docId.substring(2);
                                try {
                                    long currentNumber = Long.parseLong(numberPart);  // Chuyển đổi phần số thành long
                                    if (currentNumber > maxNumber) {
                                        maxNumber = currentNumber;  // Lưu lại số lớn nhất
                                    }
                                } catch (NumberFormatException e) {
                                    // Nếu phần số không thể chuyển thành long, bỏ qua tài liệu này
                                    continue;
                                }
                            }
                        }
                        // Tạo mã sinh viên mới từ số lớn nhất
                        studentId = generateNextStudentId(maxNumber);
                    } else {
                        // Nếu chưa có sinh viên, tạo mã đầu tiên "ST0001"
                        studentId = "ST0001";
                    }

                    // Tạo đối tượng Student với danh sách chứng chỉ đã chọn
                    Student student = new Student(studentId, name, age, phoneNumber, gender, email, address, certificates, averageScore);

                    // Lưu đối tượng vào Firestore
                    db.collection("students").document(studentId)
                            .set(student)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();

                                // Sau khi lưu sinh viên, cập nhật lại studentIds trong các chứng chỉ
                                updateCertificatesWithStudentId(studentId);
                                setResult(Activity.RESULT_OK);
                                finish(); // Đóng Activity hoặc chuyển đến một màn hình khác
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to add student: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCertificatesWithStudentId(String studentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Duyệt qua từng chứng chỉ và cập nhật studentIds
        for (String certificateCode : certificates) {
            db.collection("certificates").document(certificateCode)
                    .update("studentIds", FieldValue.arrayUnion(studentId)) // Thêm studentId vào studentIds
                    .addOnSuccessListener(aVoid -> {
                        // Nếu cập nhật thành công chứng chỉ này, bạn có thể xử lý thêm nếu cần.
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String generateNextStudentId(long maxNumber) {
        // Tăng số lên 1
        maxNumber++;

        // Tạo mã mới theo định dạng "STxxxx"
        return String.format("ST%04d", maxNumber); // Ví dụ: "ST0002"
    }
}
