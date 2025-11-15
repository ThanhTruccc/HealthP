package com.example.healthprofile;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Register_1_Activity extends AppCompatActivity {
    EditText edtFullName, edtPhone, edtEmail;
    Button btnNext;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_1);

        edtFullName = findViewById(R.id.edt_Name);
        edtPhone = findViewById(R.id.edt_Phone);
        edtEmail = findViewById(R.id.edt_Email);
        btnNext = findViewById(R.id.btn_Next);

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS user (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "password TEXT, " + // Đã bỏ username TEXT,
                "fullName TEXT, email TEXT, phone TEXT, role TEXT)");


        btnNext.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();

            if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, Register_2_Activity.class);
            intent.putExtra("fullName", fullName);
            intent.putExtra("phone", phone);
            intent.putExtra("email", email);
            startActivity(intent);
        });
    }
}
