package com.example.healthprofile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

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

public class UnifiedStepActivity extends AppCompatActivity implements SensorEventListener {

    // ========== STEP COUNTER VARIABLES ==========
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorPresent = false;
    private boolean useAccelerometer = false;
    private AccelerometerStepCounter accelStepCounter;

    private TextView tvStepCount, tvCalories, tvDistance, tvGoalProgress;
    private TextView tvSensorMode, tvTargetSteps;
    private ProgressBar progressBar;
    private Button btnResetSteps;
    private ImageButton btnBack, btnSensorInfo;

    private SharedPreferences prefs;
    private int todaySteps = 0;
    private int sensorInitialSteps = 0;
    private boolean isFirstReading = true;
    private String today;

    // ========== STATISTICS VARIABLES ==========
    private TextView btnWeek, btnMonth, btn6Month;
    private TextView tvDateRange, tvAvgSteps, tvCurrentPeriodSteps, tvLastPeriodSteps;
    private BarChart barChart;
    private LinearLayout tabWeek, tabMonth, tab6Month;
    private CardView cardStatistics;
    private ImageButton btnToggleStats;

    private SQLiteDatabase db;
    private String currentPeriod = "week";
    private boolean statsVisible = false;

    // ========== CONSTANTS ==========
    private static final int DAILY_GOAL = 10000;
    private static final double CALORIES_PER_STEP = 0.04;
    private static final double METERS_PER_STEP = 0.762;

    // ========== PERMISSION LAUNCHER ==========
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Đã cấp quyền", Toast.LENGTH_SHORT).show();
                    startStepCounting();
                    startBackgroundService();
                } else {
                    Toast.makeText(this, "Cần quyền để đếm bước chân", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_step);

        prefs = getSharedPreferences("StepCounter", MODE_PRIVATE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        today = sdf.format(Calendar.getInstance().getTime());

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
        createStepsTable();
        syncTodayStepsFromPrefs();

        initViews();
        checkPermissionAndStart();
        setupChart();

        // Load statistics by default
        loadWeekData();
        startAutoRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        refreshCurrentData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (useAccelerometer && accelStepCounter != null) {
            // Keep running
        } else if (isSensorPresent && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (useAccelerometer && accelStepCounter != null) {
            accelStepCounter.stop();
        } else if (isSensorPresent && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // ========== INITIALIZATION ==========

    private void initViews() {
        // Step Counter Views
        btnBack = findViewById(R.id.btn_back_unified);
        tvStepCount = findViewById(R.id.tv_step_count);
        tvCalories = findViewById(R.id.tv_step_calories);
        tvDistance = findViewById(R.id.tv_step_distance);
        tvGoalProgress = findViewById(R.id.tv_goal_progress);
        tvSensorMode = findViewById(R.id.tv_sensor_mode);
        tvTargetSteps = findViewById(R.id.tv_target_steps);
        progressBar = findViewById(R.id.progress_steps);
        btnResetSteps = findViewById(R.id.btn_reset_steps);


        ImageButton btnSensorInfo = findViewById(R.id.btn_sensor_info);
        btnToggleStats = findViewById(R.id.btn_toggle_stats);

        // Statistics Views
        cardStatistics = findViewById(R.id.card_statistics);
        tabWeek = findViewById(R.id.tab_week);
        tabMonth = findViewById(R.id.tab_month);
        tab6Month = findViewById(R.id.tab_6month);
        btnWeek = findViewById(R.id.btn_week);
        btnMonth = findViewById(R.id.btn_month);
        btn6Month = findViewById(R.id.btn_6month);
        tvDateRange = findViewById(R.id.tv_date_range);
        tvAvgSteps = findViewById(R.id.tv_avg_steps);
        tvCurrentPeriodSteps = findViewById(R.id.tv_current_period_steps);
        tvLastPeriodSteps = findViewById(R.id.tv_last_period_steps);
        barChart = findViewById(R.id.bar_chart);

        // Setup Listeners
        btnBack.setOnClickListener(v -> finish());
        btnResetSteps.setOnClickListener(v -> resetSteps());
        btnSensorInfo.setOnClickListener(v -> showDetailedSensorInfo());
        btnToggleStats.setOnClickListener(v -> toggleStatistics());

        tabWeek.setOnClickListener(v -> switchTab("week"));
        tabMonth.setOnClickListener(v -> switchTab("month"));
        tab6Month.setOnClickListener(v -> switchTab("6month"));

        tvTargetSteps.setText("Mục tiêu: " + String.format(Locale.getDefault(), "%,d", DAILY_GOAL) + " bước/ngày");

        loadTodaySteps();
        updateUI();
    }

    private void toggleStatistics() {
        statsVisible = !statsVisible;

        if (statsVisible) {
            cardStatistics.setVisibility(View.VISIBLE);
            btnToggleStats.setImageResource(android.R.drawable.arrow_up_float);
            refreshCurrentData();
        } else {
            cardStatistics.setVisibility(View.GONE);
            btnToggleStats.setImageResource(android.R.drawable.arrow_down_float);
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
            int steps = prefs.getInt("today_steps", 0);
            if (steps > 0) {
                saveTodayStepsToDatabase();
            }
        } catch (Exception e) {
            Log.e("UnifiedStep", "Sync error: " + e.getMessage());
        }
    }

    // ========== SENSOR MANAGEMENT ==========

    private void checkPermissionAndStart() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        if (stepSensor == null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (stepSensor != null) {
            isSensorPresent = true;
            useAccelerometer = false;
            showSensorInfo();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
                } else {
                    startStepCounting();
                    startBackgroundService();
                }
            } else {
                startStepCounting();
                startBackgroundService();
            }
        } else if (accelerometer != null) {
            isSensorPresent = true;
            useAccelerometer = true;
            showAccelerometerMode();
            startAccelerometerCounting();
        } else {
            isSensorPresent = false;
            showNoSensorDialog();
        }
    }

    private void startStepCounting() {
        if (isSensorPresent && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void startAccelerometerCounting() {
        accelStepCounter = new AccelerometerStepCounter(this, steps -> {
            todaySteps++;
            saveSteps();
            saveTodayStepsToDatabase();
            runOnUiThread(() -> updateUI());
        });

        if (accelStepCounter.isAvailable()) {
            accelStepCounter.start();
            if (tvSensorMode != null) {
                tvSensorMode.setVisibility(View.VISIBLE);
                tvSensorMode.setText("• Chế độ: Accelerometer (ước tính)");
            }
            Toast.makeText(this, "Đang dùng Accelerometer để đếm bước", Toast.LENGTH_LONG).show();
        }
    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "Dịch vụ đếm bước đã bật", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            handleStepCounter(event);
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            handleStepDetector(event);
        }
    }

    private void handleStepCounter(SensorEvent event) {
        int totalStepsSinceBoot = (int) event.values[0];

        String savedDate = prefs.getString("last_date", "");
        if (!today.equals(savedDate)) {
            resetForNewDay();
            prefs.edit().putString("last_date", today).apply();
        }

        if (isFirstReading) {
            sensorInitialSteps = totalStepsSinceBoot;
            isFirstReading = false;
            int savedOffset = prefs.getInt("step_offset_" + today, 0);
            sensorInitialSteps -= savedOffset;
        }

        todaySteps = totalStepsSinceBoot - sensorInitialSteps;
        saveSteps();
        saveTodayStepsToDatabase();
        updateUI();
    }

    private void handleStepDetector(SensorEvent event) {
        todaySteps++;
        saveSteps();
        saveTodayStepsToDatabase();
        updateUI();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    // ========== DATA MANAGEMENT ==========

    private void loadTodaySteps() {
        String savedDate = prefs.getString("last_date", "");
        if (today.equals(savedDate)) {
            todaySteps = prefs.getInt("today_steps", 0);
        } else {
            todaySteps = 0;
            prefs.edit().putString("last_date", today).putInt("today_steps", 0).apply();
        }
    }

    private void saveSteps() {
        prefs.edit()
                .putInt("today_steps", todaySteps)
                .putString("last_date", today)
                .apply();
    }

    private void saveTodayStepsToDatabase() {
        try {
            double calories = todaySteps * CALORIES_PER_STEP;
            double distance = (todaySteps * METERS_PER_STEP) / 1000.0;

            db.execSQL("INSERT OR REPLACE INTO daily_steps (date, steps, calories, distance, created_at) " +
                    "VALUES ('" + today + "', " + todaySteps + ", " + calories + ", " +
                    distance + ", " + System.currentTimeMillis() + ")");
        } catch (Exception e) {
            Log.e("UnifiedStep", "DB save error: " + e.getMessage());
        }
    }

    private void resetForNewDay() {
        todaySteps = 0;
        isFirstReading = true;
        sensorInitialSteps = 0;
        prefs.edit().putInt("today_steps", 0).putInt("step_offset_" + today, 0).apply();
    }

    private void resetSteps() {
        new AlertDialog.Builder(this)
                .setTitle("Đặt lại bước chân")
                .setMessage("Bạn có chắc muốn đặt lại số bước chân hôm nay về 0?")
                .setPositiveButton("Đặt lại", (dialog, which) -> {
                    todaySteps = 0;
                    isFirstReading = true;
                    prefs.edit().putInt("today_steps", 0).putInt("step_offset_" + today, 0).apply();
                    updateUI();
                    Toast.makeText(this, "Đã đặt lại về 0 bước", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ========== UI UPDATES ==========

    private void updateUI() {
        tvStepCount.setText(String.format(Locale.getDefault(), "%,d", todaySteps));

        double calories = todaySteps * CALORIES_PER_STEP;
        tvCalories.setText(String.format(Locale.getDefault(), "%.1f kcal", calories));

        double distanceKm = (todaySteps * METERS_PER_STEP) / 1000.0;
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));

        int progress = Math.min((int) ((todaySteps * 100.0) / DAILY_GOAL), 100);
        progressBar.setProgress(progress);
        tvGoalProgress.setText(progress + "%");

        if (progress >= 100) {
            tvGoalProgress.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (progress >= 50) {
            tvGoalProgress.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvGoalProgress.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void startAutoRefresh() {
        new Thread(() -> {
            while (!isFinishing()) {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(() -> {
                        loadTodaySteps();
                        updateUI();
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    // ========== STATISTICS ==========

    private void setupChart() {
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(false);
        barChart.setTouchEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraBottomOffset(10f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#999999"));
        xAxis.setTextSize(12f);
        xAxis.setGranularity(1f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextColor(Color.parseColor("#999999"));
        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
    }

    private void refreshCurrentData() {
        syncTodayStepsFromPrefs();
        switch (currentPeriod) {
            case "week": loadWeekData(); break;
            case "month": loadMonthData(); break;
            case "6month": load6MonthData(); break;
        }
    }

    private void switchTab(String period) {
        currentPeriod = period;

        tabWeek.setBackgroundResource(R.drawable.tab_unselected_bg);
        tabMonth.setBackgroundResource(R.drawable.tab_unselected_bg);
        tab6Month.setBackgroundResource(R.drawable.tab_unselected_bg);

        int grayColor = Color.parseColor("#999999");
        btnWeek.setTextColor(grayColor);
        btnMonth.setTextColor(grayColor);
        btn6Month.setTextColor(grayColor);

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

    private void loadWeekData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        String startDate = displaySdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String endDate = displaySdf.format(calendar.getTime());
        tvDateRange.setText(startDate + " - " + endDate);

        calendar.add(Calendar.DAY_OF_WEEK, -6);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int totalSteps = 0, daysWithData = 0;

        for (int i = 0; i < 7; i++) {
            String date = sdf.format(calendar.getTime());
            int steps = getStepsForDate(date);
            if (steps > 0) daysWithData++;
            totalSteps += steps;
            entries.add(new BarEntry(i, steps));
            labels.add("T" + (i + 2));
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        updateChart(entries, labels);

        int avgSteps = daysWithData > 0 ? totalSteps / daysWithData : 0;
        tvAvgSteps.setText(String.format(Locale.getDefault(), "%,d", avgSteps));
        tvCurrentPeriodSteps.setText(String.format(Locale.getDefault(), "%,d bước/ngày", avgSteps));

        int lastWeekAvg = calculateLastWeekAverage();
        tvLastPeriodSteps.setText(String.format(Locale.getDefault(), "Tuần trước: %,d bước/ngày", lastWeekAvg));
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

        for (int week = 0; week < 4; week++) {
            int weekSteps = 0;
            for (int day = 0; day < 7; day++) {
                int dayOfMonth = week * 7 + day + 1;
                if (dayOfMonth <= daysInMonth) {
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    weekSteps += getStepsForDate(sdf.format(calendar.getTime()));
                }
            }
            totalSteps += weekSteps;
            entries.add(new BarEntry(week, weekSteps));
            labels.add("T" + (week + 1));
        }

        updateChart(entries, labels);

        int avgSteps = daysInMonth > 0 ? totalSteps / daysInMonth : 0;
        tvAvgSteps.setText(String.format(Locale.getDefault(), "%,d", avgSteps));
        tvCurrentPeriodSteps.setText(String.format(Locale.getDefault(), "%,d bước/ngày", avgSteps));

        int lastMonthAvg = calculateLastMonthAverage();
        tvLastPeriodSteps.setText(String.format(Locale.getDefault(), "Tháng trước: %,d bước/ngày", lastMonthAvg));
    }

    private void load6MonthData() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        tvDateRange.setText("6 tháng gần nhất");

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int totalSteps = 0, totalDays = 0;

        calendar.add(Calendar.MONTH, -5);

        for (int i = 0; i < 6; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            int monthSteps = 0;

            for (int day = 1; day <= daysInMonth; day++) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                monthSteps += getStepsForDate(sdf.format(calendar.getTime()));
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
        tvCurrentPeriodSteps.setText(String.format(Locale.getDefault(), "%,d bước/ngày", avgSteps));
        tvLastPeriodSteps.setText("Trung bình 6 tháng");
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

    private int calculateLastWeekAverage() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -14);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int totalSteps = 0, daysWithData = 0;
        for (int i = 0; i < 7; i++) {
            int steps = getStepsForDate(sdf.format(calendar.getTime()));
            totalSteps += steps;
            if (steps > 0) daysWithData++;
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return daysWithData > 0 ? totalSteps / daysWithData : 0;
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
            totalSteps += getStepsForDate(sdf.format(calendar.getTime()));
        }
        return daysInMonth > 0 ? totalSteps / daysInMonth : 0;
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

    // ========== DIALOGS ==========

    private void showSensorInfo() {
        String sensorType = stepSensor.getType() == Sensor.TYPE_STEP_COUNTER ?
                "Step Counter (Chính xác)" : "Step Detector (Ước tính)";

        new AlertDialog.Builder(this)
                .setTitle("✅ Cảm biến được hỗ trợ")
                .setMessage("Thiết bị của bạn hỗ trợ đếm bước chân!\n\n" +
                        "📱 Loại cảm biến: " + sensorType + "\n" +
                        "🔧 Tên: " + stepSensor.getName() + "\n\n" +
                        "Ứng dụng sẽ đếm bước ngay cả khi đóng app.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAccelerometerMode() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Chế độ Accelerometer")
                .setMessage("Thiết bị của bạn KHÔNG có cảm biến bước chân chuyên dụng.\n\n" +
                        "✅ Giải pháp: Sử dụng cảm biến gia tốc (Accelerometer) để ước tính số bước.\n\n" +
                        "⚠️ Lưu ý:\n" +
                        "• Độ chính xác thấp hơn (~85-90%)\n" +
                        "• Tốn pin hơn\n" +
                        "• Cần giữ điện thoại trong túi/đeo người")
                .setPositiveButton("Sử dụng", null)
                .setNegativeButton("Xem chi tiết", (dialog, which) -> showDetailedSensorInfo())
                .setCancelable(false)
                .show();
    }

    private void showNoSensorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("❌ Không hỗ trợ")
                .setMessage("Thiết bị của bạn KHÔNG có cảm biến bước chân.\n\n" +
                        "Các thiết bị thường không có cảm biến:\n" +
                        "• Máy tính bảng\n" +
                        "• Điện thoại cũ (trước 2014)\n" +
                        "• Máy ảo/Emulator\n\n" +
                        "Vui lòng sử dụng thiết bị khác có cảm biến bước chân.")
                .setPositiveButton("Đóng", (dialog, which) -> finish())
                .setCancelable(false)
                .show();

        tvStepCount.setText("N/A");
        btnResetSteps.setEnabled(false);
    }

    private void showDetailedSensorInfo() {
        String info = SensorChecker.getSensorInfo(this);

        new AlertDialog.Builder(this)
                .setTitle("📱 Thông tin cảm biến")
                .setMessage(info)
                .setPositiveButton("OK", null)
                .setNeutralButton("Xem tất cả sensors", (dialog, which) -> showAllSensors())
                .show();
    }

    private void showAllSensors() {
        String allSensors = SensorChecker.getAllSensors(this);

        new AlertDialog.Builder(this)
                .setTitle("🔧 Tất cả cảm biến thiết bị")
                .setMessage(allSensors)
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Sao chép", (dialog, which) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip =
                            android.content.ClipData.newPlainText("Sensor Info", allSensors);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Đã sao chép vào clipboard", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}