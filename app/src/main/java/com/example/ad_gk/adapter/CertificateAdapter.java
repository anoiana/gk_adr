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

    // Interface to handle delete action
    public interface OnDeleteClickListener {
        void onDelete(Certificate certificate); // Callback when delete button is clicked
    }

    public CertificateAdapter(List<Certificate> certificateList, OnDeleteClickListener onDeleteClickListener) {
        this.certificateList = certificateList;
        this.onDeleteClickListener = onDeleteClickListener;
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

        // Set click listener for the delete button
        holder.btnDeleteCertificate.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDelete(certificate); // Notify listener to remove item
            }
        });

        // Thiết lập sự kiện cho nút chỉnh sửa
        holder.btnEditCertificate.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EditCertificateActivity.class);
            intent.putExtra("certificateId", certificate.getCertificateId()); // Truyền certificateId
            holder.itemView.getContext().startActivity(intent); // Mở activity chỉnh sửa
        });

        // Thiết lập sự kiện nhấn vào item
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
            // Bind data to views
            tvCertificateId.setText(certificate.getCertificateId());
            tvCertificateName.setText(certificate.getName());
        }
    }
}
