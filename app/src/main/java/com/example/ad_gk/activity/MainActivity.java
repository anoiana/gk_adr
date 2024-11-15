package com.example.ad_gk.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ad_gk.R;
import com.example.ad_gk.adapter.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Nhận dữ liệu từ Intent
        String role = getIntent().getStringExtra("userRole");
        String userId = getIntent().getStringExtra("userId");

        // Kiểm tra nếu userId là null, điều hướng về trang đăng nhập
        if (userId == null) {
            // Chuyển hướng về LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Kết thúc MainActivity để người dùng không thể quay lại trang này
            return; // Dừng lại để không tiếp tục xử lý sau đó
        }

        // Setup giao diện hệ thống để tương thích với Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ View
        viewPager = findViewById(R.id.viewPager);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up ViewPager với Adapter và truyền role, userId
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, role, userId);
        viewPager.setAdapter(adapter);

        // Đồng bộ ViewPager với BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.nav_student) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.nav_certificate) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.nav_user) {
                viewPager.setCurrentItem(3);
                return true;
            } else {
                return false;
            }
        });

        // Thay đổi BottomNavigationView khi ViewPager được swipe
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_student);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_certificate);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_user);
                        break;
                }
            }
        });
    }
}
