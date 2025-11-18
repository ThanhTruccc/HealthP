package com.example.healthprofile;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.PrescriptionItemAdapter;
import com.example.healthprofile.model.PrescriptionItem;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateMedicalRecordActivity extends AppCompatActivity {

    private TextView tvPatientName, tvDate;
    private EditText etChiefComplaint, etDiagnosis, etSymptoms;
    private EditText etBloodPressure, etTemperature, etHeartRate, etWeight, etHeight;
    private EditText etTreatmentPlan, etNotes;
    private RecyclerView rvPrescriptions;
    private Button btnAddMedication, btnSave;
    private ImageButton btnBack;

    private SQLiteDatabase db;
    private int appointmentId;
    private int doctorUserId;
    private String patientEmail, patientName, doctorName;
    private List<PrescriptionItem> prescriptionList;
    private PrescriptionItemAdapter adapter;

    // Dropdown options
    private String[] dosageOptions = {
            "1/4 viên", "1/2 viên", "1 viên", "2 viên", "3 viên",
            "1 gói", "2 gói", "5ml", "10ml", "15ml", "1 ống"
    };

    private String[] frequencyOptions = {
            "1 lần/ngày", "2 lần/ngày", "3 lần/ngày", "4 lần/ngày",
            "Sáng 1 lần", "Tối 1 lần", "Sáng - Tối",
            "Sáng - Trưa - Tối", "Khi cần thiết",
            "Mỗi 4 giờ", "Mỗi 6 giờ", "Mỗi 8 giờ"
    };

    private String[] durationOptions = {
            "3 ngày", "5 ngày", "7 ngày", "10 ngày", "14 ngày",
            "3 tuần", "1 tháng", "2 tháng", "3 tháng",
            "Dùng hết", "Dùng lâu dài"
    };

    private String[] instructionTemplates = {
            "Uống sau ăn 30 phút",
            "Uống trước ăn 30 phút",
            "Uống trong khi ăn",
            "Uống khi đói",
            "Uống trước khi ngủ",
            "Uống khi thức dậy",
            "Uống với nhiều nước",
            "Ngậm dưới lưỡi",
            "Bôi ngoài da",
            "Nhỏ mắt",
            "Nhỏ tai",
            "Nhỏ mũi"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_medical_record);

        appointmentId = getIntent().getIntExtra("appointment_id", 0);
        patientEmail = getIntent().getStringExtra("patient_email");
        patientName = getIntent().getStringExtra("patient_name");

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        doctorUserId = prefs.getInt("user_id", 0);
        doctorName = prefs.getString("fullName", "");

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        initViews();
    }

    private void initViews() {
        tvPatientName = findViewById(R.id.tv_patient_name_record);
        tvDate = findViewById(R.id.tv_visit_date);
        etChiefComplaint = findViewById(R.id.et_chief_complaint);
        etDiagnosis = findViewById(R.id.et_diagnosis);
        etSymptoms = findViewById(R.id.et_symptoms);
        etBloodPressure = findViewById(R.id.et_blood_pressure);
        etTemperature = findViewById(R.id.et_temperature);
        etHeartRate = findViewById(R.id.et_heart_rate);
        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        etTreatmentPlan = findViewById(R.id.et_treatment_plan);
        etNotes = findViewById(R.id.et_record_notes);
        rvPrescriptions = findViewById(R.id.rv_prescriptions);
        btnAddMedication = findViewById(R.id.btn_add_medication);
        btnSave = findViewById(R.id.btn_save_record);
        btnBack = findViewById(R.id.btn_back_record);

        tvPatientName.setText("Bệnh nhân: " + patientName);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText("Ngày khám: " + sdf.format(Calendar.getInstance().getTime()));

        btnBack.setOnClickListener(v -> finish());
        btnAddMedication.setOnClickListener(v -> showAddMedicationDialog());
        btnSave.setOnClickListener(v -> saveMedicalRecord());

        rvPrescriptions.setLayoutManager(new LinearLayoutManager(this));
        prescriptionList = new ArrayList<>();
        adapter = new PrescriptionItemAdapter(this, prescriptionList, item -> {
            prescriptionList.remove(item);
            adapter.notifyDataSetChanged();
        });
        rvPrescriptions.setAdapter(adapter);
    }

    private void showAddMedicationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_prescription, null);

        TextInputEditText etMedicationName = dialogView.findViewById(R.id.et_medication_name);
        TextInputEditText etDosage = dialogView.findViewById(R.id.et_medication_dosage);
        TextInputEditText etFrequency = dialogView.findViewById(R.id.et_medication_frequency);
        TextInputEditText etDuration = dialogView.findViewById(R.id.et_medication_duration);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.et_medication_quantity);
        TextInputEditText etInstructions = dialogView.findViewById(R.id.et_medication_instructions);
        Button btnSelectMedication = dialogView.findViewById(R.id.btn_select_medication);

        // Make fields clickable to show dropdown
        setupDropdownField(etDosage, "Chọn liều lượng", dosageOptions);
        setupDropdownField(etFrequency, "Chọn tần suất", frequencyOptions);
        setupDropdownField(etDuration, "Chọn thời gian", durationOptions);
        setupDropdownField(etInstructions, "Chọn hướng dẫn", instructionTemplates);

        // Select medication button
        btnSelectMedication.setOnClickListener(v -> {
            showMedicationListDialog(etMedicationName);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm thuốc")
                .setView(dialogView)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnAdd.setOnClickListener(v -> {
                String medName = etMedicationName.getText().toString().trim();
                String dosage = etDosage.getText().toString().trim();
                String frequency = etFrequency.getText().toString().trim();
                String duration = etDuration.getText().toString().trim();
                String quantityStr = etQuantity.getText().toString().trim();
                String instructions = etInstructions.getText().toString().trim();

                // Validate required fields
                if (medName.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn tên thuốc", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dosage.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập liều lượng", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (frequency.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập tần suất", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (duration.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập thời gian sử dụng", Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);

                PrescriptionItem item = new PrescriptionItem();
                item.setMedicationName(medName);
                item.setDosage(dosage);
                item.setFrequency(frequency);
                item.setDuration(duration);
                item.setQuantity(quantity);
                item.setInstructions(instructions);

                prescriptionList.add(item);
                adapter.notifyDataSetChanged();

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void setupDropdownField(TextInputEditText editText, String title, String[] options) {
        // Make field non-editable but clickable
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setInputType(InputType.TYPE_NULL);

        editText.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title);

            // Add "Nhập thủ công" option
            String[] extendedOptions = new String[options.length + 1];
            extendedOptions[0] = "✏️ Nhập thủ công";
            System.arraycopy(options, 0, extendedOptions, 1, options.length);

            builder.setItems(extendedOptions, (dialog, which) -> {
                if (which == 0) {
                    // Show manual input dialog
                    showManualInputDialog(editText, title);
                } else {
                    // Set selected option
                    editText.setText(extendedOptions[which]);
                }
            });

            builder.setNegativeButton("Hủy", null);
            builder.show();
        });
    }

    private void showManualInputDialog(TextInputEditText targetEditText, String title) {
        EditText input = new EditText(this);
        input.setHint("Nhập " + title.toLowerCase());
        input.setPadding(50, 30, 50, 30);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (!value.isEmpty()) {
                        targetEditText.setText(value);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showMedicationListDialog(TextInputEditText etMedicationName) {
        List<String> medicationNames = new ArrayList<>();

        // Query danh sách thuốc
        Cursor cursor = db.rawQuery("SELECT name FROM medications ORDER BY name", null);

        if (cursor.moveToFirst()) {
            do {
                medicationNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (medicationNames.isEmpty()) {
            Toast.makeText(this, "Chưa có thuốc trong danh sách", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = medicationNames.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thuốc");

        // Add search functionality
        builder.setItems(items, (dialog, which) -> {
            etMedicationName.setText(items[which]);
        });

        builder.setNegativeButton("Hủy", null);

        // Add "Nhập thuốc khác" button
        builder.setNeutralButton("Nhập khác", (dialog, which) -> {
            showManualInputDialog(etMedicationName, "Tên thuốc");
        });

        builder.show();
    }

    private void saveMedicalRecord() {
        String chiefComplaint = etChiefComplaint.getText().toString().trim();
        String diagnosis = etDiagnosis.getText().toString().trim();
        String symptoms = etSymptoms.getText().toString().trim();
        String bloodPressure = etBloodPressure.getText().toString().trim();
        String temperatureStr = etTemperature.getText().toString().trim();
        String heartRateStr = etHeartRate.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String treatmentPlan = etTreatmentPlan.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (chiefComplaint.isEmpty() || diagnosis.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền ít nhất Lý do khám và Chẩn đoán", Toast.LENGTH_SHORT).show();
            return;
        }

        float temperature = temperatureStr.isEmpty() ? 0 : Float.parseFloat(temperatureStr);
        int heartRate = heartRateStr.isEmpty() ? 0 : Integer.parseInt(heartRateStr);
        float weight = weightStr.isEmpty() ? 0 : Float.parseFloat(weightStr);
        float height = heightStr.isEmpty() ? 0 : Float.parseFloat(heightStr);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String visitDate = sdf.format(Calendar.getInstance().getTime());

        try {
            // Insert bệnh án với doctor_user_id
            db.execSQL("INSERT INTO medical_records (appointment_id, patient_email, patient_name, doctor_user_id, doctor_name, " +
                    "visit_date, chief_complaint, diagnosis, symptoms, blood_pressure, temperature, heart_rate, weight, height, " +
                    "treatment_plan, notes, created_at) VALUES (" +
                    appointmentId + ", '" + escapeString(patientEmail) + "', '" + escapeString(patientName) + "', " +
                    doctorUserId + ", '" + escapeString(doctorName) + "', '" + visitDate + "', '" + escapeString(chiefComplaint) + "', '" +
                    escapeString(diagnosis) + "', '" + escapeString(symptoms) + "', '" + escapeString(bloodPressure) + "', " +
                    temperature + ", " + heartRate + ", " + weight + ", " + height + ", '" + escapeString(treatmentPlan) + "', '" +
                    escapeString(notes) + "', " + System.currentTimeMillis() + ")");

            // Lấy ID của medical record vừa tạo
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            cursor.moveToFirst();
            int medicalRecordId = cursor.getInt(0);
            cursor.close();

            // Insert các đơn thuốc với doctor_user_id
            for (PrescriptionItem item : prescriptionList) {
                db.execSQL("INSERT INTO prescriptions (medical_record_id, patient_email, doctor_user_id, medication_name, " +
                        "dosage, frequency, duration, instructions, quantity, created_at) VALUES (" +
                        medicalRecordId + ", '" + escapeString(patientEmail) + "', " + doctorUserId + ", '" +
                        escapeString(item.getMedicationName()) + "', '" + escapeString(item.getDosage()) + "', '" +
                        escapeString(item.getFrequency()) + "', '" + escapeString(item.getDuration()) + "', '" +
                        escapeString(item.getInstructions()) + "', " + item.getQuantity() + ", " +
                        System.currentTimeMillis() + ")");
            }

            // Cập nhật trạng thái appointment
            if (appointmentId > 0) {
                db.execSQL("UPDATE appointments SET status = 'completed' WHERE id = " + appointmentId);
            }

            Toast.makeText(this, "Đã lưu bệnh án thành công", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
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