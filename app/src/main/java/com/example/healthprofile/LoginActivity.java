package com.example.healthprofile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView tvRegister;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_Email);
        edtPassword = findViewById(R.id.edt_Password);
        btnLogin = findViewById(R.id.btn_Login);
        tvRegister = findViewById(R.id.tv_Register);

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);


        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra định dạng email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra đăng nhập bằng email
            Cursor cursor = db.rawQuery("SELECT * FROM user WHERE email = ? AND password = ?",
                    new String[]{email, password});

            if (cursor.moveToFirst()) {
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow("fullName"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));

                cursor.close();

                // Lưu trạng thái đăng nhập
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("email", email);
                editor.putString("fullName", fullName);
                editor.putString("phone", phone);
                editor.putString("role", role);
                editor.apply();

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển về trang chủ
                Intent intent;
                if ("admin".equals(role)) {
                    // Nếu là admin -> chuyển đến AdminDashboard
                    intent = new Intent(this, AdminManagementActivity.class);
                } else {
                    // Nếu là user thường -> chuyển đến Home
                    intent = new Intent(this, HomeActivity.class);
                }
                intent.putExtra("email", email);
                intent.putExtra("fullName", fullName);
                intent.putExtra("phone", phone);
                intent.putExtra("role", role);
                startActivity(intent);
                finishAffinity();
            } else {
                cursor.close();
                Toast.makeText(this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
            }
        });

        // Chuyển đến trang đăng ký
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, Register_1_Activity.class);
            startActivity(intent);
        });
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (db != null && db.isOpen()) {
//            db.close();
//        }
//    }
}