package com.example.ad_gk.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ad_gk.fragment.ListCertificateFragment;
import com.example.ad_gk.fragment.ListStudentFragment;
import com.example.ad_gk.fragment.ListUserFragment;
import com.example.ad_gk.fragment.ProfileFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ProfileFragment();
            case 1:
                return new ListStudentFragment();
            case 2:
                return new ListCertificateFragment();
            case 3:
                return new ListUserFragment();
            default:
                return new ProfileFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Number of tabs
    }
}
