package com.example.ad_gk.fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.AddStudentActivity;
import com.example.ad_gk.adapter.StudentAdapter;
import com.example.ad_gk.model.Student;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListStudentFragment extends Fragment {

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private List<Student> studentList;
    private FirebaseFirestore db;
    private ImageView btnAddStudent;
    private SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_student, container, false);

        // Khởi tạo Firestore
        btnAddStudent = view.findViewById(R.id.btn_add_student);
        db = FirebaseFirestore.getInstance();
        searchView = view.findViewById(R.id.searchView);

        // Thiết lập RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách và adapter
        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(studentAdapter);

        // Thiết lập sự kiện click cho nút add student
        btnAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddStudentActivity.class);
            startActivityForResult(intent, 1); // Gọi Activity và đợi kết quả
        });

        // Lấy dữ liệu từ Firestore và lắng nghe sự thay đổi
        loadStudentsFromFirestore();

        // Lắng nghe sự thay đổi của thanh tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Lọc khi nhấn Enter
                filterStudents(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Lọc khi nhập văn bản
                filterStudents(newText);
                return false;
            }
        });

        return view;
    }

    // Xử lý khi Activity trả kết quả
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            loadStudentsFromFirestore(); // Nếu thêm thành công, tải lại danh sách sinh viên
        }
    }

    // Phương thức để lắng nghe và tự động cập nhật dữ liệu từ Firestore
    private void loadStudentsFromFirestore() {
        CollectionReference studentsRef = db.collection("students");

        // Lắng nghe sự thay đổi trong Firestore
        studentsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Lỗi khi lấy dữ liệu từ Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                studentList.clear(); // Xóa dữ liệu cũ
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Student student = document.toObject(Student.class);
                    studentList.add(student);
                }
                studentAdapter.notifyDataSetChanged();
            }
        });
    }

    // Phương thức lọc sinh viên theo tên hoặc mã sinh viên
    private void filterStudents(String query) {
        CollectionReference studentsRef = db.collection("students");

        // Lọc dữ liệu trực tiếp từ Firestore khi có thay đổi
        studentsRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Lỗi khi lấy dữ liệu từ Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                studentList.clear(); // Xóa dữ liệu cũ
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Student student = document.toObject(Student.class);
                    // Kiểm tra xem tên hoặc mã sinh viên có chứa từ khóa tìm kiếm không
                    if (student.getName().toLowerCase().contains(query.toLowerCase()) ||
                            student.getStudentId().toLowerCase().contains(query.toLowerCase())) {
                        studentList.add(student);
                    }
                }
                studentAdapter.notifyDataSetChanged();
            }
        });
    }
}
