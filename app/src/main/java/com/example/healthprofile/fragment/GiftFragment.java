package com.example.healthprofile.fragment;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.adapter.VoucherAdapter;
import com.example.healthprofile.adapter.RedeemedVoucherAdapter;
import com.example.healthprofile.model.Voucher;
import com.example.healthprofile.model.RedeemedVoucher;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class GiftFragment extends Fragment {

    private TextView tvPoints;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;

    // SQLiteDatabase trực tiếp
    private SQLiteDatabase db;
    private String userEmail;

    private VoucherAdapter voucherAdapter;
    private RedeemedVoucherAdapter redeemedAdapter;

    // Database constants
    private static final String DATABASE_NAME = "health_profile.db";
    private static final String TABLE_REWARD_POINTS = "reward_points";
    private static final String TABLE_VOUCHERS = "vouchers";
    private static final String TABLE_REDEEMED_VOUCHERS = "redeemed_vouchers";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gift, container, false);

        // Mở database trực tiếp
        db = requireContext().openOrCreateDatabase(DATABASE_NAME, android.content.Context.MODE_PRIVATE, null);

        // Lấy email user
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession",
                android.content.Context.MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        // Khởi tạo views
        tvPoints = view.findViewById(R.id.tv_points);
        recyclerView = view.findViewById(R.id.recycler_vouchers);
        tabLayout = view.findViewById(R.id.tab_layout);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Setup tabs
        setupTabs();

        // Load data
        loadUserPoints();
        loadVouchers();

        return view;
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Voucher Khả Dụng"));
        tabLayout.addTab(tabLayout.newTab().setText("Lịch Sử Đổi Thưởng"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadVouchers();
                } else {
                    loadRedeemedVouchers();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Lấy tổng điểm của user
     */
    private void loadUserPoints() {
        int points = getUserPoints(userEmail);
        tvPoints.setText(points + " điểm");
    }

    private int getUserPoints(String userEmail) {
        String sql = "SELECT COALESCE(SUM(points_change), 0) FROM " + TABLE_REWARD_POINTS +
                " WHERE user_email = '" + escapeString(userEmail) + "'";
        Cursor cursor = db.rawQuery(sql, null);
        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(0);
        }
        cursor.close();
        return Math.max(0, points); // Đảm bảo không âm
    }

    /**
     * Load danh sách voucher khả dụng
     */
    private void loadVouchers() {
        List<Voucher> vouchers = getAllVouchers();

        voucherAdapter = new VoucherAdapter(requireContext(), vouchers, voucher -> {
            showRedeemDialog(voucher);
        });

        recyclerView.setAdapter(voucherAdapter);
    }

    private List<Voucher> getAllVouchers() {
        List<Voucher> vouchers = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_VOUCHERS +
                " WHERE is_available = 1 " +
                "ORDER BY points_required ASC";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                Voucher voucher = new Voucher();
                voucher.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                voucher.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                voucher.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                voucher.setPointsRequired(cursor.getInt(cursor.getColumnIndexOrThrow("points_required")));
                voucher.setDiscountPercent(cursor.getInt(cursor.getColumnIndexOrThrow("discount_percent")));
                voucher.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                voucher.setImageResource(cursor.getInt(cursor.getColumnIndexOrThrow("image_resource")));
                voucher.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow("is_available")) == 1);
                vouchers.add(voucher);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return vouchers;
    }

    /**
     * Load lịch sử voucher đã đổi
     */
    private void loadRedeemedVouchers() {
        List<RedeemedVoucher> redeemed = getRedeemedVouchers(userEmail);

        redeemedAdapter = new RedeemedVoucherAdapter(requireContext(), redeemed);
        recyclerView.setAdapter(redeemedAdapter);
    }

    private List<RedeemedVoucher> getRedeemedVouchers(String userEmail) {
        List<RedeemedVoucher> redeemed = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_REDEEMED_VOUCHERS +
                " WHERE user_email = '" + escapeString(userEmail) + "' " +
                "ORDER BY redeemed_date DESC";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                RedeemedVoucher rv = new RedeemedVoucher();
                rv.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                rv.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
                rv.setVoucherId(cursor.getInt(cursor.getColumnIndexOrThrow("voucher_id")));
                rv.setVoucherTitle(cursor.getString(cursor.getColumnIndexOrThrow("voucher_title")));
                rv.setPointsUsed(cursor.getInt(cursor.getColumnIndexOrThrow("points_used")));
                rv.setVoucherCode(cursor.getString(cursor.getColumnIndexOrThrow("voucher_code")));
                rv.setRedeemedDate(cursor.getLong(cursor.getColumnIndexOrThrow("redeemed_date")));
                rv.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                redeemed.add(rv);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return redeemed;
    }

    /**
     * Hiển thị dialog xác nhận đổi voucher
     */
    private void showRedeemDialog(Voucher voucher) {
        int currentPoints = getUserPoints(userEmail);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Đổi Voucher");

        String message = "Bạn có muốn đổi voucher này?\n\n" +
                "Voucher: " + voucher.getTitle() + "\n" +
                "Giảm: " + voucher.getDiscountPercent() + "%\n" +
                "Điểm cần: " + voucher.getPointsRequired() + "\n" +
                "Điểm hiện tại: " + currentPoints;

        builder.setMessage(message);

        builder.setPositiveButton("Đổi Ngay", (dialog, which) -> {
            if (redeemVoucher(userEmail, voucher)) {
                Toast.makeText(requireContext(), "Đổi voucher thành công!",
                        Toast.LENGTH_SHORT).show();
                loadUserPoints();

                // Reload danh sách nếu đang ở tab voucher
                if (tabLayout.getSelectedTabPosition() == 0) {
                    loadVouchers();
                }
            } else {
                Toast.makeText(requireContext(), "Không đủ điểm!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    /**
     * Đổi voucher
     */
    private boolean redeemVoucher(String userEmail, Voucher voucher) {
        int currentPoints = getUserPoints(userEmail);

        if (currentPoints < voucher.getPointsRequired()) {
            return false; // Không đủ điểm
        }

        try {
            // Tạo mã voucher
            String voucherCode = "VC" + System.currentTimeMillis();

            // Trừ điểm
            addRewardPoints(userEmail, "redeem_voucher", -voucher.getPointsRequired(),
                    "Đổi: " + voucher.getTitle());

            // Lưu voucher đã đổi
            String sql = "INSERT INTO " + TABLE_REDEEMED_VOUCHERS +
                    " (user_email, voucher_id, voucher_title, points_used, voucher_code, redeemed_date, status) " +
                    "VALUES ('" + escapeString(userEmail) + "', " +
                    voucher.getId() + ", " +
                    "'" + escapeString(voucher.getTitle()) + "', " +
                    voucher.getPointsRequired() + ", " +
                    "'" + voucherCode + "', " +
                    System.currentTimeMillis() + ", 'active')";
            db.execSQL(sql);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm điểm thưởng
     */
    private void addRewardPoints(String userEmail, String action, int pointsChange, String description) {
        int currentPoints = getUserPoints(userEmail);
        int newPoints = currentPoints + pointsChange;

        String sql = "INSERT INTO " + TABLE_REWARD_POINTS +
                " (user_email, points, actionn, points_change, description, timestamp) " +
                "VALUES ('" + escapeString(userEmail) + "', " +
                newPoints + ", " +
                "'" + escapeString(action) + "', " +
                pointsChange + ", " +
                "'" + escapeString(description) + "', " +
                System.currentTimeMillis() + ")";
        db.execSQL(sql);
    }

    /**
     * Escape string để tránh SQL injection
     */
    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserPoints();

        // Reload data dựa trên tab hiện tại
        if (tabLayout.getSelectedTabPosition() == 0) {
            loadVouchers();
        } else {
            loadRedeemedVouchers();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}