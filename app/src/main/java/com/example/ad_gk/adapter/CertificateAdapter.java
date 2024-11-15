package com.example.ad_gk.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ad_gk.R;
import com.example.ad_gk.activity.CertificateDetailActivity;
import com.example.ad_gk.activity.EditCertificateActivity;
import com.example.ad_gk.model.Certificate;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {

    private List<Certificate> certificateList;
    private OnDeleteClickListener onDeleteClickListener;
    private String userRole;  // Biến lưu trữ role của người dùng

    // Interface to handle delete action
    public interface OnDeleteClickListener {
        void onDelete(Certificate certificate); // Callback khi nút xóa được nhấn
    }

    public CertificateAdapter(List<Certificate> certificateList, String userRole, OnDeleteClickListener onDeleteClickListener) {
        this.certificateList = certificateList;
        this.userRole = userRole;
        this.onDeleteClickListener = onDeleteClickListener;
         // Nhận vai trò người dùng khi khởi tạo adapter
    }

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate certificate = certificateList.get(position);
        holder.bind(certificate);

        // Kiểm tra nếu role là 'Employee', ẩn các nút và sự kiện
        if ("Employee".equals(userRole)) {
            holder.btnDeleteCertificate.setVisibility(View.GONE);  // Ẩn nút xóa
            holder.btnEditCertificate.setVisibility(View.GONE);// Ẩn nút chỉnh sửa
            holder.itemView.setEnabled(false);// Không thể nhấn vào item

        } else {
            // Thiết lập sự kiện cho nút xóa
            holder.btnDeleteCertificate.setVisibility(View.VISIBLE);
            holder.btnDeleteCertificate.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDelete(certificate); // Thông báo tới listener để xóa chứng chỉ
                }
            });

            // Thiết lập sự kiện cho nút chỉnh sửa
            holder.btnEditCertificate.setVisibility(View.VISIBLE);
            holder.btnEditCertificate.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), EditCertificateActivity.class);
                intent.putExtra("certificateId", certificate.getCertificateId()); // Truyền certificateId
                holder.itemView.getContext().startActivity(intent); // Mở Activity chỉnh sửa
            });
        }

        // Thiết lập sự kiện nhấn vào item để hiển thị chi tiết chứng chỉ
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CertificateDetailActivity.class);
            intent.putExtra("certificateId", certificate.getCertificateId());
            intent.putExtra("certificateName", certificate.getName());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    // Phương thức xóa chứng chỉ khỏi danh sách
    public void removeItem(Certificate certificate) {
        int position = certificateList.indexOf(certificate);
        if (position != -1) {
            certificateList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class CertificateViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCertificateId;
        private TextView tvCertificateName;
        private ImageView btnDeleteCertificate, btnEditCertificate;

        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertificateId = itemView.findViewById(R.id.tvCertificateId);
            tvCertificateName = itemView.findViewById(R.id.tvCertificateName);
            btnDeleteCertificate = itemView.findViewById(R.id.btn_delete_certificate);
            btnEditCertificate = itemView.findViewById(R.id.btn_edit_certificate);
        }

        public void bind(Certificate certificate) {
            tvCertificateId.setText(certificate.getCertificateId());
            tvCertificateName.setText(certificate.getName());
        }
    }
}
