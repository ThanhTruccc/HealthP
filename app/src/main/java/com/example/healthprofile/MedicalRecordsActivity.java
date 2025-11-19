package com.example.healthprofile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.DoctorMedicalRecordAdapter;
import com.example.healthprofile.model.HealthRecord;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordsActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView rvMedicalRecords;
    private LinearLayout emptyState;
    private CardView btnBack;

    private SQLiteDatabase db;
    private int doctorUserId;
    private List<HealthRecord> recordList;
    private List<HealthRecord> filteredList;
    private DoctorMedicalRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_records);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        doctorUserId = prefs.getInt("user_id", 0);

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

        adapter = new DoctorMedicalRecordAdapter(this, filteredList, record -> {
            Intent intent = new Intent(MedicalRecordsActivity.this, ViewMedicalRecordDetailActivity.class);
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

        // Query lấy tất cả medical_records của bác sĩ này
        Cursor cursor = db.rawQuery(
                "SELECT mr.*, u.full_name as patient_full_name " +
                        "FROM medical_records mr " +
                        "JOIN users u ON mr.patient_email = u.email " +
                        "WHERE mr.doctor_user_id = ? " +
                        "ORDER BY mr.visit_date DESC, mr.created_at DESC",
                new String[]{String.valueOf(doctorUserId)}
        );

        if (cursor.moveToFirst()) {
            do {
                HealthRecord record = new HealthRecord();
                record.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

                // Lấy patient_name từ join hoặc từ cột patient_name
                int patientNameIndex = cursor.getColumnIndex("patient_full_name");
                if (patientNameIndex != -1 && !cursor.isNull(patientNameIndex)) {
                    String patientName = cursor.getString(patientNameIndex);
                    record.setNotes("Patient: " + patientName); // Tạm lưu tên bệnh nhân vào notes
                } else {
                    int nameIndex = cursor.getColumnIndex("patient_name");
                    if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                        record.setNotes("Patient: " + cursor.getString(nameIndex));
                    }
                }

                record.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
                record.setDate(cursor.getString(cursor.getColumnIndexOrThrow("visit_date")));

                int complaintIndex = cursor.getColumnIndex("chief_complaint");
                if (complaintIndex != -1 && !cursor.isNull(complaintIndex)) {
                    record.setSymptoms(cursor.getString(complaintIndex));
                }

                int diagnosisIndex = cursor.getColumnIndex("diagnosis");
                if (diagnosisIndex != -1 && !cursor.isNull(diagnosisIndex)) {
                    record.setDiagnosis(cursor.getString(diagnosisIndex));
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
                String patientName = record.getNotes() != null ? record.getNotes().toLowerCase() : "";
                String diagnosis = record.getDiagnosis() != null ? record.getDiagnosis().toLowerCase() : "";
                String symptoms = record.getSymptoms() != null ? record.getSymptoms().toLowerCase() : "";

                if (patientName.contains(lowerQuery) ||
                        diagnosis.contains(lowerQuery) ||
                        symptoms.contains(lowerQuery)) {
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