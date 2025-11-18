package com.example.healthprofile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorPresent = false;
    private boolean useAccelerometer = false; // Dùng accelerometer thay thế
    private AccelerometerStepCounter accelStepCounter;

    private TextView tvStepCount;
    private TextView tvCalories;
    private TextView tvDistance;
    private TextView tvGoalProgress;
    private TextView tvSensorMode;
    private ProgressBar progressBar;
    private CardView cardStepCounter;
    private Button btnResetSteps;
    private Button btnSensorInfo;
    private ImageButton btnBack;

    private SharedPreferences prefs;
    private int todaySteps = 0;
    private int sensorInitialSteps = 0;
    private boolean isFirstReading = true;
    private String today;

    // Constants
    private static final int DAILY_GOAL = 10000; // WHO recommends 10,000 steps/day
    private static final double CALORIES_PER_STEP = 0.04; // Average calories per step
    private static final double METERS_PER_STEP = 0.762; // Average step length

    // Permission launcher for Android 10+
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
        setContentView(R.layout.activity_step_counter);

        prefs = getSharedPreferences("StepCounter", MODE_PRIVATE);

        // Get today's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        today = sdf.format(Calendar.getInstance().getTime());

        // Tạo bảng database nếu chưa có
        createStepsTableIfNeeded();

        initViews();
        checkPermissionAndStart();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_steps);
        tvStepCount = findViewById(R.id.tv_step_count);
        tvCalories = findViewById(R.id.tv_step_calories);
        tvDistance = findViewById(R.id.tv_step_distance);
        tvGoalProgress = findViewById(R.id.tv_goal_progress);
        tvSensorMode = findViewById(R.id.tv_sensor_mode);
        progressBar = findViewById(R.id.progress_steps);
        cardStepCounter = findViewById(R.id.card_step_counter);
        btnResetSteps = findViewById(R.id.btn_reset_steps);
        btnSensorInfo = findViewById(R.id.btn_sensor_info);

        btnBack.setOnClickListener(v -> finish());
        btnResetSteps.setOnClickListener(v -> resetSteps());
        btnSensorInfo.setOnClickListener(v -> showDetailedSensorInfo());

        // Load saved steps every time UI updates
        loadTodaySteps();
        updateUI();

        // Auto-refresh UI every 2 seconds to sync with service
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        new Thread(() -> {
            while (!isFinishing()) {
                try {
                    Thread.sleep(2000); // Refresh every 2 seconds
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

    private void checkPermissionAndStart() {
        // Check if device has step sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Log.d("StepCounter", "=== Checking Sensors ===");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            Log.d("StepCounter", "TYPE_STEP_COUNTER: " + (stepSensor != null ? "YES" : "NO"));
        }

        if (stepSensor == null) {
            // Try step detector as fallback
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            Log.d("StepCounter", "TYPE_STEP_DETECTOR: " + (stepSensor != null ? "YES" : "NO"));
        }

        // Check accelerometer
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.d("StepCounter", "TYPE_ACCELEROMETER: " + (accelerometer != null ? "YES" : "NO"));

        if (stepSensor != null) {
            // Có step sensor - sử dụng bình thường
            Log.d("StepCounter", "Using Step Sensor");
            isSensorPresent = true;
            useAccelerometer = false;
            showSensorInfo();

            // Check permission for Android 10+ (API 29+)
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
            // Không có step sensor - thử dùng accelerometer
            Log.d("StepCounter", "No Step Sensor - Using Accelerometer");
            isSensorPresent = true;
            useAccelerometer = true;
            showAccelerometerMode();
            startAccelerometerCounting();
        } else {
            // Không có gì cả
            Log.d("StepCounter", "No sensors available!");
            isSensorPresent = false;
            useAccelerometer = false;
            showNoSensorDialog();
        }

        Log.d("StepCounter", "isSensorPresent: " + isSensorPresent);
        Log.d("StepCounter", "useAccelerometer: " + useAccelerometer);
    }

    private void showAccelerometerMode() {
        Log.d("StepCounter", "showAccelerometerMode() called");

        Toast.makeText(this, "Samsung A20 phát hiện - Dùng Accelerometer", Toast.LENGTH_LONG).show();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Chế độ Accelerometer")
                .setMessage("Thiết bị của bạn KHÔNG có cảm biến bước chân chuyên dụng.\n\n" +
                        "📱 Thiết bị: Samsung A20 (hoặc tương tự)\n\n" +
                        "✅ Giải pháp: Sử dụng cảm biến gia tốc (Accelerometer) để ước tính số bước.\n\n" +
                        "⚠️ Lưu ý:\n" +
                        "• Độ chính xác thấp hơn (~85-90%)\n" +
                        "• Tốn pin hơn\n" +
                        "• Cần giữ điện thoại trong túi/đeo người\n\n" +
                        "💡 Mẹo: Điện thoại ở tư thế đứng (vertical) sẽ chính xác hơn.")
                .setPositiveButton("Sử dụng", (dialog, which) -> {
                    Log.d("StepCounter", "User accepted Accelerometer mode");
                })
                .setNegativeButton("Xem chi tiết", (dialog, which) -> {
                    showDetailedSensorInfo();
                })
                .setCancelable(false)
                .show();

        Log.d("StepCounter", "Dialog should be showing now");
    }

    private void startAccelerometerCounting() {
        Log.d("StepCounter", "startAccelerometerCounting() called");

        accelStepCounter = new AccelerometerStepCounter(this, new AccelerometerStepCounter.StepListener() {
            @Override
            public void onStep(int steps) {
                Log.d("StepCounter", "Step detected by Accelerometer!");
                todaySteps++;
                saveSteps();
                saveTodayStepsToDatabase(); // Lưu vào database
                runOnUiThread(() -> updateUI());
            }
        });

        if (accelStepCounter.isAvailable()) {
            accelStepCounter.start();

            // Hiển thị indicator
            if (tvSensorMode != null) {
                tvSensorMode.setVisibility(View.VISIBLE);
                tvSensorMode.setText("• Chế độ: Accelerometer (Ước tính)");
            }

            Toast.makeText(this, "Đang dùng Accelerometer để đếm bước", Toast.LENGTH_LONG).show();
            Log.d("StepCounter", "Accelerometer started successfully");
        } else {
            Log.e("StepCounter", "Accelerometer not available!");
            Toast.makeText(this, "Không thể khởi động Accelerometer", Toast.LENGTH_LONG).show();
        }
    }

    private void createStepsTableIfNeeded() {
        try {
            SQLiteDatabase db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS daily_steps (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT UNIQUE NOT NULL, " +
                    "steps INTEGER DEFAULT 0, " +
                    "calories REAL DEFAULT 0, " +
                    "distance REAL DEFAULT 0, " +
                    "created_at INTEGER)";
            db.execSQL(sql);
            db.close();
        } catch (Exception e) {
            Log.e("StepCounter", "Error creating table: " + e.getMessage());
        }
    }

    private void showSensorInfo() {
        String sensorType = "";
        String sensorName = stepSensor.getName();

        if (stepSensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            sensorType = "Step Counter (Chính xác)";
        } else if (stepSensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            sensorType = "Step Detector (Ước tính)";
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("✅ Cảm biến được hỗ trợ")
                .setMessage("Thiết bị của bạn hỗ trợ đếm bước chân!\n\n" +
                        "📱 Loại cảm biến: " + sensorType + "\n" +
                        "🔧 Tên: " + sensorName + "\n\n" +
                        "Ứng dụng sẽ đếm bước ngay cả khi đóng app.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showNoSensorDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
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

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Toast.makeText(this, "Dịch vụ đếm bước đã bật", Toast.LENGTH_SHORT).show();
    }

    private void stopBackgroundService() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        stopService(serviceIntent);
    }

    private void showDetailedSensorInfo() {
        String info = SensorChecker.getSensorInfo(this);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📱 Thông tin cảm biến")
                .setMessage(info)
                .setPositiveButton("OK", null)
                .setNeutralButton("Xem tất cả sensors", (dialog, which) -> {
                    showAllSensors();
                })
                .show();
    }

    private void showAllSensors() {
        String allSensors = SensorChecker.getAllSensors(this);

        new androidx.appcompat.app.AlertDialog.Builder(this)
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

    private void startStepCounting() {
        if (isSensorPresent && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
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

        // Check if it's a new day
        String savedDate = prefs.getString("last_date", "");
        if (!today.equals(savedDate)) {
            // New day - reset
            resetForNewDay();
            savedDate = today;
            prefs.edit().putString("last_date", savedDate).apply();
        }

        if (isFirstReading) {
            // First reading - save initial value
            sensorInitialSteps = totalStepsSinceBoot;
            isFirstReading = false;

            // Load saved offset for today
            int savedOffset = prefs.getInt("step_offset_" + today, 0);
            sensorInitialSteps -= savedOffset;
        }

        // Calculate today's steps
        todaySteps = totalStepsSinceBoot - sensorInitialSteps;

        // Save current steps
        saveSteps();
        saveTodayStepsToDatabase(); // Lưu vào database
        updateUI();
    }

    private void handleStepDetector(SensorEvent event) {
        // TYPE_STEP_DETECTOR fires an event for each step
        todaySteps++;
        saveSteps();
        saveTodayStepsToDatabase(); // Lưu vào database
        updateUI();
    }

    private void saveTodayStepsToDatabase() {
        try {
            double calories = todaySteps * CALORIES_PER_STEP;
            double distance = (todaySteps * METERS_PER_STEP) / 1000.0;

            String sql = "INSERT OR REPLACE INTO daily_steps (date, steps, calories, distance, created_at) " +
                    "VALUES ('" + today + "', " + todaySteps + ", " + calories + ", " +
                    distance + ", " + System.currentTimeMillis() + ")";

            SQLiteDatabase db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
            db.execSQL(sql);
            db.close();
        } catch (Exception e) {
            Log.e("StepCounter", "Error saving to database: " + e.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void loadTodaySteps() {
        String savedDate = prefs.getString("last_date", "");

        if (today.equals(savedDate)) {
            // Same day - load saved steps
            todaySteps = prefs.getInt("today_steps", 0);
        } else {
            // New day - reset
            todaySteps = 0;
            prefs.edit()
                    .putString("last_date", today)
                    .putInt("today_steps", 0)
                    .apply();
        }
    }

    private void saveSteps() {
        prefs.edit()
                .putInt("today_steps", todaySteps)
                .putString("last_date", today)
                .apply();
    }

    private void updateUI() {
        // Update step count
        tvStepCount.setText(String.format(Locale.getDefault(), "%,d", todaySteps));

        // Calculate and update calories
        double calories = todaySteps * CALORIES_PER_STEP;
        tvCalories.setText(String.format(Locale.getDefault(), "%.1f kcal", calories));

        // Calculate and update distance
        double distanceMeters = todaySteps * METERS_PER_STEP;
        double distanceKm = distanceMeters / 1000.0;
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));

        // Update progress
        int progress = (int) ((todaySteps * 100.0) / DAILY_GOAL);
        progress = Math.min(progress, 100); // Cap at 100%
        progressBar.setProgress(progress);
        tvGoalProgress.setText(progress + "%");

        // Update progress text color based on achievement
        if (progress >= 100) {
            tvGoalProgress.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (progress >= 50) {
            tvGoalProgress.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvGoalProgress.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void resetSteps() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đặt lại bước chân")
                .setMessage("Bạn có chắc muốn đặt lại số bước chân hôm nay về 0?")
                .setPositiveButton("Đặt lại", (dialog, which) -> {
                    todaySteps = 0;
                    isFirstReading = true;

                    prefs.edit()
                            .putInt("today_steps", 0)
                            .putInt("step_offset_" + today, 0)
                            .apply();

                    updateUI();
                    Toast.makeText(this, "Đã đặt lại về 0 bước", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void resetForNewDay() {
        todaySteps = 0;
        isFirstReading = true;
        sensorInitialSteps = 0;

        prefs.edit()
                .putInt("today_steps", 0)
                .putInt("step_offset_" + today, 0)
                .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (useAccelerometer && accelStepCounter != null) {
            // Không stop accelerometer vì cần chạy liên tục
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
        // Service continues running in background
    }
}