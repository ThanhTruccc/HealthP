package com.example.healthprofile;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import java.util.List;

public class SensorChecker {

    /**
     * Kiểm tra thiết bị có hỗ trợ cảm biến bước chân không
     */
    public static boolean hasStepSensor(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager == null) {
            return false;
        }

        // Kiểm tra TYPE_STEP_COUNTER (chính xác hơn)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepCounter != null) {
                return true;
            }
        }

        // Kiểm tra TYPE_STEP_DETECTOR (backup)
        Sensor stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        return stepDetector != null;
    }

    /**
     * Lấy thông tin chi tiết về cảm biến
     */
    public static String getSensorInfo(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager == null) {
            return "Không thể truy cập SensorManager";
        }

        StringBuilder info = new StringBuilder();

        // Kiểm tra Step Counter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepCounter != null) {
                info.append("✅ Step Counter\n");
                info.append("   Tên: ").append(stepCounter.getName()).append("\n");
                info.append("   Vendor: ").append(stepCounter.getVendor()).append("\n");
                info.append("   Version: ").append(stepCounter.getVersion()).append("\n");
                info.append("   Power: ").append(stepCounter.getPower()).append(" mA\n");
                info.append("   Max Range: ").append((int)stepCounter.getMaximumRange()).append(" steps\n\n");
            }
        }

        // Kiểm tra Step Detector
        Sensor stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepDetector != null) {
            info.append("✅ Step Detector\n");
            info.append("   Tên: ").append(stepDetector.getName()).append("\n");
            info.append("   Vendor: ").append(stepDetector.getVendor()).append("\n\n");
        }

        if (info.length() == 0) {
            info.append("❌ Không có cảm biến bước chân\n\n");
            info.append("Thiết bị này không hỗ trợ đếm bước chân.\n");
            info.append("Các thiết bị thường không có:\n");
            info.append("• Máy tính bảng\n");
            info.append("• Điện thoại cũ (< 2014)\n");
            info.append("• Máy ảo/Emulator");
        }

        return info.toString();
    }

    /**
     * Lấy loại cảm biến (để hiển thị cho user)
     */
    public static String getSensorType(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager == null) {
            return "Unknown";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepCounter != null) {
                return "Step Counter (Chính xác)";
            }
        }

        Sensor stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepDetector != null) {
            return "Step Detector (Ước tính)";
        }

        return "Không hỗ trợ";
    }

    /**
     * Liệt kê TẤT CẢ các sensor trên thiết bị (debug purpose)
     */
    public static String getAllSensors(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager == null) {
            return "Không thể truy cập SensorManager";
        }

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        StringBuilder info = new StringBuilder();
        info.append("📱 Tổng số sensor: ").append(sensors.size()).append("\n\n");

        for (Sensor sensor : sensors) {
            info.append("• ").append(sensor.getName()).append("\n");
            info.append("  Type: ").append(getSensorTypeName(sensor.getType())).append("\n");
            info.append("  Vendor: ").append(sensor.getVendor()).append("\n\n");
        }

        return info.toString();
    }

    private static String getSensorTypeName(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: return "Accelerometer";
            case Sensor.TYPE_GYROSCOPE: return "Gyroscope";
            case Sensor.TYPE_MAGNETIC_FIELD: return "Magnetic Field";
            case Sensor.TYPE_LIGHT: return "Light";
            case Sensor.TYPE_PROXIMITY: return "Proximity";
            case Sensor.TYPE_STEP_COUNTER: return "Step Counter ⭐";
            case Sensor.TYPE_STEP_DETECTOR: return "Step Detector ⭐";
            case Sensor.TYPE_HEART_RATE: return "Heart Rate";
            default: return "Unknown (" + type + ")";
        }
    }

    /**
     * Kiểm tra phiên bản Android có hỗ trợ step sensor không
     */
    public static boolean isAndroidVersionSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT; // Android 4.4+
    }

    /**
     * Lấy message thông báo cho user
     */
    public static String getUserMessage(Context context) {
        if (!isAndroidVersionSupported()) {
            return "❌ Phiên bản Android quá cũ\n\nCần Android 4.4 (KitKat) trở lên để sử dụng tính năng đếm bước chân.";
        }

        if (!hasStepSensor(context)) {
            // Kiểm tra xem có accelerometer không
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (accelerometer != null) {
                return "⚠️ Không có Step Counter\n\n" +
                        "Thiết bị của bạn KHÔNG có cảm biến bước chân chuyên dụng.\n\n" +
                        "✅ Giải pháp: Sử dụng Accelerometer\n" +
                        "App sẽ dùng cảm biến gia tốc để ước tính số bước.\n\n" +
                        "⚠️ Lưu ý:\n" +
                        "• Độ chính xác: ~85-90%\n" +
                        "• Tốn pin hơn Step Counter\n" +
                        "• Giữ điện thoại trong túi/đeo người\n\n" +
                        "📱 Điện thoại thường không có:\n" +
                        "• Samsung A series (A10, A20, A30...)\n" +
                        "• Xiaomi Redmi series giá rẻ\n" +
                        "• Oppo/Vivo series giá rẻ";
            }

            return "❌ Không hỗ trợ cảm biến\n\nThiết bị của bạn không có cảm biến bước chân VÀ không có accelerometer.\n\n" +
                    "Các thiết bị thường không có:\n" +
                    "• Máy tính bảng\n" +
                    "• Điện thoại cũ (trước 2014)\n" +
                    "• Máy ảo/Emulator\n\n" +
                    "Vui lòng sử dụng thiết bị khác.";
        }

        return "✅ Thiết bị được hỗ trợ\n\n" +
                "Loại cảm biến: " + getSensorType(context) + "\n\n" +
                "Ứng dụng sẽ đếm bước ngay cả khi đóng app.";
    }
}