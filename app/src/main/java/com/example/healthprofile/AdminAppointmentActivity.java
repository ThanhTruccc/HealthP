package com.example.healthprofile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.healthprofile.adapter.AdminAppointmentAdapter;

import com.example.healthprofile.model.Appointment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminAppointmentActivity extends AppCompatActivity implements AdminAppointmentAdapter.OnAdminAppointmentActionListener {
    private static final String DATABASE_NAME = "health_profile.db";
    private static final String TABLE_APPOINTMENTS = "appointments";
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private AdminAppointmentAdapter adapter;
    private List<Appointment> appointments;
    private LinearLayout tvEmpty;
    private ProgressBar progressBar;
    private TextView tvTotalAppointments;
    private ImageButton btnBack;
    private SQLiteDatabase database;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointment);

        openDatabase();

        initViews();
        setupTabLayout();
        setupRecyclerView();
        loadAppointments("all");
    }
    private void openDatabase() {
        try {
            database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            createAppointmentsTable(database); // Đảm bảo bảng tồn tại
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể mở database", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    private void createAppointmentsTable(SQLiteDatabase db) {
        String createAppointmentsTable =
                "CREATE TABLE IF NOT EXISTS " + TABLE_APPOINTMENTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_email TEXT, " +
                        "doctor_name TEXT NOT NULL, " +
                        "patient_name TEXT NOT NULL, " +
                        "phone TEXT NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "time TEXT NOT NULL, " +
                        "reason TEXT, " +
                        "status TEXT DEFAULT 'pending', " +
                        "timestamp INTEGER, " +
                        "fee INTEGER DEFAULT 200000" +
                        ")";
        db.execSQL(createAppointmentsTable);
    }
    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout_admin);
        recyclerView = findViewById(R.id.rv_admin_appointments);
        tvEmpty = findViewById(R.id.tv_empty_admin);
        progressBar = findViewById(R.id.progress_bar_admin);
        tvTotalAppointments = findViewById(R.id.tv_total_appointments);
        btnBack = findViewById(R.id.btn_back_admin);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Chờ xác nhận"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã xác nhận"));
        tabLayout.addTab(tabLayout.newTab().setText("Hoàn thành"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã hủy"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadAppointments("all");
                        break;
                    case 1:
                        loadAppointments("pending");
                        break;
                    case 2:
                        loadAppointments("confirmed");
                        break;
                    case 3:
                        loadAppointments("completed");
                        break;
                    case 4:
                        loadAppointments("cancelled");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointments = new ArrayList<>();
        adapter = new AdminAppointmentAdapter(this, appointments, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadAppointments(String filter) {
        currentFilter = filter;
        new LoadAppointmentsTask(filter).execute();
    }
    @Override
    public void onConfirmAppointment(Appointment appointment, int position) {
        new UpdateStatusTask(appointment.getId(), "confirmed", position).execute();
    }

    @Override
    public void onCompleteAppointment(Appointment appointment, int position) {
        new UpdateStatusTask(appointment.getId(), "completed", position).execute();
    }

    @Override
    public void onCancelAppointment(Appointment appointment, int position) {
        showCancelConfirmDialog(appointment, position);
    }

    @Override
    public void onDeleteAppointment(Appointment appointment, int position) {
        showDeleteConfirmDialog(appointment, position);
    }

    @Override
    public void onViewDetail(Appointment appointment) {
        showDetailDialog(appointment);
    }

    /**
     * Hiển thị dialog xác nhận hủy
     */
    private void showCancelConfirmDialog(Appointment appointment, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy lịch")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn của " + appointment.getPatientName() + "?")
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    new UpdateStatusTask(appointment.getId(), "cancelled", position).execute();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    /**
     * Hiển thị dialog xác nhận xóa
     */
    private void showDeleteConfirmDialog(Appointment appointment, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn XÓA VĨNH VIỄN lịch hẹn này?\n\nHành động này không thể hoàn tác!")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new DeleteAppointmentTask(appointment.getId(), position).execute();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Hiển thị chi tiết appointment
     */
    private void showDetailDialog(Appointment appointment) {
        String details = "THÔNG TIN CHI TIẾT\n\n" +
                "ID: #" + appointment.getId() + "\n" +
                "Bệnh nhân: " + appointment.getPatientName() + "\n" +
                "Số điện thoại: " + appointment.getPhone() + "\n" +
                "Bác sĩ: " + appointment.getDoctorName() + "\n" +
                "Ngày khám: " + appointment.getDate() + "\n" +
                "Giờ khám: " + appointment.getTime() + "\n" +
                "Lý do: " + appointment.getReason() + "\n" +
                "Trạng thái: " + getVietnameseStatus(appointment.getStatus());

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết lịch hẹn")
                .setMessage(details)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private String getVietnameseStatus(String status) {
        switch (status) {
            case "pending":
                return "Chờ xác nhận";
            case "confirmed":
                return "Đã xác nhận";
            case "completed":
                return "Hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return "Không rõ";
        }
    }

    /**
     * Helper method: Escape string để tránh SQL injection
     */
    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    /**
     * Giả định class Appointment có các setter tương ứng
     */
    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment apt = new Appointment();
        apt.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

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

        return apt;
    }
    /**
     * AsyncTask để load tất cả appointments
     */
    private class LoadAppointmentsTask extends AsyncTask<Void, Void, List<Appointment>> {
        private String filter;

        LoadAppointmentsTask(String filter) {
            this.filter = filter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Appointment> doInBackground(Void... voids) {
            List<Appointment> results = new ArrayList<>();
            Cursor cursor = null;
            String sql;

            if (database == null || !database.isOpen()) {
                openDatabase();
            }

            try {
                if (filter.equals("all")) {
                    // Lấy tất cả appointments
                    sql = "SELECT * FROM " + TABLE_APPOINTMENTS + " ORDER BY id DESC";
                } else {
                    // Lấy appointments theo status
                    sql = "SELECT * FROM " + TABLE_APPOINTMENTS +
                            " WHERE status = '" + escapeString(filter) + "' " +
                            "ORDER BY date DESC, time DESC";
                }

                cursor = database.rawQuery(sql, null);

                if (cursor.moveToFirst()) {
                    do {
                        results.add(cursorToAppointment(cursor));
                    } while (cursor.moveToNext());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<Appointment> result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);

            appointments.clear();
            appointments.addAll(result);
            adapter.notifyDataSetChanged();

            // Update total count
            tvTotalAppointments.setText("Tổng: " + appointments.size() + " lịch hẹn");

            // Show/hide empty state
            if (appointments.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * AsyncTask để cập nhật status appointment TRỰC TIẾP
     */
    private class UpdateStatusTask extends AsyncTask<Void, Void, Boolean> {
        private int appointmentId;
        private String newStatus;
        private int position;

        UpdateStatusTask(int appointmentId, String newStatus, int position) {
            this.appointmentId = appointmentId;
            this.newStatus = newStatus;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (database == null || !database.isOpen()) {
                openDatabase();
            }
            try {
                // Câu lệnh SQL UPDATE TRỰC TIẾP
                String sql = "UPDATE " + TABLE_APPOINTMENTS +
                        " SET status = '" + escapeString(newStatus) + "' " +
                        "WHERE id = " + appointmentId;
                database.execSQL(sql);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                Toast.makeText(AdminAppointmentActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();

                // Reload data to reflect change
                loadAppointments(currentFilter);

            } else {
                Toast.makeText(AdminAppointmentActivity.this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * AsyncTask để xóa appointment TRỰC TIẾP
     */
    private class DeleteAppointmentTask extends AsyncTask<Void, Void, Boolean> {
        private int appointmentId;
        private int position;

        DeleteAppointmentTask(int appointmentId, int position) {
            this.appointmentId = appointmentId;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (database == null || !database.isOpen()) {
                openDatabase();
            }
            try {
                // Câu lệnh SQL DELETE TRỰC TIẾP
                String sql = "DELETE FROM " + TABLE_APPOINTMENTS + " WHERE id = " + appointmentId;
                database.execSQL(sql);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                Toast.makeText(AdminAppointmentActivity.this, "Đã xóa lịch hẹn", Toast.LENGTH_SHORT).show();

                // Reload data
                loadAppointments(currentFilter);
            } else {
                Toast.makeText(AdminAppointmentActivity.this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đóng database
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}