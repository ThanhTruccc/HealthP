package com.example.healthprofile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.AppointmentAdapter;
import com.example.healthprofile.model.Appointment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.MODE_PRIVATE;

public class AppointmentListFragment extends Fragment implements AppointmentAdapter.OnAppointmentActionListener {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<Appointment> appointments;
    private LinearLayout tvEmpty;
    private ProgressBar progressBar;
    private String filterStatus;
    private SQLiteDatabase db;
    private String userEmail;

    private static final String DATABASE_NAME = "health_profile.db";
    private static final String TABLE_APPOINTMENTS = "appointments";
    private ExecutorService executorService;
    private Handler mainHandler;

    public static AppointmentListFragment newInstance(String status) {
        AppointmentListFragment fragment = new AppointmentListFragment();
        Bundle args = new Bundle();
        args.putString("status", status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            filterStatus = getArguments().getString("status");
        }

        // Lấy email user từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        // Mở database trực tiếp
        db = requireContext().openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);

        // Khởi tạo ExecutorService và Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_list, container, false);

        recyclerView = view.findViewById(R.id.rv_appointments);
        tvEmpty = view.findViewById(R.id.tv_empty);
        progressBar = view.findViewById(R.id.progress_bar_appointments);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        setupRecyclerView();
        loadAppointmentsFromDatabase();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appointments = new ArrayList<>();
        adapter = new AppointmentAdapter(getContext(), appointments, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Load appointments từ database theo user email
     */
    private void loadAppointmentsFromDatabase() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        executorService.execute(() -> {
            List<Appointment> result = getUserAppointmentsByStatus(userEmail, filterStatus);

            mainHandler.post(() -> {
                if (isAdded() && getContext() != null) {
                    updateAppointmentList(result);
                }
            });
        });
    }

    /**
     * Lấy appointments của user theo status
     */
    private List<Appointment> getUserAppointmentsByStatus(String userEmail, String status) {
        List<Appointment> appointmentList = new ArrayList<>();

        try {
            String sql;
            if (status.equals("upcoming")) {
                // Lấy cả pending và confirmed của user
                sql = "SELECT * FROM " + TABLE_APPOINTMENTS +
                        " WHERE user_email = '" + escapeString(userEmail) + "' " +
                        "AND status IN ('pending', 'confirmed') " +
                        "ORDER BY appointment_date DESC, appointment_time DESC";
            } else if (status.equals("all")) {
                // Lấy tất cả appointments của user
                sql = "SELECT * FROM " + TABLE_APPOINTMENTS +
                        " WHERE user_email = '" + escapeString(userEmail) + "' " +
                        "ORDER BY appointment_date DESC, appointment_time DESC";
            } else {
                // Lấy theo status cụ thể
                sql = "SELECT * FROM " + TABLE_APPOINTMENTS +
                        " WHERE user_email = '" + escapeString(userEmail) + "' " +
                        "AND status = '" + escapeString(status) + "' " +
                        "ORDER BY appointment_date DESC, appointment_time DESC";
            }

            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                do {
                    Appointment apt = new Appointment();
                    apt.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

                    // User email
                    int userEmailIndex = cursor.getColumnIndex("user_email");
                    if (userEmailIndex >= 0 && !cursor.isNull(userEmailIndex)) {
                        apt.setPatientEmail(cursor.getString(userEmailIndex));
                    }

                    // Doctor info
                    int doctorIdIndex = cursor.getColumnIndex("doctor_id");
                    if (doctorIdIndex >= 0 && !cursor.isNull(doctorIdIndex)) {
                        apt.setDoctorId(cursor.getInt(doctorIdIndex));
                    }

                    int doctorNameIndex = cursor.getColumnIndex("doctor_name");
                    if (doctorNameIndex >= 0 && !cursor.isNull(doctorNameIndex)) {
                        apt.setDoctorName(cursor.getString(doctorNameIndex));
                    }

                    // Patient info
                    int patientNameIndex = cursor.getColumnIndex("patient_name");
                    if (patientNameIndex >= 0 && !cursor.isNull(patientNameIndex)) {
                        apt.setPatientName(cursor.getString(patientNameIndex));
                    }

                    int phoneIndex = cursor.getColumnIndex("phone");
                    if (phoneIndex >= 0 && !cursor.isNull(phoneIndex)) {
                        apt.setPhone(cursor.getString(phoneIndex));
                    }

                    // Date and Time - FIXED: Sử dụng appointment_date và appointment_time
                    int dateIndex = cursor.getColumnIndex("appointment_date");
                    if (dateIndex >= 0 && !cursor.isNull(dateIndex)) {
                        apt.setAppointmentDate(cursor.getString(dateIndex));
                    } else {
                        // Fallback to old 'date' column if exists
                        int oldDateIndex = cursor.getColumnIndex("date");
                        if (oldDateIndex >= 0 && !cursor.isNull(oldDateIndex)) {
                            apt.setAppointmentDate(cursor.getString(oldDateIndex));
                        }
                    }

                    int timeIndex = cursor.getColumnIndex("appointment_time");
                    if (timeIndex >= 0 && !cursor.isNull(timeIndex)) {
                        apt.setAppointmentTime(cursor.getString(timeIndex));
                    } else {
                        // Fallback to old 'time' column if exists
                        int oldTimeIndex = cursor.getColumnIndex("time");
                        if (oldTimeIndex >= 0 && !cursor.isNull(oldTimeIndex)) {
                            apt.setAppointmentTime(cursor.getString(oldTimeIndex));
                        }
                    }

                    // Other info
                    int reasonIndex = cursor.getColumnIndex("reason");
                    if (reasonIndex >= 0 && !cursor.isNull(reasonIndex)) {
                        apt.setReason(cursor.getString(reasonIndex));
                    }

                    int statusIndex = cursor.getColumnIndex("status");
                    if (statusIndex >= 0 && !cursor.isNull(statusIndex)) {
                        apt.setStatus(cursor.getString(statusIndex));
                    }

                    int notesIndex = cursor.getColumnIndex("notes");
                    if (notesIndex >= 0 && !cursor.isNull(notesIndex)) {
                        apt.setNotes(cursor.getString(notesIndex));
                    }

                    int timestampIndex = cursor.getColumnIndex("timestamp");
                    if (timestampIndex >= 0 && !cursor.isNull(timestampIndex)) {
                        apt.setTimestamp(cursor.getLong(timestampIndex));
                    }

                    int feeIndex = cursor.getColumnIndex("fee");
                    if (feeIndex >= 0 && !cursor.isNull(feeIndex)) {
                        apt.setFee(cursor.getInt(feeIndex));
                    } else {
                        apt.setFee(200000); // Default fee
                    }

                    appointmentList.add(apt);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return appointmentList;
    }

    /**
     * Update danh sách appointments trên UI
     */
    private void updateAppointmentList(List<Appointment> result) {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        appointments.clear();
        appointments.addAll(result);
        adapter.notifyDataSetChanged();

        // Show/hide empty state
        if (appointments.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancelAppointment(Appointment appointment, int position) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Hủy lịch hẹn")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này không?")
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    // Update status trong database (background thread)
                    executorService.execute(() -> {
                        boolean success = updateAppointmentStatus(appointment.getId(), "cancelled");

                        // Update UI on main thread
                        mainHandler.post(() -> {
                            if (isAdded() && getContext() != null) {
                                handleCancelResult(success, position);
                            }
                        });
                    });
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    /**
     * Update appointment status
     */
    private boolean updateAppointmentStatus(int id, String status) {
        try {
            String sql = "UPDATE " + TABLE_APPOINTMENTS +
                    " SET status = '" + escapeString(status) + "' " +
                    "WHERE id = " + id;
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xử lý kết quả cancel appointment
     */
    private void handleCancelResult(boolean success, int position) {
        if (success) {
            // Remove from list if we're showing upcoming/pending appointments
            if (filterStatus.equals("upcoming") || filterStatus.equals("pending")) {
                adapter.removeAppointment(position);
                Toast.makeText(getContext(), "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show();

                // Show empty state if no more appointments
                if (appointments.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                // Just reload data for "all" or other filters
                loadAppointmentsFromDatabase();
                Toast.makeText(getContext(), "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Lỗi khi hủy lịch hẹn", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewDetail(Appointment appointment) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), AppointmentUserDetailActivity.class);
            intent.putExtra("appointment_id", appointment.getId());
            startActivity(intent);
        }
    }

    /**
     * Refresh data
     */
    public void refreshData() {
        loadAppointmentsFromDatabase();
    }

    /**
     * Escape string để tránh SQL injection
     */
    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment resumes
        loadAppointmentsFromDatabase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Shutdown executor
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        // Đóng database
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}