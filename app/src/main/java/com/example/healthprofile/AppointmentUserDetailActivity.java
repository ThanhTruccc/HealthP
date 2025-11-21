package com.example.healthprofile;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.healthprofile.model.Appointment;

public class AppointmentUserDetailActivity extends AppCompatActivity {

    private TextView tvPatientName, tvPatientEmail, tvPhone;
    private TextView tvDoctorName, tvDateTime, tvReason, tvFee;
    private TextView tvStatus, tvNotes;
    private CardView cardStatus;
    private Button btnCancel;
    private LinearLayout layoutActions;
    private ImageView btnBack;

    private SQLiteDatabase db;
    private Appointment appointment;
    private int appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_user_detail);

        appointmentId = getIntent().getIntExtra("appointment_id", 0);
        if (appointmentId == 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        initViews();
        loadAppointmentDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvPatientName = findViewById(R.id.tv_patient_name);
        tvPatientEmail = findViewById(R.id.tv_patient_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvDoctorName = findViewById(R.id.tv_doctor_name);
        tvDateTime = findViewById(R.id.tv_date_time);
        tvReason = findViewById(R.id.tv_reason);
        tvFee = findViewById(R.id.tv_fee);
        tvStatus = findViewById(R.id.tv_status);
        //tvNotes = findViewById(R.id.tv_notes);
        cardStatus = findViewById(R.id.card_status);
        layoutActions = findViewById(R.id.layout_actions);
        btnCancel = findViewById(R.id.btn_cancel);

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> showCancelDialog());
    }

    private void loadAppointmentDetails() {
        Cursor cursor = db.rawQuery("SELECT * FROM appointments WHERE id = " + appointmentId, null);

        if (cursor.moveToFirst()) {
            appointment = new Appointment();
            appointment.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            appointment.setPatientEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
            appointment.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
            appointment.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            appointment.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow("doctor_id")));
            appointment.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
            appointment.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow("appointment_date")));
            appointment.setAppointmentTime(cursor.getString(cursor.getColumnIndexOrThrow("appointment_time")));
            appointment.setReason(cursor.getString(cursor.getColumnIndexOrThrow("reason")));
            appointment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
            appointment.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
            appointment.setFee(cursor.getInt(cursor.getColumnIndexOrThrow("fee")));

            displayAppointmentDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
        }
        cursor.close();
    }

    private void displayAppointmentDetails() {
        tvPatientName.setText(appointment.getPatientName());
        tvPatientEmail.setText(appointment.getPatientEmail());
        tvPhone.setText(appointment.getPhone() != null ? appointment.getPhone() : "Chưa có");
        tvDoctorName.setText(appointment.getDoctorName());
        tvDateTime.setText(appointment.getFormattedDateTime());
        tvReason.setText(appointment.getReason());
        tvFee.setText(appointment.getFeeFormatted());
        tvStatus.setText(appointment.getStatusText());
        cardStatus.setCardBackgroundColor(appointment.getStatusColor());

//        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
//            tvNotes.setText(appointment.getNotes());
//            tvNotes.setVisibility(View.VISIBLE);
//        } else {
//            tvNotes.setVisibility(View.GONE);
//        }

        updateActionButtons();
    }

    private void updateActionButtons() {
        String status = appointment.getStatus();

        btnCancel.setVisibility(View.GONE);

        switch (status) {
            case "pending":
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case "confirmed":
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case "completed":
            case "cancelled":
                layoutActions.setVisibility(View.GONE);
                break;
        }
    }

    private void confirmAppointment() {
        db.execSQL("UPDATE appointments SET status = 'confirmed' WHERE id = " + appointmentId);
        appointment.setStatus("confirmed");
        displayAppointmentDetails();
        Toast.makeText(this, "Đã xác nhận lịch hẹn", Toast.LENGTH_SHORT).show();
    }

    private void completeAppointment() {
        db.execSQL("UPDATE appointments SET status = 'completed' WHERE id = " + appointmentId);
        appointment.setStatus("completed");
        displayAppointmentDetails();
        Toast.makeText(this, "Đã hoàn thành lịch hẹn", Toast.LENGTH_SHORT).show();
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hủy lịch hẹn");
        builder.setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này không?");

        builder.setPositiveButton("Hủy lịch", (dialog, which) -> {
            db.execSQL("UPDATE appointments SET status = 'cancelled' WHERE id = " + appointmentId);
            appointment.setStatus("cancelled");
            displayAppointmentDetails();
            Toast.makeText(this, "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void startExamination() {
        Intent intent = new Intent(this, CreateMedicalRecordActivity.class);
        intent.putExtra("appointment_id", appointment.getId());
        intent.putExtra("patient_email", appointment.getPatientEmail());
        intent.putExtra("patient_name", appointment.getPatientName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointmentDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}