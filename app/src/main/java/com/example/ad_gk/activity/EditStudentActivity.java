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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EditStudentActivity extends AppCompatActivity {
    private EditText editTextName, editTextAge, editTextPhoneNumber, editTextEmail, editTextAddress;
    private Spinner spinnerGender;
    private LinearLayout checkboxContainer;
    private Button buttonSaveStudent;

    private List<String> certificates = new ArrayList<>();
    private String studentId; // ID của sinh viên cần chỉnh sửa
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student); // Dùng lại layout của AddStudentActivity

        // Khởi tạo các view
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextAddress = findViewById(R.id.editTextAddress);
        spinnerGender = findViewById(R.id.spinnerGender);
        checkboxContainer = findViewById(R.id.checkboxContainer);
        buttonSaveStudent = findViewById(R.id.buttonSaveStudent);

        // Thiết lập Firebase và lấy studentId từ intent
        db = FirebaseFirestore.getInstance();
        studentId = getIntent().getStringExtra("studentId");

        // Thiết lập spinnerGender
        List<String> genderList = new ArrayList<>();
        genderList.add("Nam");
        genderList.add("Nữ");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderList);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Tải dữ liệu sinh viên và chứng chỉ
        loadStudentData();
        loadCertificatesFromFirestore();

        // Xử lý sự kiện nút lưu
        buttonSaveStudent.setOnClickListener(v -> updateStudentInFirestore());
    }

    private void loadStudentData() {
        // Lấy dữ liệu sinh viên từ Firestore
        db.collection("students").document(studentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Student student = documentSnapshot.toObject(Student.class);
                        if (student != null) {
                            editTextName.setText(student.getName());
                            editTextAge.setText(String.valueOf(student.getAge()));
                            editTextPhoneNumber.setText(student.getPhoneNumber());
                            editTextEmail.setText(student.getEmail());
                            editTextAddress.setText(student.getAddress());
                            spinnerGender.setSelection(student.getGender().equals("Nam") ? 0 : 1);
                            certificates = student.getCertificates();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải dữ liệu sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadCertificatesFromFirestore() {
        db.collection("certificates").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String certificateName = documentSnapshot.getString("name");
                        String certificateCode = documentSnapshot.getId();
                        if (certificateName != null && certificateCode != null) {
                            String certificateLabel = certificateName + " - " + certificateCode;
                            CheckBox checkBox = new CheckBox(this);
                            checkBox.setText(certificateLabel);

                            // Đánh dấu checkbox nếu sinh viên đã có chứng chỉ này
                            if (certificates.contains(certificateCode)) {
                                checkBox.setChecked(true);
                            }

                            checkboxContainer.addView(checkBox);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải chứng chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateStudentInFirestore() {
        // Lấy và kiểm tra dữ liệu từ các view
        String name = editTextName.getText().toString().trim();
        String ageText = editTextAge.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String email = editTextEmail.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if (name.isEmpty() || ageText.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Tuổi không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy danh sách chứng chỉ đã chọn
        certificates.clear();
        for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) checkboxContainer.getChildAt(i);
            if (checkBox.isChecked()) {
                String fullText = checkBox.getText().toString();
                String[] parts = fullText.split(" - ");
                if (parts.length > 1) {
                    String certificateCode = parts[1].trim();
                    certificates.add(certificateCode);
                }
            }
        }

        // Cập nhật dữ liệu sinh viên
        Student student = new Student(studentId, name, age, phoneNumber, gender, email, address, certificates);
        db.collection("students").document(studentId)
                .set(student)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật sinh viên thành công!", Toast.LENGTH_SHORT).show();
                    updateCertificatesWithStudentId(studentId); // Cập nhật chứng chỉ sau khi cập nhật sinh viên thành công
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateCertificatesWithStudentId(String studentId) {
        // Duyệt qua từng chứng chỉ và cập nhật studentIds
        StringBuilder errorMessages = new StringBuilder();
        for (String certificateCode : certificates) {
            db.collection("certificates").document(certificateCode)
                    .update("studentIds", FieldValue.arrayUnion(studentId)) // Thêm studentId vào studentIds
                    .addOnFailureListener(e -> {
                        // Gom lỗi vào StringBuilder nếu cập nhật thất bại
                        errorMessages.append("Failed to update certificate ")
                                .append(certificateCode).append(": ").append(e.getMessage()).append("\n");
                    });
        }

        // Kiểm tra và hiển thị lỗi nếu có
        if (errorMessages.length() > 0) {
            Toast.makeText(this, errorMessages.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "All certificates updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}

