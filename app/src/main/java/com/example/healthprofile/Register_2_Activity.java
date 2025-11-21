package com.example.healthprofile;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Register_2_Activity extends AppCompatActivity {
    EditText edtPassword, edtConfirm;
    Button btnRegister;

    String Name, phone, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_2);

        edtPassword = findViewById(R.id.edt_Password);
        edtConfirm = findViewById(R.id.edt_ConfirmPassword);
        btnRegister = findViewById(R.id.btn_Register);

        Name = getIntent().getStringExtra("fullName");
        phone = getIntent().getStringExtra("phone");
        email = getIntent().getStringExtra("email");

        btnRegister.setOnClickListener(v -> {
            String password = edtPassword.getText().toString().trim();
            String confirm = edtConfirm.getText().toString().trim();

            if (password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

            // Kiểm tra trùng username
            if (db.rawQuery("SELECT * FROM user WHERE email = ?", new String[]{email}).getCount() > 0) {
                Toast.makeText(this, "Email này đã được đăng ký!", Toast.LENGTH_SHORT).show();
                db.close();
                return;
            }

            // Lưu vào database
            ContentValues values = new ContentValues();
            values.put("password", password);
            values.put("fullName", Name);
            values.put("email", email);
            values.put("phone", phone);
            values.put("role", "user");

            long result = db.insert("user", null, values);
            db.close();

            if (result != -1) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                // 🔹 Lưu trạng thái đăng nhập (SharedPreferences)
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("email", email);
                editor.putString("fullName", Name);
                editor.apply();

                // 🔹 Chuyển về trang chủ
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("fullName", Name);
                startActivity(intent);
                finishAffinity(); // Xóa stack activity cũ
            } else {
                Toast.makeText(this, "Lỗi khi lưu tài khoản!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
