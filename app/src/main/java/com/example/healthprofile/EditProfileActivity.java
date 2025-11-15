package com.example.healthprofile;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthprofile.model.User;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity chỉnh sửa thông tin cá nhân - Sử dụng SQLiteDatabase trực tiếp
 */
public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnSaveProfile, btnChangePassword;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private SQLiteDatabase db;
    private String currentEmail;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();

        // Mở database
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        // Lấy email từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentEmail = prefs.getString("email", "");

        // Load thông tin user
        loadUserProfile();

        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void loadUserProfile() {
        new LoadProfileTask().execute();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> {
            if (validateProfileInput()) {
                showConfirmDialog();
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            if (validatePasswordInput()) {
                changePassword();
            }
        });
    }

    private boolean validateProfileInput() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return false;
        }

        if (phone.length() < 10) {
            etPhone.setError("Số điện thoại phải có ít nhất 10 số");
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validatePasswordInput() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            etCurrentPassword.requestFocus();
            return false;
        }

        if (!currentPassword.equals(currentUser.getPassword())) {
            etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
            etCurrentPassword.requestFocus();
            return false;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            etNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etNewPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (currentPassword.equals(newPassword)) {
            etNewPassword.setError("Mật khẩu mới phải khác mật khẩu cũ");
            etNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showConfirmDialog() {
        String newEmail = etEmail.getText().toString().trim();

        String message = "Bạn có chắc chắn muốn cập nhật thông tin?\n\n";
        if (!newEmail.equals(currentEmail)) {
            message += "⚠️ Lưu ý: Email sẽ được thay đổi từ:\n" +
                    currentEmail + "\n→ " + newEmail + "\n\n" +
                    "Bạn sẽ cần đăng nhập lại sau khi cập nhật.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận cập nhật")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    updateProfile();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateProfile() {
        new UpdateProfileTask().execute();
    }

    private void changePassword() {
        new ChangePasswordTask().execute();
    }

    /**
     * AsyncTask load user profile
     */
    private class LoadProfileTask extends AsyncTask<Void, Void, User> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected User doInBackground(Void... voids) {
            try {
                String sql = "SELECT * FROM user WHERE email = '" + escapeString(currentEmail) + "'";
                Cursor cursor = db.rawQuery(sql, null);

                User user = null;
                if (cursor.moveToFirst()) {
                    user = new User(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("password")),
                            cursor.getString(cursor.getColumnIndexOrThrow("fullName")),
                            cursor.getString(cursor.getColumnIndexOrThrow("email")),
                            cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                            cursor.getString(cursor.getColumnIndexOrThrow("role"))
                    );
                }
                cursor.close();
                return user;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            progressBar.setVisibility(View.GONE);

            if (user != null) {
                currentUser = user;
                etFullName.setText(user.getFullName());
                etEmail.setText(user.getEmail());
                etPhone.setText(user.getPhone());
            } else {
                Toast.makeText(EditProfileActivity.this,
                        "Lỗi khi tải thông tin!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * AsyncTask cập nhật profile
     */
    private class UpdateProfileTask extends AsyncTask<Void, Void, Integer> {
        private String newEmail;
        private boolean emailChanged;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            btnSaveProfile.setEnabled(false);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                String fullName = etFullName.getText().toString().trim();
                newEmail = etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();

                emailChanged = !newEmail.equals(currentEmail);

                // Nếu email thay đổi, kiểm tra email mới đã tồn tại chưa
                if (emailChanged) {
                    String checkSql = "SELECT COUNT(*) FROM user WHERE email = '" +
                            escapeString(newEmail) + "' AND email != '" +
                            escapeString(currentEmail) + "'";
                    Cursor cursor = db.rawQuery(checkSql, null);

                    int count = 0;
                    if (cursor.moveToFirst()) {
                        count = cursor.getInt(0);
                    }
                    cursor.close();

                    if (count > 0) {
                        return 0; // Email đã tồn tại
                    }
                }

                // Cập nhật user
                // Nếu email thay đổi, cần update theo email cũ
                String updateSql = "UPDATE user SET " +
                        "fullName = '" + escapeString(fullName) + "', " +
                        "email = '" + escapeString(newEmail) + "', " +
                        "phone = '" + escapeString(phone) + "' " +
                        "WHERE email = '" + escapeString(currentEmail) + "'";

                db.execSQL(updateSql);

                // Cập nhật currentUser object
                currentUser.setFullName(fullName);
                currentUser.setEmail(newEmail);
                currentUser.setPhone(phone);

                return 1; // Thành công
            } catch (Exception e) {
                e.printStackTrace();
                return -1; // Lỗi
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            btnSaveProfile.setEnabled(true);

            if (result == 1) {
                Toast.makeText(EditProfileActivity.this,
                        "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                // Cập nhật SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", newEmail);
                editor.putString("fullName", currentUser.getFullName());
                editor.putString("phone", currentUser.getPhone());
                editor.apply();

                if (emailChanged) {
                    // Email đã thay đổi, yêu cầu đăng nhập lại
                    new AlertDialog.Builder(EditProfileActivity.this)
                            .setTitle("Thành công")
                            .setMessage("Thông tin đã được cập nhật.\n\nEmail đã thay đổi, vui lòng đăng nhập lại.")
                            .setPositiveButton("Đăng nhập", (dialog, which) -> {
                                // Clear session và quay về màn hình đăng nhập
                                editor.clear().apply();
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    finish();
                }
            } else if (result == 0) {
                Toast.makeText(EditProfileActivity.this,
                        "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfileActivity.this,
                        "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * AsyncTask đổi mật khẩu
     */
    private class ChangePasswordTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            btnChangePassword.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String newPassword = etNewPassword.getText().toString().trim();

                String sql = "UPDATE user SET " +
                        "password = '" + escapeString(newPassword) + "' " +
                        "WHERE email = '" + escapeString(currentUser.getEmail()) + "'";

                db.execSQL(sql);

                currentUser.setPassword(newPassword);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            progressBar.setVisibility(View.GONE);
            btnChangePassword.setEnabled(true);

            if (success) {
                Toast.makeText(EditProfileActivity.this,
                        "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();

                // Clear password fields
                etCurrentPassword.setText("");
                etNewPassword.setText("");
                etConfirmPassword.setText("");

                // Hiển thị dialog
                new AlertDialog.Builder(EditProfileActivity.this)
                        .setTitle("Thành công")
                        .setMessage("Mật khẩu đã được thay đổi.\n\nVui lòng đăng nhập lại với mật khẩu mới.")
                        .setPositiveButton("Đăng nhập", (dialog, which) -> {
                            // Clear session
                            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                            prefs.edit().clear().apply();
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            } else {
                Toast.makeText(EditProfileActivity.this,
                        "Lỗi khi đổi mật khẩu!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Escape string để tránh SQL injection
     */
    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}