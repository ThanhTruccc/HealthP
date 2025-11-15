package com.example.healthprofile;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import java.util.Calendar;

public class MedicationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int reminderId = intent.getIntExtra("reminder_id", -1);
        int notificationId = intent.getIntExtra("notification_id", -1);

        if (action == null || reminderId == -1) return;

        // Đóng notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);

        if (action.equals("ACTION_TAKEN")) {
            // Lưu lịch sử đã uống thuốc vào database
            logMedicationTaken(context, reminderId);
            Toast.makeText(context, "Đã ghi nhận uống thuốc ✓", Toast.LENGTH_SHORT).show();

        } else if (action.equals("ACTION_SNOOZE")) {
            // Hoãn lại 10 phút
            String timeSlot = intent.getStringExtra("time_slot");
            scheduleSnoozeNotification(context, reminderId, timeSlot);
            Toast.makeText(context, "Đã hoãn 10 phút", Toast.LENGTH_SHORT).show();
        }
    }

    private void logMedicationTaken(Context context, int reminderId) {
        try {
            SQLiteDatabase db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

            // Tạo bảng lịch sử nếu chưa có
            String createTableSql = "CREATE TABLE IF NOT EXISTS medication_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "reminder_id INTEGER NOT NULL, " +
                    "taken_at INTEGER NOT NULL, " +
                    "notes TEXT)";
            db.execSQL(createTableSql);

            // Thêm lịch sử
            String insertSql = "INSERT INTO medication_history (reminder_id, taken_at) " +
                    "VALUES (" + reminderId + ", " + System.currentTimeMillis() + ")";
            db.execSQL(insertSql);

            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleSnoozeNotification(Context context, int reminderId, String timeSlot) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, MedicationNotificationReceiver.class);
        intent.putExtra("reminder_id", reminderId);
        intent.putExtra("time_slot", timeSlot);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId * 100, // ID khác để không trùng với alarm chính
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Đặt alarm sau 10 phút
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}