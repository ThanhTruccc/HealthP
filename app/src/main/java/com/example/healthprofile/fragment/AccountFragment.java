package com.example.healthprofile.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.healthprofile.EditProfileActivity;
import com.example.healthprofile.MainActivity;
import com.example.healthprofile.R;
import com.example.healthprofile.SecuritySettingsActivity;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Tìm các view trong layout
        TextView tvEmail = view.findViewById(R.id.tv_email);
        TextView tvFullName = view.findViewById(R.id.tv_fullname);
        TextView tvPhone = view.findViewById(R.id.tv_phone);
        TextView tv2FAStatus = view.findViewById(R.id.tv_2fa_status);

        CardView cardEditProfile = view.findViewById(R.id.card_edit_profile);
        CardView cardSecurity = view.findViewById(R.id.card_security);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        // Lấy thông tin user từ SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        String email = prefs.getString("email", "");
        String fullName = prefs.getString("fullName", "");
        String phone = prefs.getString("phone", "");
        boolean is2FAEnabled = prefs.getBoolean("2fa_enabled", false);

        // Hiển thị thông tin
        tvEmail.setText(email);
        tvFullName.setText(fullName);
        tvPhone.setText(phone);

        // Hiển thị trạng thái 2FA
        if (is2FAEnabled) {
            tv2FAStatus.setText("Đã bật");
            tv2FAStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tv2FAStatus.setText("⚠Chưa bật");
            tv2FAStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }

        // Xử lý sự kiện Chỉnh sửa hồ sơ
        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện Bảo mật
        cardSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SecuritySettingsActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện nút Đăng xuất
        btnLogout.setOnClickListener(v -> {
            // Xóa session đăng nhập
            prefs.edit().clear().apply();

            // Quay về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finishAffinity();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh lại view khi quay lại từ EditProfile hoặc Security
        if (getView() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);

            TextView tvEmail = getView().findViewById(R.id.tv_email);
            TextView tvFullName = getView().findViewById(R.id.tv_fullname);
            TextView tvPhone = getView().findViewById(R.id.tv_phone);
            TextView tv2FAStatus = getView().findViewById(R.id.tv_2fa_status);

            tvEmail.setText(prefs.getString("email", ""));
            tvFullName.setText(prefs.getString("fullName", ""));
            tvPhone.setText(prefs.getString("phone", ""));

            boolean is2FAEnabled = prefs.getBoolean("2fa_enabled", false);
            if (is2FAEnabled) {
                tv2FAStatus.setText("Đã bật");
                tv2FAStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tv2FAStatus.setText("Chưa bật");
                tv2FAStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        }
    }
}