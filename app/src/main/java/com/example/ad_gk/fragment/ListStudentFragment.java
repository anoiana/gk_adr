package com.example.ad_gk.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ad_gk.R;
import com.example.ad_gk.adapter.StudentAdapter;
import com.example.ad_gk.model.Student;

import java.util.ArrayList;
import java.util.List;


public class ListStudentFragment extends Fragment {

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private List<Student> studentList;

    public ListStudentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_student, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tạo danh sách sinh viên (thay thế bằng dữ liệu thực tế từ Firebase hoặc cơ sở dữ liệu)
        studentList = new ArrayList<>();
        studentList.add(new Student("Nguyễn Văn A", "123456"));
        studentList.add(new Student("Trần Thị B", "789012"));
        studentList.add(new Student("Lê Văn C", "345678"));

        studentAdapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(studentAdapter);

        return view;
    }
}
