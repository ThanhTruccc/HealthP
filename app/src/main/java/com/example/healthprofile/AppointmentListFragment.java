package com.example.healthprofile;

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

    // SQLiteDatabase trực tiếp
    private SQLiteDatabase db;
    private String userEmail;

    // Database constants
    private static final String DATABASE_NAME = "health_profile.db";
    private static final String TABLE_APPOINTMENTS = "appointments";

    // ExecutorService thay cho AsyncTask
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
        // Show progress bar
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Execute in background thread
        executorService.execute(() -> {
            List<Appointment> result = getUserAppointmentsByStatus(userEmail, filterStatus);

            // Update UI on main thread
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
                        "ORDER BY date DESC, time DESC";
            } else if (status.equals("all")) {
                // Lấy tất cả appointments của user
                sql = "SELECT * FROM " + TABLE_APPOINTMENTS +
                        " WHERE user_email = '" + escapeString(userEmail) + "' " +
                        "ORDER BY date DESC, time DESC";
            } else {
                // Lấy theo status cụ thể
                sql = "SELECT * FROM " + TABLE_APPOINTMENTS +
                        " WHERE user_email = '" + escapeString(userEmail) + "' " +
                        "AND status = '" + escapeString(status) + "' " +
                        "ORDER BY date DESC, time DESC";
            }

            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                do {
                    Appointment apt = new Appointment();
                    apt.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

                    // Kiểm tra xem cột user_email có tồn tại không
                    int userEmailIndex = cursor.getColumnIndex("user_email");
                    if (userEmailIndex >= 0) {
                        apt.setUserEmail(cursor.getString(userEmailIndex));
                    }

                    apt.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
                    apt.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
                    apt.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                    apt.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                    apt.setTime(cursor.getString(cursor.getColumnIndexOrThrow("time")));
                    apt.setReason(cursor.getString(cursor.getColumnIndexOrThrow("reason")));
                    apt.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                    apt.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                    apt.setFee(cursor.getInt(cursor.getColumnIndexOrThrow("fee")));

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
            adapter.removeAppointment(position);
            Toast.makeText(getContext(), "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show();

            // Show empty state if no more appointments
            if (appointments.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(getContext(), "Lỗi khi hủy lịch hẹn", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewDetail(Appointment appointment) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Chi tiết lịch hẹn: " + appointment.getDoctorName(),
                    Toast.LENGTH_SHORT).show();
        }
        // TODO: Open detail activity
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