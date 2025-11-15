package com.example.healthprofile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminManagementActivity extends AppCompatActivity {

    private TextView tvAdminName, tvTotalUsers, tvTotalDoctors, tvTotalAppointments, tvTotalChallenges;
    private CardView cardManageUsers, cardManageDoctors, cardManageAppointments, cardManageChallenges;
    private Button btnLogout;

    private SQLiteDatabase db;

    // Database constants
    private static final String DATABASE_NAME = "health_profile.db";
    private static final String TABLE_USERS = "user";
    private static final String TABLE_DOCTORS = "doctors";
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String TABLE_CHALLENGES = "challenges";
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        // Kiểm tra quyền admin
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = prefs.getString("role", "");

        if (!"admin".equals(role)) {
            Toast.makeText(this, "Bạn không có quyền truy cập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mở database trực tiếp
        db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);

        // Khởi tạo ExecutorService và Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        loadStatistics();
        setupClickListeners();
    }

    private void initViews() {
        tvAdminName = findViewById(R.id.tv_admin_name);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalDoctors = findViewById(R.id.tv_total_doctors);
        tvTotalAppointments = findViewById(R.id.tv_total_appointments);
        tvTotalChallenges = findViewById(R.id.tv_total_challenges);

        cardManageUsers = findViewById(R.id.card_manage_users);
        cardManageDoctors = findViewById(R.id.card_manage_doctors);
        cardManageAppointments = findViewById(R.id.card_manage_appointments);
        cardManageChallenges = findViewById(R.id.card_manage_challenges);

        btnLogout = findViewById(R.id.btn_logout);

        // Hiển thị tên admin
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String adminName = prefs.getString("fullName", "Admin");
        tvAdminName.setText("Xin chào, " + adminName);
    }

    /**
     * Load thống kê (background thread)
     */
    private void loadStatistics() {
        // Execute in background thread
        executorService.execute(() -> {
            int[] stats = getStatistics();

            // Update UI on main thread
            mainHandler.post(() -> {
                updateStatisticsUI(stats);
            });
        });
    }

    /**
     * Lấy thống kê từ database
     */
    private int[] getStatistics() {
        int[] stats = new int[4];

        try {
            // Đếm users
            stats[0] = getCount(TABLE_USERS);

            // Đếm doctors
            stats[1] = getCount(TABLE_DOCTORS);

            // Đếm appointments
            stats[2] = getCount(TABLE_APPOINTMENTS);

            // Đếm challenges
            stats[3] = getCount(TABLE_CHALLENGES);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Đếm số lượng records trong bảng
     */
    private int getCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        Cursor cursor = db.rawQuery(sql, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Update UI với thống kê
     */
    private void updateStatisticsUI(int[] stats) {
        tvTotalUsers.setText("Người dùng: " + stats[0]);
        tvTotalDoctors.setText("Bác sĩ: " + stats[1]);
        tvTotalAppointments.setText("Lịch hẹn: " + stats[2]);
        tvTotalChallenges.setText("Thử thách: " + stats[3]);
    }

    private void setupClickListeners() {
        // Quản lý tài khoản
        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserManagementActivity.class);
            startActivity(intent);
        });

        // Quản lý bác sĩ
        cardManageDoctors.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDoctorManagementActivity.class);
            startActivity(intent);
        });

        // Quản lý lịch hẹn
        cardManageAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAppointmentActivity.class);
            startActivity(intent);
        });

        // Quản lý thử thách
        cardManageChallenges.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminChallengeManagementActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload statistics khi quay lại activity
        loadStatistics();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Shutdown executor
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        // Đóng database
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}