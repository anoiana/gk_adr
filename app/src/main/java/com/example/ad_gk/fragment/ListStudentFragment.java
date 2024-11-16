package com.example.ad_gk.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.AddStudentActivity;
import com.example.ad_gk.adapter.StudentAdapter;
import com.example.ad_gk.model.Student;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
public class ListStudentFragment extends Fragment {

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private List<Student> studentList;
    private FirebaseFirestore db;
    private ImageView btnAddStudent;
    private ImageView btnMoreOptions;
    private SearchView searchView;
    private String userRole;

    private static final int PICK_FILE_REQUEST = 1001;  // Mã yêu cầu mở file

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_student, container, false);

        if (getArguments() != null) {
            userRole = getArguments().getString("role");
        }

        db = FirebaseFirestore.getInstance();
        btnAddStudent = view.findViewById(R.id.btn_add_student);
        btnMoreOptions = view.findViewById(R.id.btn_more_options);
        searchView = view.findViewById(R.id.searchView);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList, userRole);
        recyclerView.setAdapter(studentAdapter);

        checkUserRole();

        btnAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddStudentActivity.class);
            startActivityForResult(intent, 1);
        });

        btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_export_file) {
                    exportFile();
                    return true;
                } else if (item.getItemId() == R.id.menu_import_file) {
                    importFile();  // Kích hoạt chọn file
                    return true;
                } else if (item.getItemId() == R.id.menu_sort_by_avg_score) {
                    sortStudentsByAverageScore();
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        loadStudentsFromFirestore();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterStudents(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStudents(newText);
                return false;
            }
        });

        return view;
    }

    private void importFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");  // Chỉ chọn file Excel
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                try {
                    // Sử dụng ContentResolver để mở InputStream từ Uri
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    InputStream inputStream = contentResolver.openInputStream(fileUri);

                    // Mở workbook từ InputStream
                    XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                    Sheet sheet = workbook.getSheetAt(0);

                    // Truy vấn tất cả sinh viên và lấy Document ID có phần số lớn nhất
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("students")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                String studentId = null;
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

                                // Xử lý dữ liệu từ sheet (giống như bạn đã làm trước)
                                for (Row row : sheet) {
                                    if (row.getRowNum() > 0) { // Bỏ qua dòng tiêu đề
                                        String name = row.getCell(0).getStringCellValue();
                                        int age = (int) row.getCell(1).getNumericCellValue();
                                        String phoneNumber = row.getCell(2).getStringCellValue();
                                        String gender = row.getCell(3).getStringCellValue();
                                        String email = row.getCell(4).getStringCellValue();
                                        String address = row.getCell(5).getStringCellValue();

                                        String certificatesString = row.getCell(6).getStringCellValue();
                                        List<String> certificates = Arrays.asList(certificatesString.split(","));

                                        double averageScore = row.getCell(7).getNumericCellValue();

                                        // Tạo đối tượng Student với studentId mới
                                        Student student = new Student(studentId, name, age, phoneNumber, gender, email, address, certificates, averageScore);

                                        // Lưu đối tượng vào Firestore
                                        db.collection("students").document(studentId)
                                                .set(student)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(requireContext(), "Nhập dữ liệu thành công!", Toast.LENGTH_SHORT).show();

                                                    // Cập nhật danh sách sinh viên
                                                    studentList.add(student);
                                                    studentAdapter.notifyDataSetChanged();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi khi thêm sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                                        // Tăng studentId lên 1 cho sinh viên tiếp theo
                                        studentId = generateNextStudentId(Long.parseLong(studentId.substring(2)));  // Tạo mã mới
                                    }
                                }
                                // Đóng InputStream và Workbook
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    workbook.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Lỗi khi lấy dữ liệu sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } catch (IOException e) {
                    Toast.makeText(requireContext(), "Lỗi khi mở file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Hàm để tạo studentId mới từ số lớn nhất
    private String generateNextStudentId(long maxNumber) {
        long newNumber = maxNumber + 1;
        return String.format("ST%04d", newNumber);  // Tạo mã sinh viên với 4 chữ số (ST0001, ST0002, ...)
    }



    private void exportFile() {
        // Tạo tên file Excel
        String fileName = "listStudent.xlsx";
        FileOutputStream fos = null;
        XSSFWorkbook workbook = null;

        try {
            // Lấy thư mục "Download" từ bộ nhớ ngoài
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

            // Tạo Workbook Excel (XSSFWorkbook cho file .xlsx)
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Students");

            // Tạo tiêu đề cột
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Student ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Age");
            headerRow.createCell(3).setCellValue("Phone Number");
            headerRow.createCell(4).setCellValue("Gender");
            headerRow.createCell(5).setCellValue("Email");
            headerRow.createCell(6).setCellValue("Address");
            headerRow.createCell(7).setCellValue("Certificates");
            headerRow.createCell(8).setCellValue("Average Score");

            // Lặp qua danh sách sinh viên và ghi dữ liệu vào file Excel
            int rowNum = 1; // Bắt đầu từ dòng thứ 2 vì dòng đầu là header
            for (Student student : studentList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(student.getAge());
                row.createCell(3).setCellValue(student.getPhoneNumber());
                row.createCell(4).setCellValue(student.getGender());
                row.createCell(5).setCellValue(student.getEmail());
                row.createCell(6).setCellValue(student.getAddress());

                // Chuyển danh sách chứng chỉ thành một chuỗi (comma-separated)
                String certificates = String.join(", ", student.getCertificates());
                row.createCell(7).setCellValue(certificates);

                row.createCell(8).setCellValue(student.getAverageScore());
            }

            // Ghi Workbook vào file
            fos = new FileOutputStream(file);
            workbook.write(fos);

            Toast.makeText(requireContext(), "Xuất file thành công! File lưu tại thư mục Download.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (fos != null) fos.close();
                if (workbook != null) workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadStudentsFromFirestore() {
        CollectionReference studentsRef = db.collection("students");

        studentsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Lỗi khi lấy dữ liệu từ Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                studentList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Student student = document.toObject(Student.class);
                    studentList.add(student);
                }
                studentAdapter.notifyDataSetChanged();
            }
        });
    }

    private void filterStudents(String query) {
        CollectionReference studentsRef = db.collection("students");

        studentsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Lỗi khi lấy dữ liệu từ Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                studentList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Student student = document.toObject(Student.class);
                    if (student.getName().toLowerCase().contains(query.toLowerCase())) {
                        studentList.add(student);
                    }
                }
                studentAdapter.notifyDataSetChanged();
            }
        });
    }

    private void checkUserRole() {
        // Kiểm tra và hiển thị các nút dựa trên vai trò người dùng
        if ("Employee".equals(userRole)) {
            btnAddStudent.setVisibility(View.GONE);
            btnMoreOptions.setVisibility(View.GONE);
        } else {
            btnAddStudent.setVisibility(View.VISIBLE);
        }
    }

    private void sortStudentsByAverageScore() {
        CollectionReference studentsRef = db.collection("students");

        // Lấy dữ liệu và sắp xếp theo điểm trung bình
        studentsRef.orderBy("averageScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        if (queryDocumentSnapshots != null) {
                            studentList.clear(); // Xóa dữ liệu cũ

                            // Thêm dữ liệu đã sắp xếp vào danh sách sinh viên
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Student student = document.toObject(Student.class);
                                studentList.add(student);
                            }

                            // Cập nhật lại RecyclerView
                            studentAdapter.notifyDataSetChanged();

                            Toast.makeText(requireContext(), "Danh sách đã được xếp hạng!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Lỗi khi lấy dữ liệu từ Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

