package com.example.healthprofile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MedicationNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "MedicationNotif";
    private static final String CHANNEL_ID = "medication_reminder_channel";
    private static final String CHANNEL_NAME = "Nhắc nhở uống thuốc";

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra("reminder_id", -1);
        String timeSlot = intent.getStringExtra("time_slot");
        String medicationName = intent.getStringExtra("medication_name");

        Log.d(TAG, "========================================");
        Log.d(TAG, "🔔 Alarm triggered!");
        Log.d(TAG, "Reminder ID: " + reminderId);
        Log.d(TAG, "Time slot: " + timeSlot);
        Log.d(TAG, "Medication: " + medicationName);
        Log.d(TAG, "========================================");

        if (reminderId != -1) {
            // Hiển thị notification
            showNotification(context, reminderId, timeSlot, medicationName);

            // ĐẶT LẠI ALARM CHO NGÀY MAI (quan trọng!)
            MedicationAlarmScheduler.scheduleAlarmForNextDay(context, reminderId, timeSlot);

        } else {
            Log.e(TAG, "Invalid reminder ID!");
        }
    }

    private void showNotification(Context context, int reminderId, String timeSlot, String medicationName) {
        // Mở database để lấy thông tin thuốc
        SQLiteDatabase db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

        String sql = "SELECT * FROM medication_reminders WHERE id = " + reminderId;
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            medicationName = cursor.getString(cursor.getColumnIndexOrThrow("medication_name"));
            String dosage = cursor.getString(cursor.getColumnIndexOrThrow("dosage"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));

            // Tạo notification channel (bắt buộc cho Android 8.0+)
            createNotificationChannel(context);

            // Tạo intent để mở app khi nhấn vào notification
            Intent openIntent = new Intent(context, MedicationReminderActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    reminderId,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Tạo intent cho nút "Đã uống"
            Intent takenIntent = new Intent(context, MedicationActionReceiver.class);
            takenIntent.setAction("ACTION_TAKEN");
            takenIntent.putExtra("reminder_id", reminderId);
            takenIntent.putExtra("notification_id", reminderId);
            PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderId * 100 + 1,
                    takenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Tạo intent cho nút "Hoãn lại"
            Intent snoozeIntent = new Intent(context, MedicationActionReceiver.class);
            snoozeIntent.setAction("ACTION_SNOOZE");
            snoozeIntent.putExtra("reminder_id", reminderId);
            snoozeIntent.putExtra("notification_id", reminderId);
            snoozeIntent.putExtra("time_slot", timeSlot);
            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderId * 100 + 2,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Tạo nội dung notification
            String title = "⏰ Đến giờ uống thuốc!";
            String content = medicationName;
            if (dosage != null && !dosage.isEmpty()) {
                content += " - " + dosage;
            }

            String bigText = "💊 Thuốc: " + medicationName;
            if (dosage != null && !dosage.isEmpty()) {
                bigText += "\n📋 Liều lượng: " + dosage;
            }
            if (notes != null && !notes.isEmpty()) {
                bigText += "\n📝 Ghi chú: " + notes;
            }
            bigText += "\n\n⏰ Thời gian: " + timeSlot;

            // Get default notification sound
            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Build notification với icon mặc định (tránh lỗi icon không tồn tại)
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .addAction(android.R.drawable.ic_menu_save, "Đã uống", takenPendingIntent)
                    .addAction(android.R.drawable.ic_menu_recent_history, "Hoãn 10 phút", snoozePendingIntent)
                    .setSound(defaultSound)
                    .setVibrate(new long[]{0, 500, 200, 500})
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            // Hiển thị notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            try {
                notificationManager.notify(reminderId, builder.build());
                Log.d(TAG, "✅ Notification shown successfully for " + medicationName);
            } catch (SecurityException e) {
                Log.e(TAG, "❌ SecurityException: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "❌ Reminder not found in database: " + reminderId);
        }

        cursor.close();
        db.close();
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo nhắc nhở uống thuốc đúng giờ");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.enableLights(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }
}