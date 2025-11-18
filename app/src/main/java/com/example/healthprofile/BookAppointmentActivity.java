package com.example.healthprofile;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor; // Import cần thiết
import android.database.sqlite.SQLiteDatabase; // Import cần thiết
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {

    private static final String DATABASE_NAME = "health_profile.db"; // Tên database
    private static final String TABLE_APPOINTMENTS = "appointments"; // Tên bảng

    private ImageView ivDoctorAvatar;
    private TextView tvDoctorName, tvDoctorRating, tvDoctorExperience;
    private TextInputEditText etPatientName, etPhone, etReason;
    private LinearLayout layoutDatePicker;
    private TextView tvSelectedDate;
    private CardView cardSummary;
    private TextView tvSummaryDate, tvSummaryTime, tvSummaryFee;
    private Button btnConfirmAppointment;
    private ImageButton btnBack;

    private String selectedDate = "";
    private String selectedTime = "";
    private TextView selectedTimeSlot = null;
    private int doctorId;
    private String doctorName;
    private float doctorRating;
    private int doctorExperience;
    private int doctorImageResource;
    private String userEmail;
    private String userFullName;
    private String userPhone;
    private SQLiteDatabase database;

    private int[] morningSlotIds = {R.id.slot_08_00, R.id.slot_09_00, R.id.slot_10_00, R.id.slot_11_00};
    private int[] afternoonSlotIds = {R.id.slot_14_00, R.id.slot_15_00, R.id.slot_16_00, R.id.slot_17_00};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        openDatabase();

        initViews();
        loadUserInfo();
        loadDoctorInfo();
        setupTimeSlots();
        setupClickListeners();
    }

    private void openDatabase() {
        try {
            database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể mở database", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        ivDoctorAvatar = findViewById(R.id.iv_doctor_avatar);
        tvDoctorName = findViewById(R.id.tv_doctor_name);
        tvDoctorRating = findViewById(R.id.tv_doctor_rating);
        tvDoctorExperience = findViewById(R.id.tv_doctor_experience);
        etPatientName = findViewById(R.id.et_patient_name);
        etPhone = findViewById(R.id.et_phone);
        etReason = findViewById(R.id.et_reason);
        layoutDatePicker = findViewById(R.id.layout_date_picker);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        cardSummary = findViewById(R.id.card_summary);
        tvSummaryDate = findViewById(R.id.tv_summary_date);
        tvSummaryTime = findViewById(R.id.tv_summary_time);
        tvSummaryFee = findViewById(R.id.tv_summary_fee);
        btnConfirmAppointment = findViewById(R.id.btn_confirm_appointment);
        btnBack = findViewById(R.id.btn_back);
    }

    private void loadUserInfo() {
        // Lấy thông tin user từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");
        userFullName = prefs.getString("fullName", "");
        userPhone = prefs.getString("phone", "");

        // Tự động điền thông tin user
        if (etPatientName != null && !userFullName.isEmpty()) {
            etPatientName.setText(userFullName);
        }

        if (etPhone != null && !userPhone.isEmpty()) {
            etPhone.setText(userPhone);
        }
    }

    private void loadDoctorInfo() {
        // Get doctor info from intent
        doctorId = getIntent().getIntExtra("doctor_id", 0);
        doctorName = getIntent().getStringExtra("doctor_name");
        doctorRating = getIntent().getFloatExtra("doctor_rating", 0f);
        doctorExperience = getIntent().getIntExtra("doctor_experience", 0);
        doctorImageResource = getIntent().getIntExtra("doctor_image", R.drawable.doctor_placeholder);

        // Set doctor info
        if (doctorName != null) {
            tvDoctorName.setText(doctorName);
            tvDoctorRating.setText(String.format(Locale.getDefault(), "%.1f", doctorRating));
            tvDoctorExperience.setText(doctorExperience + " năm kinh nghiệm");
            ivDoctorAvatar.setImageResource(doctorImageResource);
        }
    }

    private void setupTimeSlots() {
        // Setup morning slots
        for (int slotId : morningSlotIds) {
            TextView slot = findViewById(slotId);
            slot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectTimeSlot((TextView) v);
                }
            });
        }

        // Setup afternoon slots
        for (int slotId : afternoonSlotIds) {
            TextView slot = findViewById(slotId);
            slot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectTimeSlot((TextView) v);
                }
            });
        }
    }

    private void selectTimeSlot(TextView slot) {
        // Deselect previous slot
        if (selectedTimeSlot != null) {
            selectedTimeSlot.setBackgroundResource(R.drawable.time_slot_normal);
            selectedTimeSlot.setTextColor(Color.parseColor("#666666"));
        }

        // Select new slot
        selectedTimeSlot = slot;
        slot.setBackgroundResource(R.drawable.time_slot_selected);
        slot.setTextColor(Color.WHITE);
        selectedTime = slot.getText().toString();

        // Update summary
        updateSummary();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layoutDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        btnConfirmAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAppointment();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        selectedDate = sdf.format(selectedCalendar.getTime());

                        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
                        tvSelectedDate.setText(displayFormat.format(selectedCalendar.getTime()));
                        tvSelectedDate.setTextColor(Color.parseColor("#333333"));

                        updateSummary();
                    }
                },
                year, month, day
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        // Set maximum date to 30 days from now
        Calendar maxCalendar = Calendar.getInstance();
        maxCalendar.add(Calendar.DAY_OF_MONTH, 30);
        datePickerDialog.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void updateSummary() {
        if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
            cardSummary.setVisibility(View.VISIBLE);
            tvSummaryDate.setText(selectedDate);
            tvSummaryTime.setText(selectedTime);
            // Giả định phí không đổi
            tvSummaryFee.setText("200.000 VNĐ");
        }
    }

    private void confirmAppointment() {
        String patientName = etPatientName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        // Validation
        if (patientName.isEmpty()) {
            etPatientName.setError("Vui lòng nhập họ tên");
            etPatientName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            etPhone.setError("Số điện thoại không hợp lệ");
            etPhone.requestFocus();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày khám", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giờ khám", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isEmpty()) {
            etReason.setError("Vui lòng nhập lý do khám");
            etReason.requestFocus();
            return;
        }

        // Show confirmation dialog
        showConfirmationDialog(patientName, phone, reason);
    }

    private void showConfirmationDialog(String patientName, String phone, String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đặt lịch");

        String message = "Thông tin đặt lịch:\n\n" +
                "Bác sĩ: " + doctorName + "\n" +
                "Bệnh nhân: " + patientName + "\n" +
                "Số điện thoại: " + phone + "\n" +
                "Ngày khám: " + selectedDate + "\n" +
                "Giờ khám: " + selectedTime + "\n" +
                "Lý do: " + reason + "\n" +
                "Phí tư vấn: 200.000 VNĐ";

        builder.setMessage(message);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            // Save appointment to database using direct SQLite access
            new SaveAppointmentTask(patientName, phone, reason).execute();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    /**
     * AsyncTask để lưu appointment vào database TRỰC TIẾP
     */
    private class SaveAppointmentTask extends AsyncTask<Void, Void, Long> {
        private String patientName;
        private String phone;
        private String reason;

        SaveAppointmentTask(String patientName, String phone, String reason) {
            this.patientName = patientName;
            this.phone = phone;
            this.reason = reason;
        }

        private long insertAppointmentDirectly() {
            if (database == null || !database.isOpen()) {
                // Thử mở lại database nếu nó đóng
                openDatabase();
                if (database == null || !database.isOpen()) {
                    return -1L;
                }
            }

            // Dữ liệu Appointment (giả lập)
            int docId = doctorId;
            String docName = doctorName;
            String patName = patientName;
            String apPhone = phone;
            String date = selectedDate;
            String time = selectedTime;
            String rs = reason;
            String status = "pending";
            long timestamp = System.currentTimeMillis();
            int fee = 200000;

            // Câu lệnh SQL INSERT (Sử dụng escapeString để bảo mật cơ bản)
            String sql = "INSERT INTO " + TABLE_APPOINTMENTS +
                    " (user_email, doctor_id, doctor_name, patient_name, phone, appointment_date, appointment_time, reason, status, timestamp, fee) " +
                    "VALUES ('" + escapeString(userEmail) + "', " +
                    docId + ", " +
                    "'" + escapeString(docName) + "', " +
                    "'" + escapeString(patName) + "', " +
                    "'" + escapeString(apPhone) + "', " +
                    "'" + escapeString(date) + "', " +
                    "'" + escapeString(time) + "', " +
                    "'" + escapeString(rs) + "', " +
                    "'" + escapeString(status) + "', " +
                    timestamp + ", " +
                    fee + ")";

            try {
                database.execSQL(sql);

                // Lấy ID của record vừa insert
                Cursor cursor = database.rawQuery("SELECT last_insert_rowid()", null);
                long id = -1;
                if (cursor.moveToFirst()) {
                    id = cursor.getLong(0);
                }
                cursor.close();
                return id;
            } catch (Exception e) {
                e.printStackTrace();
                return -1L;
            }
        }

        @Override
        protected Long doInBackground(Void... voids) {
            // Gọi hàm insert trực tiếp
            return insertAppointmentDirectly();
        }

        @Override
        protected void onPostExecute(Long appointmentId) {
            super.onPostExecute(appointmentId);

            if (appointmentId > 0) {
                // Thành công
                AlertDialog.Builder builder = new AlertDialog.Builder(BookAppointmentActivity.this);
                builder.setTitle("Đặt lịch thành công");
                builder.setMessage("Lịch hẹn của bạn đã được đặt thành công. Chúng tôi sẽ liên hệ với bạn qua số điện thoại " + phone + " để xác nhận.");
                builder.setPositiveButton("Đồng ý", (dialog, which) -> {
                    // Return to previous screen
                    finish();
                });
                builder.setCancelable(false);
                builder.show();

                Toast.makeText(BookAppointmentActivity.this, "Đặt lịch thành công!", Toast.LENGTH_LONG).show();
            } else {
                // Thất bại
                Toast.makeText(BookAppointmentActivity.this, "Lỗi khi đặt lịch. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}