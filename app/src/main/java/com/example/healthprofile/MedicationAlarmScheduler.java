package com.example.healthprofile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class MedicationAlarmScheduler {

    private static final String TAG = "MedicationAlarm";

    public static void scheduleAllReminders(Context context, String userEmail) {
        SQLiteDatabase db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

        String sql = "SELECT * FROM medication_reminders " +
                "WHERE user_email = '" + escapeString(userEmail) + "' " +
                "AND is_active = 1";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String medicationName = cursor.getString(cursor.getColumnIndexOrThrow("medication_name"));
                String time1 = cursor.getString(cursor.getColumnIndexOrThrow("time1"));
                String time2 = cursor.getString(cursor.getColumnIndexOrThrow("time2"));
                String time3 = cursor.getString(cursor.getColumnIndexOrThrow("time3"));

                // Đặt alarm cho từng thời gian
                if (time1 != null && !time1.isEmpty()) {
                    scheduleAlarm(context, id, medicationName, time1, 1);
                }
                if (time2 != null && !time2.isEmpty()) {
                    scheduleAlarm(context, id, medicationName, time2, 2);
                }
                if (time3 != null && !time3.isEmpty()) {
                    scheduleAlarm(context, id, medicationName, time3, 3);
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.d(TAG, "All reminders scheduled successfully");
    }

    public static void scheduleReminder(Context context, int reminderId) {
        SQLiteDatabase db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

        String sql = "SELECT * FROM medication_reminders WHERE id = " + reminderId + " AND is_active = 1";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            String medicationName = cursor.getString(cursor.getColumnIndexOrThrow("medication_name"));
            String time1 = cursor.getString(cursor.getColumnIndexOrThrow("time1"));
            String time2 = cursor.getString(cursor.getColumnIndexOrThrow("time2"));
            String time3 = cursor.getString(cursor.getColumnIndexOrThrow("time3"));

            if (time1 != null && !time1.isEmpty()) {
                scheduleAlarm(context, reminderId, medicationName, time1, 1);
            }
            if (time2 != null && !time2.isEmpty()) {
                scheduleAlarm(context, reminderId, medicationName, time2, 2);
            }
            if (time3 != null && !time3.isEmpty()) {
                scheduleAlarm(context, reminderId, medicationName, time3, 3);
            }
        }

        cursor.close();
        db.close();
    }

    private static void scheduleAlarm(Context context, int reminderId, String medicationName, String time, int timeSlot) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, MedicationNotificationReceiver.class);
        intent.putExtra("reminder_id", reminderId);
        intent.putExtra("medication_name", medicationName);
        intent.putExtra("time_slot", time);

        // Request code duy nhất cho mỗi alarm
        int requestCode = reminderId * 10 + timeSlot;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Parse thời gian (HH:mm)
        try {
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Tạo calendar cho thời gian nhắc nhở
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Log thời gian hiện tại
            Calendar now = Calendar.getInstance();
            Log.d(TAG, "Current time: " + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE));
            Log.d(TAG, "Target time: " + hour + ":" + minute);

            // Nếu thời gian đã qua trong ngày hôm nay, đặt cho ngày mai
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Log.d(TAG, "Time passed today, scheduling for tomorrow");
            }

            // Đặt alarm chính xác
            if (alarmManager != null) {
                // Kiểm tra quyền trên Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Log.e(TAG, "❌ Cannot schedule exact alarms - permission not granted!");
                        return;
                    }
                }

                // HỦY ALARM CŨ TRƯỚC (nếu có)
                alarmManager.cancel(pendingIntent);

                // SỬ DỤNG setExactAndAllowWhileIdle thay vì setRepeating
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Log.d(TAG, "✅ Alarm scheduled successfully:");
                Log.d(TAG, "  - Medication: " + medicationName);
                Log.d(TAG, "  - Time: " + time);
                Log.d(TAG, "  - Request Code: " + requestCode);
                Log.d(TAG, "  - Scheduled for: " + calendar.getTime());
                Log.d(TAG, "  - Timestamp: " + calendar.getTimeInMillis());

            } else {
                Log.e(TAG, "❌ AlarmManager is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error scheduling alarm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void scheduleAlarmForNextDay(Context context, int reminderId, String timeSlot) {
        // Đặt lại alarm cho ngày hôm sau
        SQLiteDatabase db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

        String sql = "SELECT * FROM medication_reminders WHERE id = " + reminderId;
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            String medicationName = cursor.getString(cursor.getColumnIndexOrThrow("medication_name"));

            // Tìm time slot tương ứng
            int slot = 0;
            String time1 = cursor.getString(cursor.getColumnIndexOrThrow("time1"));
            String time2 = cursor.getString(cursor.getColumnIndexOrThrow("time2"));
            String time3 = cursor.getString(cursor.getColumnIndexOrThrow("time3"));

            if (timeSlot.equals(time1)) {
                slot = 1;
                scheduleAlarm(context, reminderId, medicationName, time1, 1);
            } else if (timeSlot.equals(time2)) {
                slot = 2;
                scheduleAlarm(context, reminderId, medicationName, time2, 2);
            } else if (timeSlot.equals(time3)) {
                slot = 3;
                scheduleAlarm(context, reminderId, medicationName, time3, 3);
            }

            Log.d(TAG, "Rescheduled alarm for next day - Reminder: " + reminderId + ", Slot: " + slot);
        }

        cursor.close();
        db.close();
    }

    public static void cancelReminder(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Hủy tất cả các alarm của reminder này (time1, time2, time3)
        for (int timeSlot = 1; timeSlot <= 3; timeSlot++) {
            Intent intent = new Intent(context, MedicationNotificationReceiver.class);
            int requestCode = reminderId * 10 + timeSlot;

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d(TAG, "Cancelled alarm with request code: " + requestCode);
            }
        }
    }

    public static void cancelAllReminders(Context context, String userEmail) {
        SQLiteDatabase db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

        String sql = "SELECT id FROM medication_reminders WHERE user_email = '" + escapeString(userEmail) + "'";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                cancelReminder(context, id);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    private static String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }
}