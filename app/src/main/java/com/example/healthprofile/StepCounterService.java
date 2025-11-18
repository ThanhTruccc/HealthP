package com.example.healthprofile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    private static final String TAG = "StepCounterService";
    private static final String CHANNEL_ID = "step_counter_channel";
    private static final int NOTIFICATION_ID = 1001;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private SharedPreferences prefs;

    private int todaySteps = 0;
    private int sensorInitialSteps = 0;
    private boolean isFirstReading = true;
    private String today;

    private static final double CALORIES_PER_STEP = 0.04;
    private static final double METERS_PER_STEP = 0.762;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        prefs = getSharedPreferences("StepCounter", MODE_PRIVATE);

        // Get today's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        today = sdf.format(Calendar.getInstance().getTime());

        // Initialize sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        if (stepSensor == null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }

        // Load saved steps
        loadTodaySteps();

        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());

        // Register sensor listener
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Sensor registered successfully");
        } else {
            Log.e(TAG, "No step sensor available!");
            stopSelf(); // Stop service if no sensor
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
            resetForNewDay();
            savedDate = today;
            prefs.edit().putString("last_date", savedDate).apply();
        }

        if (isFirstReading) {
            sensorInitialSteps = totalStepsSinceBoot;
            isFirstReading = false;

            int savedOffset = prefs.getInt("step_offset_" + today, 0);
            sensorInitialSteps -= savedOffset;
        }

        todaySteps = totalStepsSinceBoot - sensorInitialSteps;
        saveSteps();
        saveTodayStepsToDatabase(); // Lưu vào database ngay lập tức
        updateNotification();

        Log.d(TAG, "Steps updated: " + todaySteps);
    }

    private void handleStepDetector(SensorEvent event) {
        todaySteps++;
        saveSteps();
        saveTodayStepsToDatabase(); // Lưu vào database ngay lập tức
        updateNotification();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void loadTodaySteps() {
        String savedDate = prefs.getString("last_date", "");

        if (today.equals(savedDate)) {
            todaySteps = prefs.getInt("today_steps", 0);
        } else {
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
                .putBoolean("service_running", true)
                .apply();

        // Lưu vào database
        saveTodayStepsToDatabase();
    }

    private void saveTodayStepsToDatabase() {
        try {
            SQLiteDatabase db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

            // Tạo bảng nếu chưa có
            db.execSQL("CREATE TABLE IF NOT EXISTS daily_steps (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT UNIQUE NOT NULL, " +
                    "steps INTEGER DEFAULT 0, " +
                    "calories REAL DEFAULT 0, " +
                    "distance REAL DEFAULT 0, " +
                    "created_at INTEGER)");

            double calories = todaySteps * CALORIES_PER_STEP;
            double distance = (todaySteps * METERS_PER_STEP) / 1000.0;

            // Insert hoặc Update
            db.execSQL("INSERT OR REPLACE INTO daily_steps (date, steps, calories, distance, created_at) " +
                    "VALUES ('" + today + "', " + todaySteps + ", " + calories + ", " +
                    distance + ", " + System.currentTimeMillis() + ")");

            db.close();

            Log.d(TAG, "Saved " + todaySteps + " steps to database for " + today);
        } catch (Exception e) {
            Log.e(TAG, "Error saving to database: " + e.getMessage());
            e.printStackTrace();
        }
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

    private Notification createNotification() {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, StepCounterActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Stop service action
        Intent stopIntent = new Intent(this, StepCounterService.class);
        stopIntent.setAction("STOP_SERVICE");
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        double calories = todaySteps * CALORIES_PER_STEP;
        double distance = (todaySteps * METERS_PER_STEP) / 1000.0;

        String contentText = String.format(Locale.getDefault(),
                "%,d bước | %.1f kcal | %.2f km",
                todaySteps, calories, distance);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🚶 Đang đếm bước chân")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_directions)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dừng", stopPendingIntent)
                .build();
    }

    private void updateNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Đếm bước chân",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Dịch vụ đếm bước chân chạy nền");
            channel.setShowBadge(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        prefs.edit().putBoolean("service_running", false).apply();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Handle stop action
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP_SERVICE".equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }
}