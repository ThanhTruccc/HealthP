package com.example.healthprofile.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.healthprofile.EditProfileActivity;
import com.example.healthprofile.LoginActivity;
import com.example.healthprofile.R;
import com.example.healthprofile.SecuritySettingsActivity;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment {

    private TextView tvEmail, tvFullName, tvPhone, tv2FAStatus;
    private CardView cardEditProfile, cardSecurity;
    private Button btnLogout;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        initViews(view);
        loadUserInfo();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvEmail = view.findViewById(R.id.tv_email);
        tvFullName = view.findViewById(R.id.tv_fullname);
        tvPhone = view.findViewById(R.id.tv_phone);
        tv2FAStatus = view.findViewById(R.id.tv_2fa_status);

        cardEditProfile = view.findViewById(R.id.card_edit_profile);
        cardSecurity = view.findViewById(R.id.card_security);
        btnLogout = view.findViewById(R.id.btn_logout);

        prefs = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
    }

    private void loadUserInfo() {
        String email = prefs.getString("email", "");
        String fullName = prefs.getString("fullName", "");
        String phone = prefs.getString("phone", "");
        boolean is2FAEnabled = prefs.getBoolean("2fa_enabled", false);

        tvEmail.setText(email);
        tvFullName.setText(fullName);
        tvPhone.setText(phone);

        update2FAStatus(is2FAEnabled);
    }

    private void update2FAStatus(boolean isEnabled) {
        if (isEnabled) {
            tv2FAStatus.setText("Đã bật");
            tv2FAStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tv2FAStatus.setText("Chưa bật");
            tv2FAStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    private void setupClickListeners() {
        // Chỉnh sửa hồ sơ
        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Bảo mật
        cardSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SecuritySettingsActivity.class);
            startActivity(intent);
        });

        // Đăng xuất - hiển thị dialog xác nhận
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    /**
     * Hiển thị dialog xác nhận đăng xuất
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Thực hiện đăng xuất
     */
    private void performLogout() {
        // Xóa session đăng nhập
        prefs.edit().clear().apply();

        // Hiển thị thông báo
        Toast.makeText(getActivity(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Đóng tất cả activities
        if (getActivity() != null) {
            getActivity().finishAffinity();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh lại view khi quay lại từ EditProfile hoặc Security
        if (getView() != null && prefs != null) {
            loadUserInfo();
        }
    }
}