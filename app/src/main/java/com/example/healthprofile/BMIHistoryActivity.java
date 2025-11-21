package com.example.healthprofile;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.BMIHistoryAdapter;
import com.example.healthprofile.model.BMIRecord;

import java.util.ArrayList;
import java.util.List;

public class BMIHistoryActivity extends AppCompatActivity {

    private RecyclerView rvBmiHistory;
    private TextView tvRecordCount;
    private LinearLayout layoutEmpty;
    private ImageButton btnBack, btnDeleteAll;

    private SQLiteDatabase db;
    private BMIHistoryAdapter adapter;
    private List<BMIRecord> bmiRecords;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_history);

        // Lấy email từ SharedPreferences hoặc Intent
        userEmail = getSharedPreferences("HealthProfile", MODE_PRIVATE)
                .getString("userEmail", "");

        initViews();
        initDatabase();
        loadBMIHistory();
        setupClickListeners();
    }

    private void initViews() {
        rvBmiHistory = findViewById(R.id.rv_bmi_history);
        tvRecordCount = findViewById(R.id.tv_record_count);
        layoutEmpty = findViewById(R.id.layout_empty);
        btnBack = findViewById(R.id.btn_back);
        btnDeleteAll = findViewById(R.id.btn_delete_all);

        rvBmiHistory.setLayoutManager(new LinearLayoutManager(this));
        bmiRecords = new ArrayList<>();
    }

    private void initDatabase() {
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        // Tạo bảng bmi_records nếu chưa có
        db.execSQL("CREATE TABLE IF NOT EXISTS bmi_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "gender TEXT, " +
                "age INTEGER, " +
                "height REAL NOT NULL, " +
                "weight REAL NOT NULL, " +
                "bmi REAL NOT NULL, " +
                "category TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL)");
    }

    private void loadBMIHistory() {
        bmiRecords.clear();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM bmi_records WHERE user_email = ? ORDER BY timestamp DESC",
                new String[]{userEmail}
        );

        if (cursor.moveToFirst()) {
            do {
                BMIRecord record = new BMIRecord();
                record.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                record.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                record.setAge(cursor.getInt(cursor.getColumnIndexOrThrow("age")));
                record.setHeight(cursor.getFloat(cursor.getColumnIndexOrThrow("height")));
                record.setWeight(cursor.getFloat(cursor.getColumnIndexOrThrow("weight")));
                record.setBmi(cursor.getFloat(cursor.getColumnIndexOrThrow("bmi")));
                record.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                record.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));

                bmiRecords.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();

        updateUI();
    }

    private void updateUI() {
        if (bmiRecords.isEmpty()) {
            rvBmiHistory.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvRecordCount.setText("0 lần đo");
            btnDeleteAll.setVisibility(View.GONE);
        } else {
            rvBmiHistory.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            tvRecordCount.setText(bmiRecords.size() + " lần đo");
            btnDeleteAll.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new BMIHistoryAdapter(this, bmiRecords, new BMIHistoryAdapter.OnItemClickListener() {
                    @Override
                    public void onDeleteClick(BMIRecord record) {
                        showDeleteConfirmDialog(record);
                    }
                });
                rvBmiHistory.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmDialog());
    }

    private void showDeleteConfirmDialog(BMIRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bản ghi")
                .setMessage("Bạn có chắc muốn xóa bản ghi này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteRecord(record))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteAllConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả")
                .setMessage("Bạn có chắc muốn xóa toàn bộ lịch sử BMI?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAllRecords())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteRecord(BMIRecord record) {
        int rowsDeleted = db.delete("bmi_records",
                "id = ?",
                new String[]{String.valueOf(record.getId())});

        if (rowsDeleted > 0) {
            Toast.makeText(this, "Đã xóa bản ghi", Toast.LENGTH_SHORT).show();
            loadBMIHistory();
        } else {
            Toast.makeText(this, "Lỗi khi xóa bản ghi", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllRecords() {
        int rowsDeleted = db.delete("bmi_records",
                "user_email = ?",
                new String[]{userEmail});

        if (rowsDeleted > 0) {
            Toast.makeText(this, "Đã xóa " + rowsDeleted + " bản ghi", Toast.LENGTH_SHORT).show();
            loadBMIHistory();
        } else {
            Toast.makeText(this, "Lỗi khi xóa dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}