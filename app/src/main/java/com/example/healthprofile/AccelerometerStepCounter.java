package com.example.healthprofile;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Đếm bước chân bằng Accelerometer cho thiết bị không có Step Counter
 * Dùng thuật toán phát hiện đỉnh (Peak Detection)
 */
public class AccelerometerStepCounter implements SensorEventListener {

    private static final String TAG = "AccelStepCounter";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private StepListener stepListener;

    // Threshold và tham số
    private static final float STEP_THRESHOLD = 11.0f; // Ngưỡng phát hiện bước
    private static final long STEP_DELAY_NS = 250000000L; // 250ms giữa các bước

    private float lastMagnitude = 0;
    private long lastStepTimeNs = 0;
    private boolean isAboveThreshold = false;

    // Smooth filter
    private static final int SMOOTHING_WINDOW = 10;
    private float[] magnitudeHistory = new float[SMOOTHING_WINDOW];
    private int historyIndex = 0;

    public interface StepListener {
        void onStep(int totalSteps);
    }

    public AccelerometerStepCounter(Context context, StepListener listener) {
        this.stepListener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    public boolean isAvailable() {
        return accelerometer != null;
    }

    public void start() {
        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            Log.d(TAG, "Accelerometer step counter started");
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Accelerometer step counter stopped");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectStep(event.values[0], event.values[1], event.values[2], event.timestamp);
        }
    }

    private void detectStep(float x, float y, float z, long timestamp) {
        // Tính độ lớn vector gia tốc
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        // Làm mượt dữ liệu
        magnitudeHistory[historyIndex] = magnitude;
        historyIndex = (historyIndex + 1) % SMOOTHING_WINDOW;

        float smoothedMagnitude = getSmoothedMagnitude();

        // Phát hiện đỉnh (Peak Detection)
        if (smoothedMagnitude > STEP_THRESHOLD && !isAboveThreshold) {
            // Đi lên qua ngưỡng
            isAboveThreshold = true;

        } else if (smoothedMagnitude < STEP_THRESHOLD && isAboveThreshold) {
            // Đi xuống qua ngưỡng - Phát hiện 1 bước
            isAboveThreshold = false;

            // Kiểm tra thời gian giữa các bước (tránh đếm nhiều lần)
            long currentTime = timestamp;
            if (currentTime - lastStepTimeNs > STEP_DELAY_NS) {
                lastStepTimeNs = currentTime;

                if (stepListener != null) {
                    stepListener.onStep(1);
                }

                Log.d(TAG, "Step detected! Magnitude: " + smoothedMagnitude);
            }
        }

        lastMagnitude = smoothedMagnitude;
    }

    private float getSmoothedMagnitude() {
        float sum = 0;
        for (float value : magnitudeHistory) {
            sum += value;
        }
        return sum / SMOOTHING_WINDOW;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}

/**
 * Alternative Algorithm: Zero-Crossing với Low-Pass Filter
 */
class ZeroCrossingStepCounter implements SensorEventListener {

    private static final String TAG = "ZeroCrossCounter";
    private static final float ALPHA = 0.8f; // Low-pass filter constant
    private static final float STEP_THRESHOLD = 0.5f;
    private static final long STEP_DELAY_NS = 300000000L; // 300ms

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private AccelerometerStepCounter.StepListener stepListener;

    private float lastZ = 0;
    private float filteredZ = 0;
    private long lastStepTime = 0;

    public ZeroCrossingStepCounter(Context context, AccelerometerStepCounter.StepListener listener) {
        this.stepListener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    public void start() {
        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Low-pass filter trên trục Z (vertical)
            float z = event.values[2];
            filteredZ = ALPHA * filteredZ + (1 - ALPHA) * z;

            // Phát hiện zero-crossing
            if (lastZ < 0 && filteredZ > STEP_THRESHOLD) {
                long currentTime = System.nanoTime();

                if (currentTime - lastStepTime > STEP_DELAY_NS) {
                    lastStepTime = currentTime;

                    if (stepListener != null) {
                        stepListener.onStep(1);
                    }

                    Log.d(TAG, "Step detected via zero-crossing");
                }
            }

            lastZ = filteredZ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}