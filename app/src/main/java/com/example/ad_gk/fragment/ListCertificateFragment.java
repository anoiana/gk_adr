package com.example.ad_gk.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.AddCertificateActivity;
import com.example.ad_gk.adapter.CertificateAdapter;
import com.example.ad_gk.model.Certificate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListCertificateFragment extends Fragment {

    private RecyclerView recyclerViewCertificate;
    private CertificateAdapter certificateAdapter;
    private List<Certificate> certificateList;
    private List<Certificate> filteredList;
    private String userRole;
    private  ImageView addButton;// Danh sách để lưu trữ các kết quả tìm kiếm

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
        db.collection("certificates")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load certificates: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        certificateList.clear(); // Xóa dữ liệu cũ trong danh sách
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            String certificateId = document.getId();
                            String certificateName = document.getString("name");

                            // Thêm chứng chỉ vào danh sách
                            certificateList.add(new Certificate(certificateId, certificateName, "", "", new ArrayList<>()));
                        }
                        filterCertificates(""); // Cập nhật danh sách hiển thị với tất cả chứng chỉ
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

    // Handle the result from AddCertificateActivity or EditCertificateActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            // Nếu kết quả trả về thành công, tải lại danh sách chứng chỉ
            loadCertificates(); // Tải lại chứng chỉ từ Firestore
        }
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
}
