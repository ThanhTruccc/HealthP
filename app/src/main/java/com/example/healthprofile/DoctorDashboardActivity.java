package com.example.healthprofile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.DoctorAppointmentAdapter;
import com.example.healthprofile.model.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DoctorDashboardActivity extends AppCompatActivity {

    private TextView tvDoctorName, tvSpecialization;
    private TextView tvTodayCount, tvPendingCount, tvCompletedCount;
    private RecyclerView rvAppointments;
    private LinearLayout emptyState;
    private Button btnLogout, btnRecords;
    private SQLiteDatabase db;
    private SharedPreferences prefs;
    private int doctorId;
    private String doctorName;
    private List<Appointment> appointmentList;
    private DoctorAppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Kiểm tra session
        prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = prefs.getString("role", "");

        if (!"doctor".equals(role)) {
            finish();
            return;
        }

        int userId = prefs.getInt("user_id", 0);
        doctorName = prefs.getString("fullName", "");

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        // DEBUG: Log để kiểm tra
        android.util.Log.d("DoctorDashboard", "User ID: " + userId);
        android.util.Log.d("DoctorDashboard", "Doctor Name: " + doctorName);
        // Lấy doctor_id từ bảng liên kết
        Cursor cursor = db.rawQuery("SELECT doctor_id FROM user_doctors WHERE user_id = " + userId, null);
        if (cursor.moveToFirst()) {
            doctorId = cursor.getInt(0);
            android.util.Log.d("DoctorDashboard", "Found doctor_id from user_doctors: " + doctorId);
        } else {
            // Nếu không tìm thấy trong user_doctors, thử tìm trong bảng doctors theo tên
            android.util.Log.d("DoctorDashboard", "Not found in user_doctors, trying doctors table");
            cursor.close();

            cursor = db.rawQuery("SELECT id FROM doctors WHERE name LIKE ?",
                    new String[]{"%" + doctorName.replace("BS. ", "") + "%"});

            if (cursor.moveToFirst()) {
                doctorId = cursor.getInt(0);
                android.util.Log.d("DoctorDashboard", "Found doctor_id from doctors table: " + doctorId);
            } else {
                doctorId = userId; // Fallback
                android.util.Log.d("DoctorDashboard", "Using userId as fallback: " + doctorId);
            }
        }
        cursor.close();

        // DEBUG: Kiểm tra appointments trong database
        Cursor allAppts = db.rawQuery("SELECT id, doctor_id, doctor_name, patient_name, status FROM appointments", null);
        android.util.Log.d("DoctorDashboard", "Total appointments in DB: " + allAppts.getCount());
        if (allAppts.moveToFirst()) {
            do {
                android.util.Log.d("DoctorDashboard", "Appointment - ID: " + allAppts.getInt(0)
                        + ", doctor_id: " + allAppts.getInt(1)
                        + ", doctor_name: " + allAppts.getString(2)
                        + ", patient: " + allAppts.getString(3)
                        + ", status: " + allAppts.getString(4));
            } while (allAppts.moveToNext());
        }
        allAppts.close();

        android.util.Log.d("DoctorDashboard", "Final doctorId used for query: " + doctorId);

        initViews();
        loadStatistics();
        loadAppointments();
    }

    private void initViews() {
        tvDoctorName = findViewById(R.id.tv_doctor_name);
        tvSpecialization = findViewById(R.id.tv_doctor_specialization);
        tvTodayCount = findViewById(R.id.tv_today_count);
        tvPendingCount = findViewById(R.id.tv_pending_count);
        tvCompletedCount = findViewById(R.id.tv_completed_count);
        rvAppointments = findViewById(R.id.rv_appointments);
        emptyState = findViewById(R.id.empty_state_appointments);
        btnRecords = findViewById(R.id.btn_view_medical_records);
        btnLogout = findViewById(R.id.btn_logout);

        tvDoctorName.setText(prefs.getString("fullName", ""));

        // Lấy specialization từ user_doctors
        Cursor cursor = db.rawQuery("SELECT specialization FROM user_doctors WHERE user_id = " +
                prefs.getInt("user_id", 0), null);
        if (cursor.moveToFirst()) {
            tvSpecialization.setText(cursor.getString(0));
        }
        cursor.close();

        btnRecords.setOnClickListener(v -> {
            Intent intent = new Intent(this, MedicalRecordsActivity.class);
            startActivity(intent);
        });

        // Xử lý nút đăng xuất - hiển thị dialog xác nhận
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();

        adapter = new DoctorAppointmentAdapter(this, appointmentList, new DoctorAppointmentAdapter.OnAppointmentClickListener() {
            @Override
            public void onViewClick(Appointment appointment) {
                viewAppointmentDetails(appointment);
            }

            @Override
            public void onStartExamClick(Appointment appointment) {
                startExamination(appointment);
            }
        });
        rvAppointments.setAdapter(adapter);
    }

    private void loadStatistics() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(Calendar.getInstance().getTime());

        // Đếm lịch hôm nay
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE doctor_id = " + doctorId +
                " AND appointment_date = '" + today + "'", null);
        cursor.moveToFirst();
        int todayCount = cursor.getInt(0);
        cursor.close();

        // Đếm lịch pending
        cursor = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE doctor_id = " + doctorId +
                " AND status = 'pending'", null);
        cursor.moveToFirst();
        int pendingCount = cursor.getInt(0);
        cursor.close();

        // Đếm lịch completed
        cursor = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE doctor_id = " + doctorId +
                " AND status = 'completed'", null);
        cursor.moveToFirst();
        int completedCount = cursor.getInt(0);
        cursor.close();

        tvTodayCount.setText(String.valueOf(todayCount));
        tvPendingCount.setText(String.valueOf(pendingCount));
        tvCompletedCount.setText(String.valueOf(completedCount));
    }

    private void loadAppointments() {
        appointmentList.clear();

        String query = "SELECT * FROM appointments WHERE doctor_id = ? ORDER BY appointment_date DESC, appointment_time DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(doctorId)});

        android.util.Log.d("DoctorDashboard", "Query result count: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                Appointment appointment = new Appointment();
                int idIndex = cursor.getColumnIndex("id");
                if (idIndex != -1) appointment.setId(cursor.getInt(idIndex));

                int emailIndex = cursor.getColumnIndex("user_email");
                if (emailIndex != -1) appointment.setPatientEmail(cursor.getString(emailIndex));

                int nameIndex = cursor.getColumnIndex("patient_name");
                if (nameIndex != -1) appointment.setPatientName(cursor.getString(nameIndex));

                int doctorIdIndex = cursor.getColumnIndex("doctor_id");
                if (doctorIdIndex != -1) appointment.setDoctorId(cursor.getInt(doctorIdIndex));

                int doctorNameIndex = cursor.getColumnIndex("doctor_name");
                if (doctorNameIndex != -1) appointment.setDoctorName(cursor.getString(doctorNameIndex));

                int dateIndex = cursor.getColumnIndex("appointment_date");
                if (dateIndex != -1) appointment.setAppointmentDate(cursor.getString(dateIndex));

                int timeIndex = cursor.getColumnIndex("appointment_time");
                if (timeIndex != -1) appointment.setAppointmentTime(cursor.getString(timeIndex));

                int reasonIndex = cursor.getColumnIndex("reason");
                if (reasonIndex != -1) appointment.setReason(cursor.getString(reasonIndex));

                int statusIndex = cursor.getColumnIndex("status");
                if (statusIndex != -1) appointment.setStatus(cursor.getString(statusIndex));

                int notesIndex = cursor.getColumnIndex("notes");
                if (notesIndex != -1 && !cursor.isNull(notesIndex)) {
                    appointment.setNotes(cursor.getString(notesIndex));
                }

                int phoneIndex = cursor.getColumnIndex("phone");
                if (phoneIndex != -1 && !cursor.isNull(phoneIndex)) {
                    appointment.setPhone(cursor.getString(phoneIndex));
                }

                int feeIndex = cursor.getColumnIndex("fee");
                if (feeIndex != -1 && !cursor.isNull(feeIndex)) {
                    appointment.setFee(cursor.getInt(feeIndex));
                }

                appointmentList.add(appointment);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.notifyDataSetChanged();


        if (appointmentList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvAppointments.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    private void viewAppointmentDetails(Appointment appointment) {
        Intent intent = new Intent(this, AppointmentDetailActivity.class);
        intent.putExtra("appointment_id", appointment.getId());
        startActivity(intent);
    }

    private void startExamination(Appointment appointment) {
        Intent intent = new Intent(this, CreateMedicalRecordActivity.class);
        intent.putExtra("appointment_id", appointment.getId());
        intent.putExtra("patient_email", appointment.getPatientEmail());
        intent.putExtra("patient_name", appointment.getPatientName());
        startActivity(intent);
    }

    /**
     * Hiển thị dialog xác nhận đăng xuất
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Thực hiện đăng xuất
     */
    private void performLogout() {
        // Xóa session đăng nhập
        prefs.edit().clear().apply();

        // Hiển thị thông báo
        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
        loadAppointments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}