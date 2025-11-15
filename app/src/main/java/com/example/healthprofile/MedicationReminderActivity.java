package com.example.healthprofile;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.MedicationReminderAdapter;
import com.example.healthprofile.model.MedicationReminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicationReminderActivity extends AppCompatActivity {

    private RecyclerView rvReminders;
    private FloatingActionButton fabAdd;
    private ImageButton btnBack;
    private LinearLayout emptyState;

    private SQLiteDatabase db;
    private String userEmail;
    private MedicationReminderAdapter adapter;
    private List<MedicationReminder> reminderList;

    // Launcher để request permission notification
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cần cấp quyền để nhận thông báo nhắc nhở", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_reminder);

        // Kiểm tra và request notification permission (Android 13+)
        checkNotificationPermission();
        checkAlarmPermission();

        // Lấy email
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        // Mở database
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
        createTableIfNotExists();

        initViews();
        loadReminders();

        // Đặt lại tất cả các alarm cho user hiện tại
        MedicationAlarmScheduler.scheduleAllReminders(this, userEmail);


    }
    private void checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Cần cấp quyền")
                        .setMessage("Ứng dụng cần quyền đặt báo thức chính xác để nhắc nhở đúng giờ")
                        .setPositiveButton("Cài đặt", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        }
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS medication_reminders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "medication_name TEXT NOT NULL, " +
                "dosage TEXT, " +
                "frequency TEXT NOT NULL, " +
                "time1 TEXT, " +
                "time2 TEXT, " +
                "time3 TEXT, " +
                "start_date TEXT, " +
                "end_date TEXT, " +
                "notes TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "created_at INTEGER, " +
                "updated_at INTEGER)";
        db.execSQL(sql);
    }

    private void initViews() {
        rvReminders = findViewById(R.id.rv_medication_reminders);
        fabAdd = findViewById(R.id.fab_add_reminder);
        btnBack = findViewById(R.id.btn_back_medication);
        emptyState = findViewById(R.id.empty_state_medication);

        btnBack.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showAddReminderDialog());

        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        reminderList = new ArrayList<>();
        adapter = new MedicationReminderAdapter(this, reminderList, new MedicationReminderAdapter.OnReminderActionListener() {
            @Override
            public void onEdit(MedicationReminder reminder) {
                showEditReminderDialog(reminder);
            }

            @Override
            public void onDelete(MedicationReminder reminder) {
                deleteReminder(reminder);
            }

            @Override
            public void onToggleActive(MedicationReminder reminder) {
                toggleReminderStatus(reminder);
            }
        });
        rvReminders.setAdapter(adapter);
        Button btnDebugAlarm = new Button(this);
        btnDebugAlarm.setText("Test Alarm (1 phút)");
        btnDebugAlarm.setOnClickListener(v -> {
            if (!reminderList.isEmpty()) {
                MedicationReminder reminder = reminderList.get(0);

                // Lấy thời gian hiện tại + 1 phút
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 1);
                String testTime = String.format("%02d:%02d",
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE));

                // Cập nhật time1 tạm thời
                String oldTime = reminder.getTime1();
                reminder.setTime1(testTime);

                // Đặt alarm
                MedicationAlarmScheduler.cancelReminder(this, reminder.getId());

                // Tạo alarm với thời gian test
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, MedicationNotificationReceiver.class);
                intent.putExtra("reminder_id", reminder.getId());
                intent.putExtra("medication_name", reminder.getMedicationName());
                intent.putExtra("time_slot", testTime);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        reminder.getId() * 10 + 1,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            cal.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Toast.makeText(this, "Alarm test sẽ reo lúc " + testTime, Toast.LENGTH_LONG).show();

                // Khôi phục time cũ
                reminder.setTime1(oldTime);
            }
        });
    }

    private void loadReminders() {
        reminderList.clear();

        String sql = "SELECT * FROM medication_reminders " +
                "WHERE user_email = '" + escapeString(userEmail) + "' " +
                "ORDER BY is_active DESC, created_at DESC";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                MedicationReminder reminder = new MedicationReminder();
                reminder.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                reminder.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
                reminder.setMedicationName(cursor.getString(cursor.getColumnIndexOrThrow("medication_name")));
                reminder.setDosage(cursor.getString(cursor.getColumnIndexOrThrow("dosage")));
                reminder.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow("frequency")));
                reminder.setTime1(cursor.getString(cursor.getColumnIndexOrThrow("time1")));
                reminder.setTime2(cursor.getString(cursor.getColumnIndexOrThrow("time2")));
                reminder.setTime3(cursor.getString(cursor.getColumnIndexOrThrow("time3")));
                reminder.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
                reminder.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
                reminder.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                reminder.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);
                reminder.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
                reminder.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
                reminderList.add(reminder);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (reminderList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvReminders.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvReminders.setVisibility(View.VISIBLE);
        }
    }

    private void showAddReminderDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medication_reminder, null);

        TextInputEditText etMedicationName = dialogView.findViewById(R.id.et_medication_name);
        TextInputEditText etDosage = dialogView.findViewById(R.id.et_dosage);
        Spinner spinnerFrequency = dialogView.findViewById(R.id.spinner_frequency);
        TextInputEditText etTime1 = dialogView.findViewById(R.id.et_time1);
        TextInputEditText etTime2 = dialogView.findViewById(R.id.et_time2);
        TextInputEditText etTime3 = dialogView.findViewById(R.id.et_time3);
        TextInputEditText etNotes = dialogView.findViewById(R.id.et_reminder_notes);

        // Setup frequency spinner
        String[] frequencies = {"Mỗi ngày", "2 lần/ngày", "3 lần/ngày", "Hàng tuần"};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencies);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(freqAdapter);

        // Time pickers
        setupTimePicker(etTime1);
        setupTimePicker(etTime2);
        setupTimePicker(etTime3);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm nhắc nhở uống thuốc")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String name = etMedicationName.getText().toString().trim();
                String dosage = etDosage.getText().toString().trim();
                String time1 = etTime1.getText().toString().trim();
                String notes = etNotes.getText().toString().trim();

                if (name.isEmpty()) {
                    etMedicationName.setError("Vui lòng nhập tên thuốc");
                    return;
                }

                if (time1.isEmpty()) {
                    etTime1.setError("Vui lòng chọn ít nhất 1 thời gian");
                    return;
                }

                MedicationReminder reminder = new MedicationReminder();
                reminder.setUserEmail(userEmail);
                reminder.setMedicationName(name);
                reminder.setDosage(dosage);
                reminder.setFrequency(getFrequencyCode(spinnerFrequency.getSelectedItemPosition()));
                reminder.setTime1(time1);
                reminder.setTime2(etTime2.getText().toString().trim());
                reminder.setTime3(etTime3.getText().toString().trim());
                reminder.setNotes(notes);

                int newId = saveReminder(reminder);
                if (newId > 0) {
                    // Đặt alarm cho reminder mới
                    MedicationAlarmScheduler.scheduleReminder(this, newId);

                    Toast.makeText(this, "Đã thêm nhắc nhở và đặt thông báo", Toast.LENGTH_SHORT).show();
                    loadReminders();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showEditReminderDialog(MedicationReminder reminder) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medication_reminder, null);

        TextInputEditText etMedicationName = dialogView.findViewById(R.id.et_medication_name);
        TextInputEditText etDosage = dialogView.findViewById(R.id.et_dosage);
        Spinner spinnerFrequency = dialogView.findViewById(R.id.spinner_frequency);
        TextInputEditText etTime1 = dialogView.findViewById(R.id.et_time1);
        TextInputEditText etTime2 = dialogView.findViewById(R.id.et_time2);
        TextInputEditText etTime3 = dialogView.findViewById(R.id.et_time3);
        TextInputEditText etNotes = dialogView.findViewById(R.id.et_reminder_notes);

        // Fill existing data
        etMedicationName.setText(reminder.getMedicationName());
        etDosage.setText(reminder.getDosage());
        etTime1.setText(reminder.getTime1());
        etTime2.setText(reminder.getTime2());
        etTime3.setText(reminder.getTime3());
        etNotes.setText(reminder.getNotes());

        // Setup frequency spinner
        String[] frequencies = {"Mỗi ngày", "2 lần/ngày", "3 lần/ngày", "Hàng tuần"};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencies);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(freqAdapter);
        spinnerFrequency.setSelection(getFrequencyPosition(reminder.getFrequency()));

        // Time pickers
        setupTimePicker(etTime1);
        setupTimePicker(etTime2);
        setupTimePicker(etTime3);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa nhắc nhở")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String name = etMedicationName.getText().toString().trim();
                String time1 = etTime1.getText().toString().trim();

                if (name.isEmpty() || time1.isEmpty()) {
                    Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                reminder.setMedicationName(name);
                reminder.setDosage(etDosage.getText().toString().trim());
                reminder.setFrequency(getFrequencyCode(spinnerFrequency.getSelectedItemPosition()));
                reminder.setTime1(time1);
                reminder.setTime2(etTime2.getText().toString().trim());
                reminder.setTime3(etTime3.getText().toString().trim());
                reminder.setNotes(etNotes.getText().toString().trim());
                reminder.setUpdatedAt(System.currentTimeMillis());

                if (updateReminder(reminder)) {
                    // Hủy alarm cũ và đặt lại alarm mới
                    MedicationAlarmScheduler.cancelReminder(this, reminder.getId());
                    if (reminder.isActive()) {
                        MedicationAlarmScheduler.scheduleReminder(this, reminder.getId());
                    }

                    Toast.makeText(this, "Đã cập nhật và đặt lại thông báo", Toast.LENGTH_SHORT).show();
                    loadReminders();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void setupTimePicker(TextInputEditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, min) -> {
                        String time = String.format("%02d:%02d", hourOfDay, min);
                        editText.setText(time);
                    }, hour, minute, true);
            timePickerDialog.show();
        });
    }

    private String getFrequencyCode(int position) {
        switch (position) {
            case 0: return "daily";
            case 1: return "twice_daily";
            case 2: return "three_times_daily";
            case 3: return "weekly";
            default: return "daily";
        }
    }

    private int getFrequencyPosition(String code) {
        switch (code) {
            case "daily": return 0;
            case "twice_daily": return 1;
            case "three_times_daily": return 2;
            case "weekly": return 3;
            default: return 0;
        }
    }

    private int saveReminder(MedicationReminder reminder) {
        try {
            String sql = "INSERT INTO medication_reminders " +
                    "(user_email, medication_name, dosage, frequency, time1, time2, time3, notes, is_active, created_at, updated_at) " +
                    "VALUES ('" + escapeString(reminder.getUserEmail()) + "', " +
                    "'" + escapeString(reminder.getMedicationName()) + "', " +
                    "'" + escapeString(reminder.getDosage()) + "', " +
                    "'" + escapeString(reminder.getFrequency()) + "', " +
                    "'" + escapeString(reminder.getTime1()) + "', " +
                    "'" + escapeString(reminder.getTime2()) + "', " +
                    "'" + escapeString(reminder.getTime3()) + "', " +
                    "'" + escapeString(reminder.getNotes()) + "', " +
                    "1, " + System.currentTimeMillis() + ", " + System.currentTimeMillis() + ")";
            db.execSQL(sql);

            // Lấy ID của reminder vừa thêm
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            int newId = -1;
            if (cursor.moveToFirst()) {
                newId = cursor.getInt(0);
            }
            cursor.close();
            return newId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean updateReminder(MedicationReminder reminder) {
        try {
            String sql = "UPDATE medication_reminders SET " +
                    "medication_name = '" + escapeString(reminder.getMedicationName()) + "', " +
                    "dosage = '" + escapeString(reminder.getDosage()) + "', " +
                    "frequency = '" + escapeString(reminder.getFrequency()) + "', " +
                    "time1 = '" + escapeString(reminder.getTime1()) + "', " +
                    "time2 = '" + escapeString(reminder.getTime2()) + "', " +
                    "time3 = '" + escapeString(reminder.getTime3()) + "', " +
                    "notes = '" + escapeString(reminder.getNotes()) + "', " +
                    "updated_at = " + System.currentTimeMillis() + " " +
                    "WHERE id = " + reminder.getId();
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteReminder(MedicationReminder reminder) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhắc nhở")
                .setMessage("Bạn có chắc muốn xóa nhắc nhở này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try {
                        // Hủy alarm trước khi xóa
                        MedicationAlarmScheduler.cancelReminder(this, reminder.getId());

                        db.execSQL("DELETE FROM medication_reminders WHERE id = " + reminder.getId());
                        Toast.makeText(this, "Đã xóa nhắc nhở và thông báo", Toast.LENGTH_SHORT).show();
                        loadReminders();
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void toggleReminderStatus(MedicationReminder reminder) {
        try {
            int newStatus = reminder.isActive() ? 0 : 1;
            String sql = "UPDATE medication_reminders SET is_active = " + newStatus +
                    ", updated_at = " + System.currentTimeMillis() +
                    " WHERE id = " + reminder.getId();
            db.execSQL(sql);

            reminder.setActive(!reminder.isActive());
            adapter.notifyDataSetChanged();

            // Bật/tắt alarm tương ứng
            if (reminder.isActive()) {
                MedicationAlarmScheduler.scheduleReminder(this, reminder.getId());
                Toast.makeText(this, "Đã bật nhắc nhở và thông báo", Toast.LENGTH_SHORT).show();
            } else {
                MedicationAlarmScheduler.cancelReminder(this, reminder.getId());
                Toast.makeText(this, "Đã tắt nhắc nhở và thông báo", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
        }
    }

    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}