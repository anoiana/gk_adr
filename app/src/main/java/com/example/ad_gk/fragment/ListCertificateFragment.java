package com.example.ad_gk.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.AddCertificateActivity;
import com.example.ad_gk.adapter.CertificateAdapter;
import com.example.ad_gk.model.Certificate;
import com.example.ad_gk.model.Student;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListCertificateFragment extends Fragment {

    private ImageView btnMoreOptions;
    private RecyclerView recyclerViewCertificate;
    private CertificateAdapter certificateAdapter;
    private List<Certificate> certificateList;
    private List<Certificate> filteredList;
    private String userRole;
    private ImageView addButton;// Danh sách để lưu trữ các kết quả tìm kiếm
    private static final int PICK_FILE_REQUEST = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_certificate, container, false);

        // Lấy giá trị role từ arguments
        if (getArguments() != null) {
            userRole = getArguments().getString("role"); // Nhận role từ Bundle
        }
        recyclerViewCertificate = view.findViewById(R.id.recyclerViewCertificate);
        recyclerViewCertificate.setLayoutManager(new LinearLayoutManager(getContext()));
        certificateList = new ArrayList<>();
        filteredList = new ArrayList<>(); // Khởi tạo danh sách kết quả tìm kiếm
        certificateAdapter = new CertificateAdapter(filteredList, userRole, new CertificateAdapter.OnDeleteClickListener() {
            @Override
            public void onDelete(Certificate certificate) {
                deleteCertificate(certificate); // Xóa chứng chỉ khi nhấn nút xóa
            }
        });
        recyclerViewCertificate.setAdapter(certificateAdapter);

        // Thiết lập SearchView để tìm kiếm chứng chỉ
        SearchView searchView = view.findViewById(R.id.searchViewCertificate);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCertificates(query); // Lọc khi nhấn nút tìm kiếm
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCertificates(newText); // Lọc khi thay đổi text
                return true;
            }
        });

        btnMoreOptions = view.findViewById(R.id.btn_more_options_certificate);
        btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_options_certificate, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_export_file) {
                    exportFile();
                    return true;
                } else if (item.getItemId() == R.id.menu_import_file) {
                    importFile();  // Kích hoạt chọn file
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        // Load certificates from Firestore when fragment is created
        loadCertificates();

        addButton = view.findViewById(R.id.btn_add_certificate);
        checkUserRole();
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddCertificateActivity.class);
                startActivityForResult(intent, 1); // Start AddCertificateActivity and expect result
            }
        });



        return view;
    }

    // Method to load certificates from Firestore
    private void loadCertificates() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference studentsRef = db.collection("certificates");

        studentsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Lỗi khi lấy dữ liệu từ Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                certificateList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Certificate certificate = document.toObject(Certificate.class);
                    certificateList.add(certificate);
                }
                filterCertificates("");
            }
        });
    }

    // Method to filter certificates based on the search keyword
    private void filterCertificates(String keyword) {
        filteredList.clear();
        for (Certificate certificate : certificateList) {
            if (certificate.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    certificate.getCertificateId().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(certificate);
            }
        }
        certificateAdapter.notifyDataSetChanged(); // Cập nhật lại danh sách hiển thị
    }

    private void importFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");  // Chỉ chọn file Excel
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }
    // Handle the result from AddCertificateActivity or EditCertificateActivity
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
                    db.collection("certificates")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                String certificateId = null;
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    long maxNumber = 0; // Số lớn nhất ban đầu là 0
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        String docId = document.getId();  // Lấy Document ID
                                        if (docId.startsWith("C")) { // Đảm bảo Document ID bắt đầu với "ST"
                                            // Cắt phần số sau "ST"
                                            String numberPart = docId.substring(1);
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
                                    certificateId = generateNextStudentId(maxNumber);
                                } else {
                                    // Nếu chưa có sinh viên, tạo mã đầu tiên "ST0001"
                                    certificateId = "C00001";
                                }

                                // Xử lý dữ liệu từ sheet (giống như bạn đã làm trước)
                                for (Row row : sheet) {
                                    if (row.getRowNum() > 0) { // Bỏ qua dòng tiêu đề
                                        String name = row.getCell(0).getStringCellValue();
                                        String issueDate = row.getCell(0).getStringCellValue();
                                        String issuer = row.getCell(2).getStringCellValue();

                                        String studentString = row.getCell(3).getStringCellValue();
                                        List<String> certificates = Arrays.asList(studentString.split(","));

                                        // Tạo đối tượng Student với studentId mới
                                        Certificate certificate = new Certificate(certificateId, name, issueDate, issuer, certificates);

                                        // Lưu đối tượng vào Firestore
                                        db.collection("certificates").document(certificateId)
                                                .set(certificate)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(requireContext(), "Nhập dữ liệu thành công!", Toast.LENGTH_SHORT).show();

                                                    // Cập nhật danh sách sinh viên
                                                    certificateList.add(certificate);
                                                    certificateAdapter.notifyDataSetChanged();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi khi thêm sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                                        // Tăng studentId lên 1 cho sinh viên tiếp theo
                                        certificateId = generateNextStudentId(Long.parseLong(certificateId.substring(2)));  // Tạo mã mới
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
        return String.format("C%05d", newNumber);
    }

    // Method to delete certificate from Firestore
    private void deleteCertificate(Certificate certificate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Xóa chứng chỉ từ Firestore
        db.collection("certificates").document(certificate.getCertificateId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa khỏi danh sách đã lọc và cập nhật RecyclerView
                    certificateAdapter.removeItem(certificate);
                    Toast.makeText(getContext(), "Certificate deleted successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserRole() {
        if ("Employee".equals(userRole)) {
            addButton.setVisibility(View.GONE); // Ẩn nút nếu vai trò là "employee"
        } else {
            addButton.setVisibility(View.VISIBLE); // Hiển thị nếu không phải employee
        }
    }

    private void exportFile() {
        // Tạo tên file Excel
        String fileName = "andeptrai.xlsx";
        FileOutputStream fos = null;
        XSSFWorkbook workbook = null;

        try {
            // Lấy thư mục "Download" từ bộ nhớ ngoài
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

            // Tạo Workbook Excel (XSSFWorkbook cho file .xlsx)
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Certificates");

            // Tạo tiêu đề cột
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Certificate ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Issue Date");
            headerRow.createCell(3).setCellValue("Issuer");
            headerRow.createCell(4).setCellValue("Student ID");


            // Lặp qua danh sách sinh viên và ghi dữ liệu vào file Excel
            int rowNum = 1; // Bắt đầu từ dòng thứ 2 vì dòng đầu là header
            for (Certificate certificate : certificateList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(certificate.getCertificateId());
                row.createCell(1).setCellValue(certificate.getName());
                row.createCell(2).setCellValue(certificate.getIssueDate());
                row.createCell(3).setCellValue(certificate.getIssuer());

                // Chuyển danh sách chứng chỉ thành một chuỗi (comma-separated)
                String certificates = String.join(", ", certificate.getStudentIds());
                row.createCell(4).setCellValue(certificates);
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
}
