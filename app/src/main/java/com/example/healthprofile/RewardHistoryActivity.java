package com.example.healthprofile;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.RewardHistoryAdapter;
import com.example.healthprofile.model.RewardPoint;

import java.util.ArrayList;
import java.util.List;

public class RewardHistoryActivity extends AppCompatActivity {

    private TextView tvTotalPoints;
    private RecyclerView recyclerView;
    private ImageButton btnBack;

    private SQLiteDatabase db;
    private String userEmail;
    private RewardHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_history);

        // Lấy email
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        // Mở database
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        initViews();
        loadRewardHistory();
    }

    private void initViews() {
        tvTotalPoints = findViewById(R.id.tv_total_points_history);
        recyclerView = findViewById(R.id.rv_reward_history);
        btnBack = findViewById(R.id.btn_back_reward);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadRewardHistory() {
        // Lấy tổng điểm
        int totalPoints = getTotalPoints();
        tvTotalPoints.setText(totalPoints + " điểm");

        // Lấy lịch sử
        List<RewardPoint> history = getRewardHistory();
        adapter = new RewardHistoryAdapter(this, history);
        recyclerView.setAdapter(adapter);
    }

    private int getTotalPoints() {
        String sql = "SELECT COALESCE(SUM(points_change), 0) FROM reward_points " +
                "WHERE user_email = '" + escapeString(userEmail) + "'";
        Cursor cursor = db.rawQuery(sql, null);

        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(0);
        }
        cursor.close();
        return Math.max(0, points);
    }

    private List<RewardPoint> getRewardHistory() {
        List<RewardPoint> history = new ArrayList<>();

        String sql = "SELECT * FROM reward_points " +
                "WHERE user_email = '" + escapeString(userEmail) + "' " +
                "ORDER BY timestamp DESC";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                RewardPoint rp = new RewardPoint();
                rp.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                rp.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
                rp.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow("points")));
                rp.setActionn(cursor.getString(cursor.getColumnIndexOrThrow("actionn")));
                rp.setPointsChange(cursor.getInt(cursor.getColumnIndexOrThrow("points_change")));
                rp.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                rp.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                history.add(rp);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return history;
    }

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