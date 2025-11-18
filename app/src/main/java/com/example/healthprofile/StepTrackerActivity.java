package com.example.healthprofile;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StepTrackerActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView btnWeek, btnMonth, btn6Month;
    private TextView tvDateRange, tvAvgSteps, tvCurrentWeekSteps, tvLastWeekSteps;
    private TextView tvTargetSteps;
    private BarChart barChart;
    private LinearLayout tabWeek, tabMonth, tab6Month;

    private SQLiteDatabase db;
    private String currentPeriod = "week"; // week, month, 6month
    private static final int DAILY_TARGET = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_tracker);

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
        createStepsTable();

        // Đồng bộ dữ liệu từ SharedPreferences
        syncTodayStepsFromPrefs();

        initViews();
        setupChart();
        loadWeekData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dữ liệu khi quay lại activity
        refreshCurrentData();
    }

    private void refreshCurrentData() {
        switch (currentPeriod) {
            case "week":
                loadWeekData();
                break;
            case "month":
                loadMonthData();
                break;
            case "6month":
                load6MonthData();
                break;
        }
    }

    private void createStepsTable() {
        db.execSQL("CREATE TABLE IF NOT EXISTS daily_steps (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT UNIQUE NOT NULL, " +
                "steps INTEGER DEFAULT 0, " +
                "calories REAL DEFAULT 0, " +
                "distance REAL DEFAULT 0, " +
                "created_at INTEGER)");
    }

    private void syncTodayStepsFromPrefs() {
        try {
            SharedPreferences prefs = getSharedPreferences("StepCounter", MODE_PRIVATE);
            int todaySteps = prefs.getInt("today_steps", 0);

            if (todaySteps > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String today = sdf.format(Calendar.getInstance().getTime());

                double calories = todaySteps * 0.04;
                double distance = todaySteps * 0.762 / 1000.0;

                db.execSQL("INSERT OR REPLACE INTO daily_steps (date, steps, calories, distance, created_at) " +
                        "VALUES ('" + today + "', " + todaySteps + ", " + calories + ", " +
                        distance + ", " + System.currentTimeMillis() + ")");

                Log.d("StepTracker", "Synced " + todaySteps + " steps from SharedPreferences");
            }
        } catch (Exception e) {
            Log.e("StepTracker", "Error syncing: " + e.getMessage());
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_tracker);
        ImageButton btnSync = findViewById(R.id.btn_sync_data);

        tabWeek = findViewById(R.id.tab_week);
        tabMonth = findViewById(R.id.tab_month);
        tab6Month = findViewById(R.id.tab_6month);

        btnWeek = findViewById(R.id.btn_week);
        btnMonth = findViewById(R.id.btn_month);
        btn6Month = findViewById(R.id.btn_6month);

        tvDateRange = findViewById(R.id.tv_date_range);
        tvAvgSteps = findViewById(R.id.tv_avg_steps);
        tvCurrentWeekSteps = findViewById(R.id.tv_current_week_steps);
        tvLastWeekSteps = findViewById(R.id.tv_last_week_steps);
        tvTargetSteps = findViewById(R.id.tv_target_steps);
        barChart = findViewById(R.id.bar_chart);

        btnBack.setOnClickListener(v -> finish());
        btnSync.setOnClickListener(v -> syncAndRefresh());

        tabWeek.setOnClickListener(v -> switchTab("week"));
        tabMonth.setOnClickListener(v -> switchTab("month"));
        tab6Month.setOnClickListener(v -> switchTab("6month"));

        tvTargetSteps.setText("Mục tiêu: " + String.format(Locale.getDefault(), "%,d", DAILY_TARGET) + " bước/ngày");
    }

    private void syncAndRefresh() {
        // Đồng bộ dữ liệu từ SharedPreferences
        syncTodayStepsFromPrefs();

        // Xóa dữ liệu cũ hơn 90 ngày
        cleanOldData();

        // Refresh UI
        refreshCurrentData();

        Toast.makeText(this, "Đã đồng bộ dữ liệu", Toast.LENGTH_SHORT).show();
    }

    private void cleanOldData() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -90);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String cutoffDate = sdf.format(calendar.getTime());

            db.execSQL("DELETE FROM daily_steps WHERE date < '" + cutoffDate + "'");

            Log.d("StepTracker", "Cleaned old data before " + cutoffDate);
        } catch (Exception e) {
            Log.e("StepTracker", "Error cleaning old data: " + e.getMessage());
        }
    }

    private int getStepsForDate(String date) {
        Cursor cursor = db.rawQuery("SELECT steps FROM daily_steps WHERE date = '" + date + "'", null);

        int steps = 0;
        if (cursor.moveToFirst()) {
            steps = cursor.getInt(0);
        }
        cursor.close();
        return steps;
    }

    private void switchTab(String period) {
        currentPeriod = period;

        // Reset all tabs
        tabWeek.setBackgroundResource(R.drawable.tab_unselected_bg);
        tabMonth.setBackgroundResource(R.drawable.tab_unselected_bg);
        tab6Month.setBackgroundResource(R.drawable.tab_unselected_bg);

        btnWeek.setTextColor(Color.parseColor("#999999"));
        btnMonth.setTextColor(Color.parseColor("#999999"));
        btn6Month.setTextColor(Color.parseColor("#999999"));

        // Highlight selected tab
        switch (period) {
            case "week":
                tabWeek.setBackgroundResource(R.drawable.tab_selected_bg);
                btnWeek.setTextColor(Color.WHITE);
                loadWeekData();
                break;
            case "month":
                tabMonth.setBackgroundResource(R.drawable.tab_selected_bg);
                btnMonth.setTextColor(Color.WHITE);
                loadMonthData();
                break;
            case "6month":
                tab6Month.setBackgroundResource(R.drawable.tab_selected_bg);
                btn6Month.setTextColor(Color.WHITE);
                load6MonthData();
                break;
        }
    }

    private void setupChart() {
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(false);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraBottomOffset(10f);

        // X Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#999999"));
        xAxis.setTextSize(12f);
        xAxis.setGranularity(1f);

        // Left Y Axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#333333"));
        leftAxis.setTextColor(Color.parseColor("#999999"));
        leftAxis.setAxisMinimum(0f);

        // Right Y Axis
        barChart.getAxisRight().setEnabled(false);
    }

    private void loadWeekData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        String startDate = displaySdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String endDate = displaySdf.format(calendar.getTime());

        tvDateRange.setText(startDate + " - " + endDate);

        // Reset về đầu tuần
        calendar.add(Calendar.DAY_OF_WEEK, -6);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int totalSteps = 0;
        int daysWithData = 0;

        for (int i = 0; i < 7; i++) {
            String date = sdf.format(calendar.getTime());

            // Query trực tiếp
            Cursor cursor = db.rawQuery("SELECT steps FROM daily_steps WHERE date = '" + date + "'", null);

            int steps = 0;
            if (cursor.moveToFirst()) {
                steps = cursor.getInt(0);
                if (steps > 0) daysWithData++;
            }
            cursor.close();

            totalSteps += steps;
            entries.add(new BarEntry(i, steps));
            labels.add("T" + (i + 2)); // T2, T3, T4...

            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        updateChart(entries, labels);

        // Cập nhật statistics
        int avgSteps = daysWithData > 0 ? totalSteps / daysWithData : 0;
        tvAvgSteps.setText(String.format(Locale.getDefault(), "%,d", avgSteps));
        tvCurrentWeekSteps.setText(String.format(Locale.getDefault(), "%,d bước/ngày", avgSteps));

        // Calculate last week average
        int lastWeekAvg = calculateLastWeekAverage();
        tvLastWeekSteps.setText(String.format(Locale.getDefault(), "%,d bước/ngày", lastWeekAvg));

        Log.d("StepTracker", "Week Summary: Total=" + totalSteps + ", Avg=" + avgSteps);
    }

    private int calculateLastWeekAverage() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -14); // 2 tuần trước
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int totalSteps = 0;
        int daysWithData = 0;

        for (int i = 0; i < 7; i++) {
            String date = sdf.format(calendar.getTime());

            Cursor cursor = db.rawQuery("SELECT steps FROM daily_steps WHERE date = '" + date + "'", null);

            if (cursor.moveToFirst()) {
                int steps = cursor.getInt(0);
                totalSteps += steps;
                if (steps > 0) daysWithData++;
            }
            cursor.close();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return daysWithData > 0 ? totalSteps / daysWithData : 0;
    }

    private void loadMonthData() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        tvDateRange.setText("Tháng " + (calendar.get(Calendar.MONTH) + 1));

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int totalSteps = 0;

        // Chia thành 4 tuần
        for (int week = 0; week < 4; week++) {
            int weekSteps = 0;

            for (int day = 0; day < 7; day++) {
                int dayOfMonth = week * 7 + day + 1;
                if (dayOfMonth <= daysInMonth) {
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String date = sdf.format(calendar.getTime());

                    Cursor cursor = db.rawQuery("SELECT steps FROM daily_steps WHERE date = '" + date + "'", null);

                    if (cursor.moveToFirst()) {
                        weekSteps += cursor.getInt(0);
                    }
                    cursor.close();
                }
            }

            totalSteps += weekSteps;
            entries.add(new BarEntry(week, weekSteps));
            labels.add("T" + (week + 1));
        }

        updateChart(entries, labels);

        int avgSteps = daysInMonth > 0 ? totalSteps / daysInMonth : 0;
        tvAvgSteps.setText(String.format(Locale.getDefault(), "%,d", avgSteps));
        tvCurrentWeekSteps.setText(String.format(Locale.getDefault(), "%,d bước/ngày", avgSteps));

        int lastMonthAvg = calculateLastMonthAverage();
        tvLastWeekSteps.setText(String.format(Locale.getDefault(), "%,d bước/ngày", lastMonthAvg));
    }

    private int calculateLastMonthAverage() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int totalSteps = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            calendar.set(Calendar.DAY_OF_MONTH, day);
            String date = sdf.format(calendar.getTime());

            Cursor cursor = db.rawQuery("SELECT steps FROM daily_steps WHERE date = '" + date + "'", null);

            if (cursor.moveToFirst()) {
                totalSteps += cursor.getInt(0);
            }
            cursor.close();
        }

        return daysInMonth > 0 ? totalSteps / daysInMonth : 0;
    }

    private void load6MonthData() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        tvDateRange.setText("6 tháng gần nhất");

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int totalSteps = 0;
        int totalDays = 0;

        // Lùi về 6 tháng trước
        calendar.add(Calendar.MONTH, -5);

        for (int i = 0; i < 6; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            int monthSteps = 0;

            for (int day = 1; day <= daysInMonth; day++) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                String date = sdf.format(calendar.getTime());

                Cursor cursor = db.rawQuery("SELECT steps FROM daily_steps WHERE date = '" + date + "'", null);

                if (cursor.moveToFirst()) {
                    monthSteps += cursor.getInt(0);
                }
                cursor.close();
            }

            totalSteps += monthSteps;
            totalDays += daysInMonth;
            entries.add(new BarEntry(i, monthSteps));
            labels.add("T" + (calendar.get(Calendar.MONTH) + 1));

            calendar.add(Calendar.MONTH, 1);
        }

        updateChart(entries, labels);

        int avgSteps = totalDays > 0 ? totalSteps / totalDays : 0;
        tvAvgSteps.setText(String.format(Locale.getDefault(), "%,d", avgSteps));
        tvCurrentWeekSteps.setText(String.format(Locale.getDefault(), "%,d bước/ngày", avgSteps));
        tvLastWeekSteps.setText("Trung bình 6 tháng");
    }

    private void updateChart(List<BarEntry> entries, List<String> labels) {
        BarDataSet dataSet = new BarDataSet(entries, "Steps");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.TRANSPARENT);
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.animateY(500);
        barChart.invalidate();
    }

    // Public static method để lưu từ bất kỳ đâu
    public static void saveDailySteps(Context context, String date, int steps) {
        try {
            SQLiteDatabase db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

            double calories = steps * 0.04;
            double distance = steps * 0.762 / 1000.0;

            db.execSQL("INSERT OR REPLACE INTO daily_steps (date, steps, calories, distance, created_at) " +
                    "VALUES ('" + date + "', " + steps + ", " + calories + ", " +
                    distance + ", " + System.currentTimeMillis() + ")");

            db.close();
        } catch (Exception e) {
            Log.e("StepTracker", "Error saving steps: " + e.getMessage());
        }
    }

    // Public static method để lấy dữ liệu
    public static int getStepsForDate(Context context, String date) {
        try {
            SQLiteDatabase db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

            Cursor cursor = db.rawQuery("SELECT steps FROM daily_steps WHERE date = '" + date + "'", null);

            int steps = 0;
            if (cursor.moveToFirst()) {
                steps = cursor.getInt(0);
            }
            cursor.close();
            db.close();

            return steps;
        } catch (Exception e) {
            return 0;
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