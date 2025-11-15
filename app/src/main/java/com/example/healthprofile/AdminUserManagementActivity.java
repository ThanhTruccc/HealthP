package com.example.healthprofile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.AdminUserAdapter;
import com.example.healthprofile.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity quản lý người dùng - Sử dụng SQLiteDatabase trực tiếp
 */
public class AdminUserManagementActivity extends AppCompatActivity
        implements AdminUserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> userList;
    private List<User> filteredList;

    private EditText edtSearch;
    private LinearLayout tvEmpty;
    private ProgressBar progressBar;
    private TextView tvTotalUsers;
    private ImageButton btnBack;
    private FloatingActionButton fabAddUser;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        // Mở database
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
        createUsersTable();

        initViews();
        setupRecyclerView();
        setupSearch();
        loadUsers();
    }

    /**
     * Tạo bảng users nếu chưa có
     */
    private void createUsersTable() {
        String createTable = "CREATE TABLE IF NOT EXISTS user (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "password TEXT NOT NULL, " +
                "fullName TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT, " +
                "role TEXT DEFAULT 'user'" +
                ")";
        db.execSQL(createTable);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_users);
        edtSearch = findViewById(R.id.edt_search_user);
        tvEmpty = findViewById(R.id.tv_empty_users);
        progressBar = findViewById(R.id.progress_bar_users);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        btnBack = findViewById(R.id.btn_back_users);
        fabAddUser = findViewById(R.id.fab_add_user);

        btnBack.setOnClickListener(v -> finish());
        fabAddUser.setOnClickListener(v -> showAddUserDialog());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AdminUserAdapter(this, filteredList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : userList) {
                if (user.getFullName().toLowerCase().contains(lowerQuery) ||
                        user.getEmail().toLowerCase().contains(lowerQuery) ||
                        (user.getPhone() != null && user.getPhone().toLowerCase().contains(lowerQuery))) {
                    filteredList.add(user);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void loadUsers() {
        new LoadUsersTask().execute();
    }

    private class LoadUsersTask extends AsyncTask<Void, Void, List<User>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            List<User> users = new ArrayList<>();

            try {
                String sql = "SELECT * FROM user ORDER BY id DESC";
                Cursor cursor = db.rawQuery(sql, null);

                if (cursor.moveToFirst()) {
                    do {
                        User user = new User(
                                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("password")),
                                cursor.getString(cursor.getColumnIndexOrThrow("fullName")),
                                cursor.getString(cursor.getColumnIndexOrThrow("email")),
                                cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                                cursor.getString(cursor.getColumnIndexOrThrow("role"))
                        );
                        users.add(user);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return users;
        }

        @Override
        protected void onPostExecute(List<User> users) {
            progressBar.setVisibility(View.GONE);

            userList.clear();
            userList.addAll(users);

            filteredList.clear();
            filteredList.addAll(users);

            adapter.notifyDataSetChanged();

            tvTotalUsers.setText("Tổng: " + userList.size() + " người dùng");
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // ============ User Actions ============

    @Override
    public void onEditUser(User user, int position) {
        showEditUserDialog(user, position);
    }

    @Override
    public void onDeleteUser(User user, int position) {
        showDeleteConfirmDialog(user, position);
    }

    @Override
    public void onViewUserDetail(User user) {
        showUserDetailDialog(user);
    }

    // ============ Dialogs ============

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);

        EditText edtFullName = dialogView.findViewById(R.id.edt_fullname);
        EditText edtEmail = dialogView.findViewById(R.id.edt_email);
        EditText edtPhone = dialogView.findViewById(R.id.edt_phone);
        EditText edtPassword = dialogView.findViewById(R.id.edt_password);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinner_role);

        // Setup spinner
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm người dùng mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnAdd.setOnClickListener(v -> {
                String fullName = edtFullName.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String role = spinnerRole.getSelectedItem().toString();

                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }

                User newUser = new User(password, fullName, email, phone, role);
                new AddUserTask(newUser, dialog).execute();
            });
        });

        dialog.show();
    }

    private class AddUserTask extends AsyncTask<Void, Void, Long> {
        private User user;
        private AlertDialog dialog;

        AddUserTask(User user, AlertDialog dialog) {
            this.user = user;
            this.dialog = dialog;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                // Kiểm tra email đã tồn tại chưa
                String checkSql = "SELECT COUNT(*) FROM user WHERE email = '" +
                        escapeString(user.getEmail()) + "'";
                Cursor cursor = db.rawQuery(checkSql, null);

                int count = 0;
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
                cursor.close();

                if (count > 0) {
                    return -1L; // Email đã tồn tại
                }

                // Insert user mới
                String sql = "INSERT INTO user (password, fullName, email, phone, role) " +
                        "VALUES ('" + escapeString(user.getPassword()) + "', " +
                        "'" + escapeString(user.getFullName()) + "', " +
                        "'" + escapeString(user.getEmail()) + "', " +
                        "'" + escapeString(user.getPhone()) + "', " +
                        "'" + escapeString(user.getRole()) + "')";

                db.execSQL(sql);

                Cursor idCursor = db.rawQuery("SELECT last_insert_rowid()", null);
                long id = -1;
                if (idCursor.moveToFirst()) {
                    id = idCursor.getLong(0);
                }
                idCursor.close();
                return id;
            } catch (Exception e) {
                e.printStackTrace();
                return -2L;
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result > 0) {
                Toast.makeText(AdminUserManagementActivity.this,
                        "Thêm người dùng thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadUsers();
            } else if (result == -1) {
                Toast.makeText(AdminUserManagementActivity.this,
                        "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AdminUserManagementActivity.this,
                        "Lỗi khi thêm người dùng!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEditUserDialog(User user, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);

        EditText edtFullName = dialogView.findViewById(R.id.edt_fullname_edit);
        EditText edtPhone = dialogView.findViewById(R.id.edt_phone_edit);
        EditText edtPassword = dialogView.findViewById(R.id.edt_password_edit);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinner_role_edit);
        TextView tvEmail = dialogView.findViewById(R.id.tv_email_edit);

        // Hiển thị thông tin hiện tại
        edtFullName.setText(user.getFullName());
        edtPhone.setText(user.getPhone());
        tvEmail.setText("Email: " + user.getEmail());

        // Setup spinner
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setSelection(user.getRole().equals("admin") ? 1 : 0);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa người dùng")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String fullName = edtFullName.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String role = spinnerRole.getSelectedItem().toString();

                if (fullName.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập họ tên!", Toast.LENGTH_SHORT).show();
                    return;
                }

                user.setFullName(fullName);
                user.setPhone(phone);
                if (!password.isEmpty()) {
                    user.setPassword(password);
                }
                user.setRole(role);

                new UpdateUserTask(user, position, dialog).execute();
            });
        });

        dialog.show();
    }

    private class UpdateUserTask extends AsyncTask<Void, Void, Boolean> {
        private User user;
        private int position;
        private AlertDialog dialog;

        UpdateUserTask(User user, int position, AlertDialog dialog) {
            this.user = user;
            this.position = position;
            this.dialog = dialog;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String sql = "UPDATE user SET " +
                        "password = '" + escapeString(user.getPassword()) + "', " +
                        "fullName = '" + escapeString(user.getFullName()) + "', " +
                        "phone = '" + escapeString(user.getPhone()) + "', " +
                        "role = '" + escapeString(user.getRole()) + "' " +
                        "WHERE email = '" + escapeString(user.getEmail()) + "'";
                db.execSQL(sql);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AdminUserManagementActivity.this,
                        "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadUsers();
            } else {
                Toast.makeText(AdminUserManagementActivity.this,
                        "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmDialog(User user, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng:\n" +
                        user.getFullName() + " (" + user.getEmail() + ")?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new DeleteUserTask(user.getEmail(), position).execute();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private class DeleteUserTask extends AsyncTask<Void, Void, Boolean> {
        private String email;
        private int position;

        DeleteUserTask(String email, int position) {
            this.email = email;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String sql = "DELETE FROM user WHERE email = '" + escapeString(email) + "'";
                db.execSQL(sql);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AdminUserManagementActivity.this,
                        "Đã xóa người dùng!", Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(AdminUserManagementActivity.this,
                        "Lỗi khi xóa!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUserDetailDialog(User user) {
        String details = "THÔNG TIN CHI TIẾT\n\n" +
                "ID: #" + user.getId() + "\n" +
                "Họ tên: " + user.getFullName() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "Số điện thoại: " + (user.getPhone() != null ? user.getPhone() : "Chưa cập nhật") + "\n" +
                "Vai trò: " + (user.isAdmin() ? "Quản trị viên" : "Người dùng");

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết người dùng")
                .setMessage(details)
                .setPositiveButton("Đóng", null)
                .show();
    }

    /**
     * Escape string để tránh SQL injection
     */
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