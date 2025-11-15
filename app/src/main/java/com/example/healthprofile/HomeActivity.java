package com.example.healthprofile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.healthprofile.fragment.AccountFragment;
import com.example.healthprofile.fragment.GiftFragment;
import com.example.healthprofile.fragment.HomeFragment;
import com.example.healthprofile.fragment.ServiceFragment;
import com.example.healthprofile.fragment.UtilityFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    String email, fullName, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Nhận dữ liệu từ LoginActivity hoặc RegisterActivity
        email = getIntent().getStringExtra("email");
        fullName = getIntent().getStringExtra("fullName");
        phone = getIntent().getStringExtra("phone");

        // Khởi tạo Bottom Navigation
        initBottomNavigation();

        // Load fragment mặc định (Trang chủ)
        loadFragment(new HomeFragment());
    }

    /**
     * Khởi tạo và thiết lập Bottom Navigation
     */
    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView == null) {
            Log.e("HomeActivity", "Bottom Navigation not found in layout!");
            return;
        }

        // Xử lý sự kiện khi click vào các tab
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_service) {
                selectedFragment = new ServiceFragment();
            } else if (itemId == R.id.nav_account) {
                selectedFragment = new AccountFragment();
            } else if (itemId == R.id.nav_utility) {
                selectedFragment = new UtilityFragment();
            } else if (itemId == R.id.nav_gift) {
                selectedFragment = new GiftFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    /**
     * Load Fragment vào container
     */
    private void loadFragment(Fragment fragment) {
        // Truyền dữ liệu user vào fragment
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("fullName", fullName);
        bundle.putString("phone", phone);
        fragment.setArguments(bundle);

        // Thay thế fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Getter cho email (có thể dùng từ fragments)
     */
    public String getUserEmail() {
        return email;
    }

    public String getUserFullName() {
        return fullName;
    }

    public String getUserPhone() {
        return phone;
    }
}