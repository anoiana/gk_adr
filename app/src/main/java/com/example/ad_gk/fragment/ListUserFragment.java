package com.example.ad_gk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.AddUserActivity;
import com.example.ad_gk.adapter.UserAdapter;
import com.example.ad_gk.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListUserFragment extends Fragment {

    private RecyclerView recyclerViewUser;
    private UserAdapter userAdapter;
    private List<User> userList;
    private List<User> filteredList;  // Danh sách để lưu trữ các kết quả tìm kiếm
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable filterRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_user, container, false);

        // Ánh xạ RecyclerView và nút add
        recyclerViewUser = view.findViewById(R.id.recyclerViewUser);
        ImageView addButton = view.findViewById(R.id.btn_add);
        SearchView searchView = view.findViewById(R.id.searchViewUser);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);  // ProgressBar

        // Khởi tạo danh sách người dùng
        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        userAdapter = new UserAdapter(filteredList);

        recyclerViewUser.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUser.setAdapter(userAdapter);

        // Thiết lập SearchView để lọc người dùng
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query); // Lọc người dùng khi nhấn nút tìm kiếm
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText); // Lọc người dùng khi thay đổi text
                return true;
            }
        });

        // Thiết lập sự kiện click cho nút add để chuyển sang AddUserActivity
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddUserActivity.class);
            startActivity(intent);
        });

        // Tải dữ liệu người dùng từ Firestore
        loadUsersFromFirestore(progressBar);

        return view;
    }

    // Tải danh sách người dùng từ Firestore
    private void loadUsersFromFirestore(ProgressBar progressBar) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .addSnapshotListener((documents, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documents != null) {
                        userList.clear();
                        for (DocumentSnapshot document : documents) {
                            String userId = document.getId();
                            String name = document.getString("name");
                            int age = document.getLong("age").intValue();
                            String phoneNumber = document.getString("phoneNumber");
                            String status = document.getString("status");
                            String role = document.getString("role");
                            String profilePicture = document.getString("profilePicture");

                            User user = new User(userId, name, age, phoneNumber, status, role, profilePicture);
                            userList.add(user);
                        }
                        filterUsers("");  // Cập nhật danh sách khi có thay đổi
                        progressBar.setVisibility(View.GONE);  // Ẩn ProgressBar khi tải xong
                    }
                });
    }

    // Lọc danh sách người dùng theo tên hoặc ID
    private void filterUsers(String keyword) {
        if (filterRunnable != null) {
            handler.removeCallbacks(filterRunnable);
        }

        filterRunnable = () -> {
            filteredList.clear();
            for (User user : userList) {
                if (user.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        user.getUserId().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredList.add(user);
                }
            }
            userAdapter.notifyDataSetChanged(); // Cập nhật lại RecyclerView
        };

        handler.postDelayed(filterRunnable, 300);  // Đợi 300ms trước khi lọc lại
    }
}
