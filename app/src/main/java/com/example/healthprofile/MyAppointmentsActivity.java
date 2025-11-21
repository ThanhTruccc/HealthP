package com.example.healthprofile;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyAppointmentsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageButton btnBack;
    private AppointmentPagerAdapter pagerAdapter;
    private String appointmentType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        initViews();
        setupViewPager();
        setupClickListeners();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        btnBack = findViewById(R.id.btn_back);
    }

    /**
     * Thiết lập ViewPager2 và TabLayout
     */
    private void setupViewPager() {
        // Create adapter
        pagerAdapter = new AppointmentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("Sắp tới");
                                break;
                            case 1:
                                tab.setText("Đã hoàn thành");
                                break;
                            case 2:
                                tab.setText("Đã hủy");
                                break;
                        }
                    }
                }).attach();

        // Set default tab
        viewPager.setCurrentItem(0);
    }

    /**
     * Thiết lập các sự kiện click
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Listen for tab changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Optional: Track analytics or update UI based on selected tab
            }
        });
    }

    /**
     * Adapter cho ViewPager2
     * Quản lý 3 fragment tương ứng với 3 tab
     */
    private static class AppointmentPagerAdapter extends FragmentStateAdapter {

        public AppointmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Return fragment based on position
            switch (position) {
                case 0:
                    // Sắp tới: pending và confirmed
                    return AppointmentListFragment.newInstance("upcoming");
                case 1:
                    // Đã hoàn thành
                    return AppointmentListFragment.newInstance("completed");
                case 2:
                    // Đã hủy
                    return AppointmentListFragment.newInstance("cancelled");
                default:
                    return AppointmentListFragment.newInstance("upcoming");
            }
        }

        @Override
        public int getItemCount() {
            return 3; // 3 tabs
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        refreshAllFragments();
    }

    /**
     * Refresh tất cả fragments để cập nhật dữ liệu mới
     */
    private void refreshAllFragments() {
        if (pagerAdapter != null) {
            // Notify adapter to refresh
            pagerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Public method để refresh từ bên ngoài nếu cần
     */
    public void refreshCurrentFragment() {
        int currentPosition = viewPager.getCurrentItem();
        // Get current fragment and refresh
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + currentPosition);
        if (fragment instanceof AppointmentListFragment) {
            ((AppointmentListFragment) fragment).refreshData();
        }
    }


}
