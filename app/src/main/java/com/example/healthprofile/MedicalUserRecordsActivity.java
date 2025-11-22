package com.example.healthprofile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.MedicalUserRecordAdapter;
import com.example.healthprofile.model.HealthRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị bệnh án của bệnh nhân
 * Chỉ hiển thị các bệnh án của bệnh nhân đang đăng nhập
 */
public class MedicalUserRecordsActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView rvMedicalRecords;
    private LinearLayout emptyState;
    private ImageView btnBack;
    private SQLiteDatabase db;
    private String patientEmail;
    private List<HealthRecord> recordList;
    private List<HealthRecord> filteredList;
    private MedicalUserRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_medical_records);

        // Lấy email bệnh nhân từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        patientEmail = prefs.getString("email", "");

        if (patientEmail.isEmpty()) {
            finish();
            return;
        }

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        initViews();
        loadMedicalRecords();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        searchView = findViewById(R.id.search_view);
        rvMedicalRecords = findViewById(R.id.rv_medical_records);
        emptyState = findViewById(R.id.empty_state);

        btnBack.setOnClickListener(v -> finish());

        rvMedicalRecords.setLayoutManager(new LinearLayoutManager(this));
        recordList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new MedicalUserRecordAdapter(this, filteredList, record -> {
            // Mở chi tiết bệnh án
            Intent intent = new Intent(MedicalUserRecordsActivity.this, MedicalRecordDetailActivity.class);
            intent.putExtra("record_id", record.getId());
            startActivity(intent);
        });
        rvMedicalRecords.setAdapter(adapter);

        // Setup search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecords(newText);
                return true;
            }
        });
    }

    private void loadMedicalRecords() {
        recordList.clear();
        filteredList.clear();

        // Query lấy tất cả medical_records của bệnh nhân này
        Cursor cursor = db.rawQuery(
                "SELECT mr.*, d.name as doctor_full_name " +
                        "FROM medical_records mr " +
                        "LEFT JOIN user_doctors ud ON mr.doctor_user_id = ud.user_id " +
                        "LEFT JOIN doctors d ON ud.doctor_id = d.id " +
                        "WHERE mr.patient_email = ? " +
                        "ORDER BY mr.visit_date DESC, mr.created_at DESC",
                new String[]{patientEmail}
        );

        if (cursor.moveToFirst()) {
            do {
                HealthRecord record = new HealthRecord();
                record.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

                // Lấy tên bác sĩ
                int doctorNameIndex = cursor.getColumnIndex("doctor_full_name");
                if (doctorNameIndex != -1 && !cursor.isNull(doctorNameIndex)) {
                    record.setDoctorName(cursor.getString(doctorNameIndex));
                } else {
                    int nameIndex = cursor.getColumnIndex("doctor_name");
                    if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                        record.setDoctorName(cursor.getString(nameIndex));
                    }
                }

                record.setDate(cursor.getString(cursor.getColumnIndexOrThrow("visit_date")));

                int complaintIndex = cursor.getColumnIndex("chief_complaint");
                if (complaintIndex != -1 && !cursor.isNull(complaintIndex)) {
                    record.setSymptoms(cursor.getString(complaintIndex));
                }

                int diagnosisIndex = cursor.getColumnIndex("diagnosis");
                if (diagnosisIndex != -1 && !cursor.isNull(diagnosisIndex)) {
                    record.setDiagnosis(cursor.getString(diagnosisIndex));
                }

                int notesIndex = cursor.getColumnIndex("notes");
                if (notesIndex != -1 && !cursor.isNull(notesIndex)) {
                    record.setNotes(cursor.getString(notesIndex));
                }

                recordList.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();

        filteredList.addAll(recordList);
        adapter.notifyDataSetChanged();

        updateEmptyState();
    }

    private void filterRecords(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(recordList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (HealthRecord record : recordList) {
                String doctorName = record.getDoctorName() != null ? record.getDoctorName().toLowerCase() : "";
                String diagnosis = record.getDiagnosis() != null ? record.getDiagnosis().toLowerCase() : "";
                String symptoms = record.getSymptoms() != null ? record.getSymptoms().toLowerCase() : "";
                String date = record.getDate() != null ? record.getDate().toLowerCase() : "";

                if (doctorName.contains(lowerQuery) ||
                        diagnosis.contains(lowerQuery) ||
                        symptoms.contains(lowerQuery) ||
                        date.contains(lowerQuery)) {
                    filteredList.add(record);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvMedicalRecords.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvMedicalRecords.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicalRecords();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}