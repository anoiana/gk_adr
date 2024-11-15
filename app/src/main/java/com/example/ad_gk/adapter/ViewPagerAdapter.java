package com.example.ad_gk.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ad_gk.fragment.ListCertificateFragment;
import com.example.ad_gk.fragment.ListStudentFragment;
import com.example.ad_gk.fragment.ListUserFragment;
import com.example.ad_gk.fragment.ProfileFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private String role;
    private String userId;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String role, String userId) {
        super(fragmentActivity);
        this.role = role; // Nhận role từ MainActivity
        this.userId = userId; // Nhận userId từ MainActivity
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;

        switch (position) {
            case 0:
                fragment = new ProfileFragment();
                break;
            case 1:
                fragment = new ListStudentFragment();
                break;
            case 2:
                fragment = new ListCertificateFragment();
                break;
            case 3:
                fragment = new ListUserFragment();
                break;
            default:
                fragment = new ProfileFragment();
        }

        // Truyền role và userId vào Fragment
        Bundle bundle = new Bundle();
        bundle.putString("role", role);
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4; // Số lượng tab
    }
}
