package com.example.healthprofile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Lấy email người dùng đã đăng nhập
            SharedPreferences prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("email", "");

            if (!userEmail.isEmpty()) {
                // Đặt lại tất cả các alarm sau khi khởi động lại thiết bị
                MedicationAlarmScheduler.scheduleAllReminders(context, userEmail);
            }
        }
    }
}