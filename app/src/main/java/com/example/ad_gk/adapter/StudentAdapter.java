package com.example.ad_gk.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.EditStudentActivity;
import com.example.ad_gk.activity.StudentDetailActivity;
import com.example.ad_gk.model.Student;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<Student> studentList;
    private FirebaseFirestore db;

    public StudentAdapter(List<Student> studentList) {
        this.studentList = studentList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.bind(student);

        // Thiết lập sự kiện xóa cho nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            deleteStudent(student.getStudentId(), holder.itemView.getContext());
        });

        // Thiết lập sự kiện chỉnh sửa cho nút edit
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EditStudentActivity.class);
            intent.putExtra("studentId", student.getStudentId());
            holder.itemView.getContext().startActivity(intent);
        });

        // Thiết lập sự kiện click cho toàn bộ item để mở Activity mới và truyền studentId
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), StudentDetailActivity.class); // Thay DetailActivity bằng Activity bạn muốn mở
            intent.putExtra("studentId", student.getStudentId()); // Truyền studentId sang Activity mới
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // Phương thức xóa sinh viên khỏi Firestore
    private void deleteStudent(String studentId, Context context) {
        // Xóa sinh viên từ Firestore
        db.collection("students").document(studentId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Nếu xóa thành công, xóa sinh viên khỏi danh sách và cập nhật RecyclerView
                    removeStudentFromList(studentId); // Gọi phương thức xóa khỏi danh sách
                    Toast.makeText(context, "Sinh viên đã được xóa", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa sinh viên", Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức xóa sinh viên khỏi danh sách
    private void removeStudentFromList(String studentId) {
        for (Student student : studentList) {
            if (student.getStudentId().equals(studentId)) {
                studentList.remove(student);
                break;
            }
        }
        notifyDataSetChanged(); // Làm mới RecyclerView sau khi xóa
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStudentId, tvName;
        private ImageView btnDelete, btnEdit; // Nút xóa và nút edit

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tv_studentId);
            tvName = itemView.findViewById(R.id.tv_name);
            btnDelete = itemView.findViewById(R.id.btn_delete); // Ánh xạ nút xóa
            btnEdit = itemView.findViewById(R.id.btn_edit); // Ánh xạ nút edit
        }

        public void bind(Student student) {
            tvStudentId.setText(student.getStudentId());
            tvName.setText(student.getName());
        }
    }
}
