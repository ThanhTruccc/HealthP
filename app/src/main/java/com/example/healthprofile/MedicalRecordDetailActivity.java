package com.example.healthprofile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.PrescriptionItemAdapter;
import com.example.healthprofile.adapter.PrescriptionViewAdapter;
import com.example.healthprofile.model.HealthRecord;
import com.example.healthprofile.model.PrescriptionItem;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDetailActivity extends AppCompatActivity {

    private TextView tvPatientName, tvDoctorName, tvVisitDate;
    private TextView tvComplaint, tvSymptoms, tvDiagnosis;
    private TextView tvBloodPressure, tvTemperature, tvHeartRate, tvWeight, tvHeight;
    private TextView tvTreatmentPlan, tvNotes;
    private RecyclerView rvPrescriptions;
    private LinearLayout layoutVitalSigns, layoutTreatment, layoutNotes, layoutSymptoms;
    private ImageView btnBack;

    private SQLiteDatabase db;
    private int recordId;
    private HealthRecord record;
    private List<PrescriptionItem> prescriptionList;
    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record_detail);

        recordId = getIntent().getIntExtra("record_id", 0);
        if (recordId == 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy bệnh án", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        initViews();
        loadRecordDetails();
        loadPrescriptions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvPatientName = findViewById(R.id.tv_patient_name);
        tvDoctorName = findViewById(R.id.tv_doctor_name);
        tvVisitDate = findViewById(R.id.tv_visit_date);
        tvComplaint = findViewById(R.id.tv_complaint);
        tvSymptoms = findViewById(R.id.tv_symptoms);
        tvDiagnosis = findViewById(R.id.tv_diagnosis);
        tvBloodPressure = findViewById(R.id.tv_blood_pressure);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvHeartRate = findViewById(R.id.tv_heart_rate);
        tvWeight = findViewById(R.id.tv_weight);
        tvHeight = findViewById(R.id.tv_height);
        tvTreatmentPlan = findViewById(R.id.tv_treatment_plan);
        tvNotes = findViewById(R.id.tv_notes);
        rvPrescriptions = findViewById(R.id.rv_prescriptions);
        layoutVitalSigns = findViewById(R.id.layout_vital_signs);
        layoutTreatment = findViewById(R.id.layout_treatment);
        layoutNotes = findViewById(R.id.layout_notes);
        layoutSymptoms = findViewById(R.id.layout_symptoms);

        btnBack.setOnClickListener(v -> finish());

        rvPrescriptions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadRecordDetails() {
        Cursor cursor = db.rawQuery(
                "SELECT mr.*, u.fullName as patient_full_name " +
                        "FROM medical_records mr " +
                        "LEFT JOIN user u ON mr.patient_email = u.email " +
                        "WHERE mr.id = ?",
                new String[]{String.valueOf(recordId)}
        );

        if (cursor.moveToFirst()) {
            record = new HealthRecord();
            record.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

            // Lấy tên bệnh nhân
            int patientNameIdx = cursor.getColumnIndex("patient_full_name");
            if (patientNameIdx != -1 && !cursor.isNull(patientNameIdx)) {
                patientName = cursor.getString(patientNameIdx);
            } else {
                int nameIdx = cursor.getColumnIndex("patient_name");
                if (nameIdx != -1 && !cursor.isNull(nameIdx)) {
                    patientName = cursor.getString(nameIdx);
                } else {
                    patientName = "Không rõ";
                }
            }

            record.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
            record.setDate(cursor.getString(cursor.getColumnIndexOrThrow("visit_date")));

            // Chief complaint
            int complaintIndex = cursor.getColumnIndex("chief_complaint");
            if (complaintIndex != -1 && !cursor.isNull(complaintIndex)) {
                record.setSymptoms(cursor.getString(complaintIndex));
            }

            // Diagnosis
            int diagnosisIndex = cursor.getColumnIndex("diagnosis");
            if (diagnosisIndex != -1 && !cursor.isNull(diagnosisIndex)) {
                record.setDiagnosis(cursor.getString(diagnosisIndex));
            }

            // Additional symptoms
            int symptomsIndex = cursor.getColumnIndex("symptoms");
            if (symptomsIndex != -1 && !cursor.isNull(symptomsIndex)) {
                record.setNotes(cursor.getString(symptomsIndex)); // Tạm lưu vào notes
            }

            // Vital signs
            int bpIndex = cursor.getColumnIndex("blood_pressure");
            if (bpIndex != -1 && !cursor.isNull(bpIndex)) {
                record.setBloodPressure(cursor.getString(bpIndex));
            }

            int tempIndex = cursor.getColumnIndex("temperature");
            if (tempIndex != -1 && !cursor.isNull(tempIndex)) {
                record.setTemperature(cursor.getFloat(tempIndex));
            }

            int hrIndex = cursor.getColumnIndex("heart_rate");
            if (hrIndex != -1) {
                int heartRate = cursor.getInt(hrIndex);
                record.setHeight(heartRate); // Tạm lưu heart_rate vào height
            }

            int weightIndex = cursor.getColumnIndex("weight");
            if (weightIndex != -1 && !cursor.isNull(weightIndex)) {
                record.setWeight(cursor.getFloat(weightIndex));
            }

            int heightIndex = cursor.getColumnIndex("height");
            if (heightIndex != -1 && !cursor.isNull(heightIndex)) {
                float height = cursor.getFloat(heightIndex);
                if (height > 0) {
                    record.setHeight(height); // Ghi đè nếu có chiều cao thật
                }
            }

            // Treatment plan
            int treatmentIndex = cursor.getColumnIndex("treatment_plan");
            if (treatmentIndex != -1 && !cursor.isNull(treatmentIndex)) {
                record.setTreatment(cursor.getString(treatmentIndex));
            }

            displayRecordDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy bệnh án", Toast.LENGTH_SHORT).show();
            finish();
        }
        cursor.close();
    }

    private void displayRecordDetails() {
        tvPatientName.setText(patientName);
        tvDoctorName.setText("BS. " + record.getDoctorName());
        tvVisitDate.setText(formatDate(record.getDate()));

        // Chief complaint
        if (record.getSymptoms() != null && !record.getSymptoms().isEmpty()) {
            tvComplaint.setText(record.getSymptoms());
        } else {
            tvComplaint.setText("Không có thông tin");
        }

        // Additional symptoms (từ notes)
        if (record.getNotes() != null && !record.getNotes().isEmpty()) {
            tvSymptoms.setText(record.getNotes());
            layoutSymptoms.setVisibility(View.VISIBLE);
        } else {
            layoutSymptoms.setVisibility(View.GONE);
        }

        // Diagnosis
        if (record.getDiagnosis() != null && !record.getDiagnosis().isEmpty()) {
            tvDiagnosis.setText(record.getDiagnosis());
        } else {
            tvDiagnosis.setText("Chưa có chẩn đoán");
        }

        // Vital Signs
        boolean hasVitalSigns = false;

        if (record.getBloodPressure() != null && !record.getBloodPressure().isEmpty()) {
            tvBloodPressure.setText(record.getBloodPressure());
            hasVitalSigns = true;
        } else {
            tvBloodPressure.setText("N/A");
        }

        if (record.getTemperature() > 0) {
            tvTemperature.setText(String.format("%.1f°C", record.getTemperature()));
            hasVitalSigns = true;
        } else {
            tvTemperature.setText("N/A");
        }

        // Heart rate (đang tạm lưu ở height khi load)
        float heartRate = record.getHeight();
        if (heartRate > 0 && heartRate < 300) { // Giả sử heart rate < 300
            tvHeartRate.setText(String.format("%.0f bpm", heartRate));
            hasVitalSigns = true;
        } else {
            tvHeartRate.setText("N/A");
        }

        if (record.getWeight() > 0) {
            tvWeight.setText(String.format("%.1f kg", record.getWeight()));
            hasVitalSigns = true;
        } else {
            tvWeight.setText("N/A");
        }

        // Load lại height từ database
        loadActualHeight();

        layoutVitalSigns.setVisibility(hasVitalSigns ? View.VISIBLE : View.GONE);

        // Treatment Plan
        if (record.getTreatment() != null && !record.getTreatment().isEmpty()) {
            tvTreatmentPlan.setText(record.getTreatment());
            layoutTreatment.setVisibility(View.VISIBLE);
        } else {
            layoutTreatment.setVisibility(View.GONE);
        }

        // Notes - Load từ database
        loadActualNotes();
    }

    private void loadActualHeight() {
        Cursor cursor = db.rawQuery("SELECT height FROM medical_records WHERE id = ?",
                new String[]{String.valueOf(recordId)});
        if (cursor.moveToFirst()) {
            int heightIndex = cursor.getColumnIndex("height");
            if (heightIndex != -1 && !cursor.isNull(heightIndex)) {
                float height = cursor.getFloat(heightIndex);
                if (height > 0) {
                    tvHeight.setText(String.format("%.1f cm", height));
                } else {
                    tvHeight.setText("N/A");
                }
            } else {
                tvHeight.setText("N/A");
            }
        }
        cursor.close();
    }

    private void loadActualNotes() {
        Cursor cursor = db.rawQuery("SELECT notes FROM medical_records WHERE id = ?",
                new String[]{String.valueOf(recordId)});
        if (cursor.moveToFirst()) {
            int notesIndex = cursor.getColumnIndex("notes");
            if (notesIndex != -1 && !cursor.isNull(notesIndex)) {
                String notes = cursor.getString(notesIndex);
                if (notes != null && !notes.isEmpty()) {
                    tvNotes.setText(notes);
                    layoutNotes.setVisibility(View.VISIBLE);
                } else {
                    layoutNotes.setVisibility(View.GONE);
                }
            } else {
                layoutNotes.setVisibility(View.GONE);
            }
        }
        cursor.close();
    }

    private void loadPrescriptions() {
        prescriptionList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM prescriptions WHERE medical_record_id = ?",
                new String[]{String.valueOf(recordId)});

        if (cursor.moveToFirst()) {
            do {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicationName(cursor.getString(cursor.getColumnIndexOrThrow("medication_name")));
                item.setDosage(cursor.getString(cursor.getColumnIndexOrThrow("dosage")));
                item.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow("frequency")));
                item.setDuration(cursor.getString(cursor.getColumnIndexOrThrow("duration")));

                int qtyIndex = cursor.getColumnIndex("quantity");
                if (qtyIndex != -1 && !cursor.isNull(qtyIndex)) {
                    item.setQuantity(cursor.getInt(qtyIndex));
                }

                int instIndex = cursor.getColumnIndex("instructions");
                if (instIndex != -1 && !cursor.isNull(instIndex)) {
                    item.setInstructions(cursor.getString(instIndex));
                }

                prescriptionList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (!prescriptionList.isEmpty()) {
            PrescriptionViewAdapter adapter = new PrescriptionViewAdapter(this, prescriptionList);
            rvPrescriptions.setAdapter(adapter);
        }
    }

    private String formatDate(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        try {
            String[] parts = date.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {
            return date;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}