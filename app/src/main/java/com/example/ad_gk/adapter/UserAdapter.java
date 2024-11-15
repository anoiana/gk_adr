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
import com.example.ad_gk.activity.EditUserActivity;
import com.example.ad_gk.activity.UserDetailActivity; // Thêm activity chi tiết người dùng
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private String userRole;  // Biến lưu trữ role của người dùng

    // Constructor nhận vào danh sách người dùng
    public UserAdapter(List<User> userList, String userRole) {
        this.userList = userList;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);

        // Kiểm tra vai trò người dùng
        if ("Employee".equals(userRole) || "Manager".equals(userRole)) {
            // Nếu vai trò là Employee, ẩn các nút và sự kiện
            holder.btnDeleteUser.setVisibility(View.GONE);
            holder.btnEditUser.setVisibility(View.GONE);
            holder.itemView.setEnabled(false);  // Không thể nhấn vào item
        } else {
            // Nếu không phải Employee, hiển thị các nút và sự kiện
            holder.btnDeleteUser.setVisibility(View.VISIBLE);
            holder.btnEditUser.setVisibility(View.VISIBLE);

            // Xử lý sự kiện xóa
            holder.btnDeleteUser.setOnClickListener(v -> {
                deleteUserFromFirestore(user.getUserId(), holder.itemView);
            });

            // Sửa người dùng
            holder.btnEditUser.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), EditUserActivity.class);
                intent.putExtra("userId", user.getUserId());
                holder.itemView.getContext().startActivity(intent);
            });

            // Xem chi tiết người dùng
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), UserDetailActivity.class);
                intent.putExtra("userId", user.getUserId());
                holder.itemView.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Phương thức xóa người dùng từ Firestore
    private void deleteUserFromFirestore(String userId, View itemView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    loadUsersFromFirestore(itemView);
                    Toast.makeText(itemView.getContext(), "User deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(itemView.getContext(), "Error deleting user", Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức tải lại danh sách người dùng từ Firestore
    private void loadUsersFromFirestore(View itemView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> updatedUserList = queryDocumentSnapshots.toObjects(User.class);
                    userList.clear();
                    userList.addAll(updatedUserList);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(itemView.getContext(), "Error loading users", Toast.LENGTH_SHORT).show();
                });
    }

    // ViewHolder để ánh xạ các phần tử trong item_user.xml
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvUserRole;
        private TextView tvUserId;
        private ImageView btnDeleteUser, btnEditUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            btnDeleteUser = itemView.findViewById(R.id.btn_delete_user);
            btnEditUser = itemView.findViewById(R.id.btn_edit_user);
        }

        // Bind dữ liệu từ User object vào các view
        public void bind(User user) {
            tvUserName.setText(user.getName());
            tvUserRole.setText(user.getRole());
            tvUserId.setText(user.getUserId());
        }
    }
}
