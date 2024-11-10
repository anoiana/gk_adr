package com.example.ad_gk.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ad_gk.R;
import com.example.ad_gk.adapter.CertificateAdapter;
import com.example.ad_gk.model.Certificate;

import java.util.ArrayList;
import java.util.List;

public class ListCertificateFragment extends Fragment {

    private RecyclerView recyclerViewCertificate;
    private CertificateAdapter certificateAdapter;
    private List<Certificate> certificateList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_certificate, container, false);

        recyclerViewCertificate = view.findViewById(R.id.recyclerViewCertificate);
        recyclerViewCertificate.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dummy data
        certificateList = new ArrayList<>();
        certificateList.add(new Certificate("Java Certificate", "2024"));
        certificateList.add(new Certificate("Android Development Certificate", "2023"));

        certificateAdapter = new CertificateAdapter(certificateList);
        recyclerViewCertificate.setAdapter(certificateAdapter);

        return view;
    }
}
