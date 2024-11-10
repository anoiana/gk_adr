package com.example.ad_gk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ad_gk.R;
import com.example.ad_gk.model.Student;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList;

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView, idTextView;
        public ImageView imageView;

        public StudentViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_student_name);
            idTextView = itemView.findViewById(R.id.tv_student_id);
            imageView = itemView.findViewById(R.id.img_student);
        }
    }

    public StudentAdapter(List<Student> students) {
        this.studentList = students;
    }

    @Override
    public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.nameTextView.setText(student.getName());
        holder.idTextView.setText(student.getId());
        // Thêm logic để hiển thị ảnh sinh viên nếu cần
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }
}

