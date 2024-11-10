package com.example.ad_gk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ad_gk.R;
import com.example.ad_gk.model.Certificate;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {

    private List<Certificate> certificateList;

    public CertificateAdapter(List<Certificate> certificateList) {
        this.certificateList = certificateList;
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
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    static class CertificateViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCertificateName;
        private TextView tvCertificateYear;

        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertificateName = itemView.findViewById(R.id.tvCertificateName);
            tvCertificateYear = itemView.findViewById(R.id.tvCertificateYear);
        }

        public void bind(Certificate certificate) {
            tvCertificateName.setText(certificate.getName());
            tvCertificateYear.setText(certificate.getYear());
        }
    }
}
