package com.example.healthprofile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
    private CardView btnLogout;

    private SQLiteDatabase db;
    private int doctorId;
    private String doctorName;
    private List<Appointment> appointmentList;
    private DoctorAppointmentAdapter adapter; // ← ĐÃ SỬA: Đổi từ AppointmentAdapter sang DoctorAppointmentAdapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Kiểm tra session - dùng UserSession chung
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = prefs.getString("role", "");

        if (!"doctor".equals(role)) {
            finish();
            return;
        }

        int userId = prefs.getInt("user_id", 0);
        doctorName = prefs.getString("fullName", "");

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        // Lấy doctor_id từ bảng liên kết
        Cursor cursor = db.rawQuery("SELECT doctor_id FROM user_doctors WHERE user_id = " + userId, null);
        if (cursor.moveToFirst()) {
            doctorId = cursor.getInt(0);
        } else {
            // Nếu chưa có, tạo doctor_id = user_id
            doctorId = userId;
        }
        cursor.close();

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
        btnLogout = findViewById(R.id.btn_logout);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        tvDoctorName.setText(prefs.getString("fullName", ""));

        // Lấy specialization từ user_doctors
        Cursor cursor = db.rawQuery("SELECT specialization FROM user_doctors WHERE user_id = " +
                prefs.getInt("user_id", 0), null);
        if (cursor.moveToFirst()) {
            tvSpecialization.setText(cursor.getString(0));
        }
        cursor.close();

        btnLogout.setOnClickListener(v -> logout());

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();

        // ← ĐÃ SỬA: Sử dụng DoctorAppointmentAdapter
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

        // Query lấy tất cả lịch hẹn của bác sĩ, sắp xếp theo ngày mới nhất
        Cursor cursor = db.rawQuery("SELECT * FROM appointments WHERE doctor_id = " + doctorId +
                " ORDER BY appointment_date DESC, appointment_time DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Appointment appointment = new Appointment();
                appointment.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                appointment.setPatientEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
                appointment.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
                appointment.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow("doctor_id")));
                appointment.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
                appointment.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow("appointment_date")));
                appointment.setAppointmentTime(cursor.getString(cursor.getColumnIndexOrThrow("appointment_time")));
                appointment.setReason(cursor.getString(cursor.getColumnIndexOrThrow("reason")));
                appointment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));


                // Kiểm tra notes có null không
                int notesIndex = cursor.getColumnIndexOrThrow("notes");
                String notes = cursor.isNull(notesIndex) ? "" : cursor.getString(notesIndex);
                appointment.setNotes(notes);

                // Kiểm tra phone có null không
                int phoneIndex = cursor.getColumnIndex("phone");
                if (phoneIndex != -1 && !cursor.isNull(phoneIndex)) {
                    appointment.setPhone(cursor.getString(phoneIndex));
                }

                // Kiểm tra fee có null không
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
        // Chuyển đến màn hình khám bệnh
        Intent intent = new Intent(this, CreateMedicalRecordActivity.class);
        intent.putExtra("appointment_id", appointment.getId());
        intent.putExtra("patient_email", appointment.getPatientEmail());
        intent.putExtra("patient_name", appointment.getPatientName());
        startActivity(intent);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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