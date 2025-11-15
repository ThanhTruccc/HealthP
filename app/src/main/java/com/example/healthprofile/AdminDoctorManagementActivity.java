package com.example.healthprofile;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.AdminDoctorAdapter;
import com.example.healthprofile.model.Doctor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AdminDoctorManagementActivity extends AppCompatActivity
        implements AdminDoctorAdapter.OnDoctorActionListener {

    private static final String TAG = "AdminDoctorMgmt";
    private static final int PICK_IMAGE_REQUEST = 1001;

    private RecyclerView recyclerView;
    private AdminDoctorAdapter adapter;
    private List<Doctor> doctorList;
    private List<Doctor> filteredList;

    private EditText edtSearch;
    private LinearLayout tvEmpty;
    private ProgressBar progressBar;
    private TextView tvTotalDoctors;
    private ImageButton btnBack;
    private FloatingActionButton fabAddDoctor;

    private SQLiteDatabase db;

    private ImageView imgPreviewDoctor;
    private String selectedImagePath = null;
    private AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_doctor_management);

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        // Đảm bảo cột specialty và workplace tồn tại
        ensureColumns();

        initViews();
        setupRecyclerView();
        setupSearch();
        loadDoctors();
    }

    /**
     * Đảm bảo cột specialty và workplace tồn tại trong bảng doctors
     */
    private void ensureColumns() {
        try {
            // Kiểm tra xem các cột đã tồn tại chưa
            Cursor cursor = db.rawQuery("PRAGMA table_info(doctors)", null);
            boolean hasSpecialty = false;
            boolean hasWorkplace = false;

            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndexOrThrow("name");

                do {
                    String columnName = cursor.getString(nameIndex);
                    if ("specialty".equals(columnName)) {
                        hasSpecialty = true;
                    }
                    if ("workplace".equals(columnName)) {
                        hasWorkplace = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            // Nếu chưa có cột specialty, thêm vào
            if (!hasSpecialty) {
                Log.d(TAG, "Adding specialty column to doctors table");
                db.execSQL("ALTER TABLE doctors ADD COLUMN specialty TEXT");
            }

            // Nếu chưa có cột workplace, thêm vào
            if (!hasWorkplace) {
                Log.d(TAG, "Adding workplace column to doctors table");
                db.execSQL("ALTER TABLE doctors ADD COLUMN workplace TEXT");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring columns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_doctors_admin);
        edtSearch = findViewById(R.id.edt_search_doctor);
        tvEmpty = findViewById(R.id.tv_empty_doctors);
        progressBar = findViewById(R.id.progress_bar_doctors);
        tvTotalDoctors = findViewById(R.id.tv_total_doctors);
        btnBack = findViewById(R.id.btn_back_doctors);
        fabAddDoctor = findViewById(R.id.fab_add_doctor);

        btnBack.setOnClickListener(v -> finish());
        fabAddDoctor.setOnClickListener(v -> showAddDoctorDialog());
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        doctorList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AdminDoctorAdapter(this, filteredList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterDoctors(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(doctorList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Doctor doctor : doctorList) {
                String name = doctor.getName() != null ? doctor.getName().toLowerCase() : "";
                String degree = doctor.getDegree() != null ? doctor.getDegree().toLowerCase() : "";
                String specialty = doctor.getSpecialty() != null ? doctor.getSpecialty().toLowerCase() : "";
                String workplace = doctor.getWorkplace() != null ? doctor.getWorkplace().toLowerCase() : "";

                if (name.contains(lowerQuery) || degree.contains(lowerQuery) ||
                        specialty.contains(lowerQuery) || workplace.contains(lowerQuery)) {
                    filteredList.add(doctor);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void loadDoctors() {
        new LoadDoctorsTask().execute();
    }

    private class LoadDoctorsTask extends AsyncTask<Void, Void, List<Doctor>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Doctor> doInBackground(Void... voids) {
            List<Doctor> doctors = new ArrayList<>();

            try {
                String sql = "SELECT * FROM doctors ORDER BY rating DESC";
                Cursor cursor = db.rawQuery(sql, null);

                Log.d(TAG, "Total doctors in database: " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    do {
                        int currentId = -1;
                        try {
                            currentId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

                            Doctor doctor = new Doctor();
                            doctor.setId(currentId);

                            // Name
                            int nameIndex = cursor.getColumnIndex("name");
                            doctor.setName(nameIndex >= 0 ? cursor.getString(nameIndex) : "Tên không rõ");

                            // Degree
                            int degreeIndex = cursor.getColumnIndex("degree");
                            doctor.setDegree(degreeIndex >= 0 ? cursor.getString(degreeIndex) : "Không rõ");

                            // Specialty
                            int specialtyIndex = cursor.getColumnIndex("specialty");
                            if (specialtyIndex >= 0) {
                                doctor.setSpecialty(cursor.getString(specialtyIndex));
                            } else {
                                doctor.setSpecialty("Chưa cập nhật");
                            }

                            // Workplace
                            int workplaceIndex = cursor.getColumnIndex("workplace");
                            if (workplaceIndex >= 0) {
                                doctor.setWorkplace(cursor.getString(workplaceIndex));
                            } else {
                                doctor.setWorkplace("Chưa cập nhật");
                            }

                            // Rating
                            int ratingIndex = cursor.getColumnIndex("rating");
                            doctor.setRating(ratingIndex >= 0 ? cursor.getFloat(ratingIndex) : 0.0f);

                            // Experience
                            int experienceIndex = cursor.getColumnIndex("experience");
                            doctor.setExperience(experienceIndex >= 0 ? cursor.getInt(experienceIndex) : 0);

                            // Image Resource
                            int imageResourceIndex = cursor.getColumnIndex("image_resource");
                            doctor.setImageResource(imageResourceIndex >= 0 ? cursor.getInt(imageResourceIndex) : R.drawable.doctor_1);

                            // Image Path
                            int imagePathIndex = cursor.getColumnIndex("image_path");
                            if (imagePathIndex >= 0 && !cursor.isNull(imagePathIndex)) {
                                String imagePath = cursor.getString(imagePathIndex);
                                if (imagePath != null && !imagePath.isEmpty()) {
                                    doctor.setImagePath(imagePath);
                                }
                            }

                            doctors.add(doctor);
                            Log.d(TAG, "Loaded doctor: " + doctor.getName());
                        } catch (Exception e) {
                            Log.e(TAG, "LỖI PARSE (Bác sĩ ID: " + currentId + "): " + e.getMessage());
                            e.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (Exception e) {
                Log.e(TAG, "Error loading doctors: " + e.getMessage());
                e.printStackTrace();
            }

            return doctors;
        }

        @Override
        protected void onPostExecute(List<Doctor> doctors) {
            progressBar.setVisibility(View.GONE);

            doctorList.clear();
            doctorList.addAll(doctors);

            filteredList.clear();
            filteredList.addAll(doctors);

            adapter.notifyDataSetChanged();

            tvTotalDoctors.setText("Tổng: " + doctorList.size() + " bác sĩ");
            updateEmptyState();

            Log.d(TAG, "Loaded " + doctors.size() + " doctors successfully");
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

    @Override
    public void onEditDoctor(Doctor doctor, int position) {
        showEditDoctorDialog(doctor, position);
    }

    @Override
    public void onDeleteDoctor(Doctor doctor, int position) {
        showDeleteConfirmDialog(doctor, position);
    }

    @Override
    public void onViewDoctorDetail(Doctor doctor) {
        showDoctorDetailDialog(doctor);
    }

    private void showAddDoctorDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor_with_image, null);

        EditText edtName = dialogView.findViewById(R.id.edt_doctor_name);
        Spinner spinnerDegree = dialogView.findViewById(R.id.spinner_degree);
        Spinner spinnerSpecialty = dialogView.findViewById(R.id.spinner_specialty);
        EditText edtWorkplace = dialogView.findViewById(R.id.edt_workplace);
        EditText edtExperience = dialogView.findViewById(R.id.edt_experience);
        EditText edtRating = dialogView.findViewById(R.id.edt_rating);
        imgPreviewDoctor = dialogView.findViewById(R.id.img_preview_doctor);
        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);
        Button btnUseDefault = dialogView.findViewById(R.id.btn_use_default);

        selectedImagePath = null;

        // Setup spinner học vị
        String[] degrees = {"BS.", "ThS.BS.", "TS.BS.", "PGS.TS.", "GS.TS."};
        ArrayAdapter<String> degreeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, degrees);
        degreeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDegree.setAdapter(degreeAdapter);

        // Setup spinner chuyên khoa
        String[] specialties = {"Nội khoa", "Ngoại khoa", "Nhi khoa", "Sản khoa",
                "Da liễu", "Tim mạch", "Tiêu hóa", "Thần kinh", "Tai mũi họng"};
        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, specialties);
        specialtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(specialtyAdapter);

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnUseDefault.setOnClickListener(v -> {
            selectedImagePath = null;
            imgPreviewDoctor.setImageResource(R.drawable.doctor_1);
            Toast.makeText(this, "Sẽ sử dụng ảnh mặc định", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm bác sĩ mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        currentDialog = dialog;

        dialog.setOnShowListener(dialogInterface -> {
            Button btnAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnAdd.setOnClickListener(v -> {
                String name = edtName.getText().toString().trim();
                String degree = spinnerDegree.getSelectedItem().toString();
                String specialty = spinnerSpecialty.getSelectedItem().toString();
                String workplace = edtWorkplace.getText().toString().trim();
                String experienceStr = edtExperience.getText().toString().trim();
                String ratingStr = edtRating.getText().toString().trim();

                if (name.isEmpty() || workplace.isEmpty() || experienceStr.isEmpty() || ratingStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int experience = Integer.parseInt(experienceStr);
                    float rating = Float.parseFloat(ratingStr);

                    if (rating < 0 || rating > 5) {
                        Toast.makeText(this, "Đánh giá phải từ 0 đến 5!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Doctor newDoctor = new Doctor();
                    newDoctor.setName(name);
                    newDoctor.setDegree(degree);
                    newDoctor.setSpecialty(specialty);
                    newDoctor.setWorkplace(workplace);
                    newDoctor.setExperience(experience);
                    newDoctor.setRating(rating);
                    newDoctor.setImageResource(R.drawable.doctor_1);
                    newDoctor.setImagePath(selectedImagePath);

                    new AddDoctorTask(newDoctor, dialog).execute();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Số năm kinh nghiệm hoặc đánh giá không hợp lệ!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private class AddDoctorTask extends AsyncTask<Void, Void, Long> {
        private Doctor doctor;
        private AlertDialog dialog;

        AddDoctorTask(Doctor doctor, AlertDialog dialog) {
            this.doctor = doctor;
            this.dialog = dialog;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            int currentId = -1;
            try {
                String imagePath = doctor.getImagePath() != null ?
                        "'" + escapeString(doctor.getImagePath()) + "'" : "NULL";

                String sql = "INSERT INTO doctors (name, degree, specialty, workplace, rating, experience, image_resource, image_path) " +
                        "VALUES ('" + escapeString(doctor.getName()) + "', " +
                        "'" + escapeString(doctor.getDegree()) + "', " +
                        "'" + escapeString(doctor.getSpecialty()) + "', " +
                        "'" + escapeString(doctor.getWorkplace()) + "', " +
                        doctor.getRating() + ", " +
                        doctor.getExperience() + ", " +
                        doctor.getImageResource() + ", " +
                        imagePath + ")";

                Log.d(TAG, "Insert SQL: " + sql);
                db.execSQL(sql);

                Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
                long id = -1;
                if (cursor.moveToFirst()) {
                    id = cursor.getLong(0);
                }
                cursor.close();

                Log.d(TAG, "Doctor inserted with ID: " + id);
                return id;
            } catch (Exception e) {
                Log.e(TAG, "LỖI INSERT (Bác sĩ ID: " + currentId + "): " + e.getMessage());
                e.printStackTrace();
                return -1L;
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result > 0) {
                Toast.makeText(AdminDoctorManagementActivity.this,
                        "Thêm bác sĩ thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadDoctors();
            } else {
                Toast.makeText(AdminDoctorManagementActivity.this,
                        "Lỗi khi thêm bác sĩ!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEditDoctorDialog(Doctor doctor, int position) {
        Toast.makeText(this, "Chức năng edit với upload ảnh - tương tự như add", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmDialog(Doctor doctor, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bác sĩ:\n" +
                        doctor.getDegree() + " " + doctor.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new DeleteDoctorTask(doctor, position).execute();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private class DeleteDoctorTask extends AsyncTask<Void, Void, Boolean> {
        private Doctor doctor;
        private int position;

        DeleteDoctorTask(Doctor doctor, int position) {
            this.doctor = doctor;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (doctor.getImagePath() != null && !doctor.getImagePath().isEmpty()) {
                    File imageFile = new File(doctor.getImagePath());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }

                String sql = "DELETE FROM doctors WHERE id = " + doctor.getId();
                db.execSQL(sql);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error deleting doctor: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AdminDoctorManagementActivity.this,
                        "Đã xóa bác sĩ!", Toast.LENGTH_SHORT).show();
                loadDoctors();
            } else {
                Toast.makeText(AdminDoctorManagementActivity.this,
                        "Lỗi khi xóa!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDoctorDetailDialog(Doctor doctor) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_doctor_detail, null);

        ImageView imgDoctor = dialogView.findViewById(R.id.img_doctor_detail);
        TextView tvName = dialogView.findViewById(R.id.tv_doctor_name_detail);
        TextView tvDegree = dialogView.findViewById(R.id.tv_doctor_degree_detail);
        TextView tvSpecialty = dialogView.findViewById(R.id.tv_doctor_specialty_detail);
        TextView tvWorkplace = dialogView.findViewById(R.id.tv_doctor_workplace_detail);
        TextView tvExperience = dialogView.findViewById(R.id.tv_doctor_experience_detail);
        TextView tvRating = dialogView.findViewById(R.id.tv_doctor_rating_detail);

        // Hiển thị ảnh
        if (doctor.getImagePath() != null && !doctor.getImagePath().isEmpty()) {
            File imageFile = new File(doctor.getImagePath());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(doctor.getImagePath());
                imgDoctor.setImageBitmap(bitmap);
            } else {
                imgDoctor.setImageResource(doctor.getImageResource());
            }
        } else {
            imgDoctor.setImageResource(doctor.getImageResource());
        }

        // Hiển thị thông tin
        tvName.setText(doctor.getName());
        tvDegree.setText("Học vị: " + doctor.getDegree());
        tvSpecialty.setText("Chuyên khoa: " + (doctor.getSpecialty() != null ? doctor.getSpecialty() : "Chưa cập nhật"));
        tvWorkplace.setText("Nơi làm việc: " + (doctor.getWorkplace() != null ? doctor.getWorkplace() : "Chưa cập nhật"));
        tvExperience.setText("Kinh nghiệm: " + doctor.getExperience() + " năm");
        tvRating.setText("Đánh giá: " + String.format("%.1f", doctor.getRating()) + "/5.0 ⭐");

        new AlertDialog.Builder(this)
                .setTitle("Thông tin bác sĩ")
                .setView(dialogView)
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                try {
                    String savedPath = saveImageToInternalStorage(imageUri);

                    if (savedPath != null) {
                        selectedImagePath = savedPath;

                        if (imgPreviewDoctor != null) {
                            Bitmap bitmap = BitmapFactory.decodeFile(savedPath);
                            imgPreviewDoctor.setImageBitmap(bitmap);
                        }

                        Toast.makeText(this, "Đã chọn ảnh thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi khi lưu ảnh!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error selecting image: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            bitmap = resizeBitmap(bitmap, 800, 800);

            String filename = "doctor_" + System.currentTimeMillis() + ".jpg";
            File directory = getFilesDir();
            File imageFile = new File(directory, filename);

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
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