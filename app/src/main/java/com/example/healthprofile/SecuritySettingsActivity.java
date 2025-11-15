package com.example.healthprofile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

/**
 * Activity quản lý bảo mật tài khoản
 * - Bật/Tắt xác thực 2 bước (OTP)
 * - Mô phỏng gửi OTP qua email
 */
public class SecuritySettingsActivity extends AppCompatActivity {

    private Switch switch2FA;
    private LinearLayout layout2FASetup;
    private TextView tvEmail, tvOTPStatus, tvCountdown;
    private TextInputEditText etOTP;
    private Button btnSendOTP, btnVerifyOTP;
    private ImageButton btnBack;

    private SharedPreferences prefs;
    private String userEmail;
    private String generatedOTP;
    private CountDownTimer otpTimer;
    private boolean isOTPSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_settings);

        initViews();

        prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        loadSettings();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        switch2FA = findViewById(R.id.switch_2fa);
        layout2FASetup = findViewById(R.id.layout_2fa_setup);
        tvEmail = findViewById(R.id.tv_email);
        tvOTPStatus = findViewById(R.id.tv_otp_status);
        tvCountdown = findViewById(R.id.tv_countdown);
        etOTP = findViewById(R.id.et_otp);
        btnSendOTP = findViewById(R.id.btn_send_otp);
        btnVerifyOTP = findViewById(R.id.btn_verify_otp);
    }

    private void loadSettings() {
        boolean is2FAEnabled = prefs.getBoolean("2fa_enabled", false);
        switch2FA.setChecked(is2FAEnabled);

        tvEmail.setText("Email: " + maskEmail(userEmail));

        // Hiển thị trạng thái
        updateStatus(is2FAEnabled);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        switch2FA.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Bật 2FA -> Hiển thị form setup
                layout2FASetup.setVisibility(View.VISIBLE);
                tvOTPStatus.setText("⚠️ Vui lòng xác thực để bật xác thực 2 bước");
            } else {
                // Tắt 2FA -> Xác nhận
                showDisable2FADialog();
            }
        });

        btnSendOTP.setOnClickListener(v -> sendOTP());

        btnVerifyOTP.setOnClickListener(v -> verifyOTP());
    }

    /**
     * Gửi OTP (Mô phỏng)
     */
    private void sendOTP() {
        // Generate random 6-digit OTP
        generatedOTP = String.format("%06d", new Random().nextInt(999999));

        // Mô phỏng gửi email
        simulateEmailSending();

        isOTPSent = true;
        btnSendOTP.setEnabled(false);
        btnSendOTP.setText("Đã gửi");

        tvOTPStatus.setText("✉️ Mã OTP đã được gửi đến email:\n" + maskEmail(userEmail));

        // Hiển thị countdown 60 giây
        startOTPTimer();

        // Log OTP (chỉ để test)
        Toast.makeText(this, "Mã OTP test: " + generatedOTP, Toast.LENGTH_LONG).show();
    }

    /**
     * Xác thực OTP
     */
    private void verifyOTP() {
        String inputOTP = etOTP.getText().toString().trim();

        if (inputOTP.isEmpty()) {
            etOTP.setError("Vui lòng nhập mã OTP");
            etOTP.requestFocus();
            return;
        }

        if (!isOTPSent) {
            Toast.makeText(this, "Vui lòng gửi mã OTP trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inputOTP.equals(generatedOTP)) {
            // OTP đúng
            enable2FA();
        } else {
            // OTP sai
            etOTP.setError("Mã OTP không đúng");
            etOTP.requestFocus();
            Toast.makeText(this, "Mã OTP không đúng. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Bật 2FA
     */
    private void enable2FA() {
        prefs.edit().putBoolean("2fa_enabled", true).apply();

        if (otpTimer != null) {
            otpTimer.cancel();
        }

        new AlertDialog.Builder(this)
                .setTitle("✅ Thành công")
                .setMessage("Xác thực 2 bước đã được BẬT.\n\nTừ giờ, bạn sẽ cần nhập mã OTP mỗi khi đăng nhập.")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    layout2FASetup.setVisibility(View.GONE);
                    updateStatus(true);
                    resetOTPForm();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Tắt 2FA
     */
    private void showDisable2FADialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tắt xác thực 2 bước?")
                .setMessage("Bạn có chắc chắn muốn tắt xác thực 2 bước?\n\nTài khoản của bạn sẽ kém bảo mật hơn.")
                .setPositiveButton("Tắt", (dialog, which) -> {
                    disable2FA();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    switch2FA.setChecked(true);
                })
                .show();
    }

    private void disable2FA() {
        prefs.edit().putBoolean("2fa_enabled", false).apply();
        layout2FASetup.setVisibility(View.GONE);
        updateStatus(false);
        Toast.makeText(this, "Đã tắt xác thực 2 bước", Toast.LENGTH_SHORT).show();
    }

    /**
     * Update trạng thái hiển thị
     */
    private void updateStatus(boolean enabled) {
        if (enabled) {
            tvOTPStatus.setText("Xác thực 2 bước đang BẬT");
            tvOTPStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvOTPStatus.setText("Xác thực 2 bước đang TẮT");
            tvOTPStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    /**
     * Mô phỏng gửi email
     */
    private void simulateEmailSending() {
        // Trong thực tế, gọi API gửi email tại đây
        // Ví dụ: EmailService.sendOTP(userEmail, generatedOTP);

        String emailBody = "Mã OTP của bạn là: " + generatedOTP + "\n" +
                "Mã có hiệu lực trong 60 giây.\n" +
                "Vui lòng không chia sẻ mã này với bất kỳ ai.";

        // Log để test
        android.util.Log.d("OTP_EMAIL", "To: " + userEmail);
        android.util.Log.d("OTP_EMAIL", "Body: " + emailBody);
    }

    /**
     * Start countdown timer cho OTP
     */
    private void startOTPTimer() {
        tvCountdown.setVisibility(View.VISIBLE);

        otpTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvCountdown.setText("Mã OTP hết hiệu lực sau: " + seconds + "s");
            }

            @Override
            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                btnSendOTP.setEnabled(true);
                btnSendOTP.setText("Gửi lại mã OTP");
                isOTPSent = false;
                generatedOTP = null;
                Toast.makeText(SecuritySettingsActivity.this,
                        "Mã OTP đã hết hiệu lực. Vui lòng gửi lại!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    /**
     * Reset form OTP
     */
    private void resetOTPForm() {
        etOTP.setText("");
        btnSendOTP.setEnabled(true);
        btnSendOTP.setText("Gửi mã OTP");
        tvCountdown.setVisibility(View.GONE);
        isOTPSent = false;
        generatedOTP = null;
    }

    /**
     * Mask email để bảo mật
     * Ví dụ: user@gmail.com -> u***@gmail.com
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 5) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String username = email.substring(0, atIndex);
            String domain = email.substring(atIndex);

            if (username.length() > 2) {
                String masked = username.charAt(0) + "***" + username.charAt(username.length() - 1);
                return masked + domain;
            }
        }

        return email;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null) {
            otpTimer.cancel();
        }
    }
}