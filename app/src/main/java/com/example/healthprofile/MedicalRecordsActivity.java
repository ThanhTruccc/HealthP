package com.example.healthprofile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.DoctorMedicalRecordAdapter;
import com.example.healthprofile.model.HealthRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordsActivity extends AppCompatActivity {

    private SearchView searchView;
    private Spinner spinnerPatients;
    private RecyclerView rvMedicalRecords;
    private LinearLayout emptyState;
    private ImageView btnBack;
    private SQLiteDatabase db;
    private int doctorUserId;
    private int doctorId;
    private List<HealthRecord> recordList;
    private List<HealthRecord> filteredList;
    private DoctorMedicalRecordAdapter adapter;

    private Map<String, String> patientMap;
    private List<String> patientNames;
    private List<String> patientEmails;
    private String selectedPatientEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_records);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        doctorUserId = prefs.getInt("user_id", 0);

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        getDoctorId();
        initViews();
        loadPatientList();
        loadMedicalRecords();
    }

    private void getDoctorId() {
        Cursor cursor = db.rawQuery(
                "SELECT doctor_id FROM user_doctors WHERE user_id = ?",
                new String[]{String.valueOf(doctorUserId)}
        );

        if (cursor.moveToFirst()) {
            doctorId = cursor.getInt(0);
        } else {
            cursor.close();
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String doctorName = prefs.getString("fullName", "");

            cursor = db.rawQuery(
                    "SELECT id FROM doctors WHERE name LIKE ?",
                    new String[]{"%" + doctorName.replace("BS. ", "") + "%"}
            );

            if (cursor.moveToFirst()) {
                doctorId = cursor.getInt(0);
            } else {
                doctorId = doctorUserId;
            }
        }
        cursor.close();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        searchView = findViewById(R.id.search_view);
        spinnerPatients = findViewById(R.id.spinner_patients);
        rvMedicalRecords = findViewById(R.id.rv_medical_records);
        emptyState = findViewById(R.id.empty_state);

        btnBack.setOnClickListener(v -> finish());

        rvMedicalRecords.setLayoutManager(new LinearLayoutManager(this));
        recordList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new DoctorMedicalRecordAdapter(this, filteredList, record -> {
            Intent intent = new Intent(MedicalRecordsActivity.this, MedicalRecordDetailActivity.class);
            intent.putExtra("record_id", record.getId());
            startActivity(intent);
        });
        rvMedicalRecords.setAdapter(adapter);

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

    private void loadPatientList() {
        patientMap = new HashMap<>();
        patientNames = new ArrayList<>();
        patientEmails = new ArrayList<>();

        patientNames.add("Tất cả bệnh nhân");
        patientEmails.add(null);

        // Query tối ưu: chỉ lấy bệnh nhân có cả bệnh án VÀ đã đặt lịch
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT mr.patient_email, u.fullName " +
                        "FROM medical_records mr " +
                        "INNER JOIN user u ON mr.patient_email = u.email " +
                        "INNER JOIN appointments a ON a.user_email = mr.patient_email AND a.doctor_id = ? " +
                        "WHERE mr.doctor_user_id = ? " +
                        "ORDER BY u.fullName ASC",
                new String[]{String.valueOf(doctorId), String.valueOf(doctorUserId)}
        );

        if (cursor.moveToFirst()) {
            do {
                String email = cursor.getString(0);
                String name = cursor.getString(1);

                if (email != null) {
                    String displayName = name != null ? name : email;
                    patientMap.put(email, displayName);
                    patientNames.add(displayName);
                    patientEmails.add(email);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Setup spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                patientNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatients.setAdapter(spinnerAdapter);

        spinnerPatients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPatientEmail = patientEmails.get(position);
                loadMedicalRecords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPatientEmail = null;
            }
        });
    }

    private void loadMedicalRecords() {
        recordList.clear();
        filteredList.clear();

        String query;
        String[] selectionArgs;

        if (selectedPatientEmail != null) {
            // Lọc theo bệnh nhân cụ thể (đã chắc chắn có appointment)
            query = "SELECT mr.*, u.fullName as patient_full_name " +
                    "FROM medical_records mr " +
                    "INNER JOIN user u ON mr.patient_email = u.email " +
                    "WHERE mr.doctor_user_id = ? AND mr.patient_email = ? " +
                    "ORDER BY mr.visit_date DESC, mr.created_at DESC";
            selectionArgs = new String[]{
                    String.valueOf(doctorUserId),
                    selectedPatientEmail
            };
        } else {
            // Tất cả bệnh nhân (chỉ những người đã đặt lịch)
            query = "SELECT mr.*, u.fullName as patient_full_name " +
                    "FROM medical_records mr " +
                    "INNER JOIN user u ON mr.patient_email = u.email " +
                    "INNER JOIN appointments a ON a.user_email = mr.patient_email AND a.doctor_id = ? " +
                    "WHERE mr.doctor_user_id = ? " +
                    "ORDER BY mr.visit_date DESC, mr.created_at DESC";
            selectionArgs = new String[]{
                    String.valueOf(doctorId),
                    String.valueOf(doctorUserId)
            };
        }

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                HealthRecord record = new HealthRecord();
                record.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

                // Lấy tên bệnh nhân
                int patientNameIndex = cursor.getColumnIndex("patient_full_name");
                if (patientNameIndex != -1 && !cursor.isNull(patientNameIndex)) {
                    String patientName = cursor.getString(patientNameIndex);
                    record.setNotes("Patient: " + patientName);
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
                String date = record.getDate() != null ? record.getDate().toLowerCase() : "";

                if (patientName.contains(lowerQuery) ||
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