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
    private Doctor editingDoctor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_doctor_management);

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
        ensureColumns();

        initViews();
        setupRecyclerView();
        setupSearch();
        loadDoctors();
    }

    private void ensureColumns() {
        try {
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

            if (!hasSpecialty) {
                Log.d(TAG, "Adding specialty column");
                db.execSQL("ALTER TABLE doctors ADD COLUMN specialty TEXT");
            }

            if (!hasWorkplace) {
                Log.d(TAG, "Adding workplace column");
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

                Log.d(TAG, "Total doctors: " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    do {
                        try {
                            Doctor doctor = new Doctor();
                            doctor.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

                            int nameIndex = cursor.getColumnIndex("name");
                            doctor.setName(nameIndex >= 0 ? cursor.getString(nameIndex) : "Không rõ");

                            int degreeIndex = cursor.getColumnIndex("degree");
                            doctor.setDegree(degreeIndex >= 0 ? cursor.getString(degreeIndex) : "Không rõ");

                            int specialtyIndex = cursor.getColumnIndex("specialty");
                            doctor.setSpecialty(specialtyIndex >= 0 ? cursor.getString(specialtyIndex) : "Chưa cập nhật");

                            int workplaceIndex = cursor.getColumnIndex("workplace");
                            doctor.setWorkplace(workplaceIndex >= 0 ? cursor.getString(workplaceIndex) : "Chưa cập nhật");

                            int ratingIndex = cursor.getColumnIndex("rating");
                            doctor.setRating(ratingIndex >= 0 ? cursor.getFloat(ratingIndex) : 0.0f);

                            int experienceIndex = cursor.getColumnIndex("experience");
                            doctor.setExperience(experienceIndex >= 0 ? cursor.getInt(experienceIndex) : 0);

                            int imageResourceIndex = cursor.getColumnIndex("image_resource");
                            doctor.setImageResource(imageResourceIndex >= 0 ? cursor.getInt(imageResourceIndex) : null);

                            int imagePathIndex = cursor.getColumnIndex("image_path");
                            if (imagePathIndex >= 0 && !cursor.isNull(imagePathIndex)) {
                                String imagePath = cursor.getString(imagePathIndex);
                                if (imagePath != null && !imagePath.isEmpty()) {
                                    doctor.setImagePath(imagePath);
                                }
                            }

                            doctors.add(doctor);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing doctor: " + e.getMessage());
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

            Log.d(TAG, "Loaded " + doctors.size() + " doctors");
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
        editingDoctor = doctor;
        selectedImagePath = doctor.getImagePath();
        showAddEditDialog(doctor);
    }

    @Override
    public void onDeleteDoctor(Doctor doctor, int position) {
        showDeleteConfirmDialog(doctor);
    }

    @Override
    public void onViewDoctorDetail(Doctor doctor) {
        showDoctorDetailDialog(doctor);
    }

    private void showAddDoctorDialog() {
        editingDoctor = null;
        selectedImagePath = null;
        showAddEditDialog(null);
    }

    private void showAddEditDialog(Doctor doctor) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_doctor, null);

        EditText edtName = dialogView.findViewById(R.id.edt_doctor_name);
        Spinner spinnerDegree = dialogView.findViewById(R.id.spinner_degree);
        Spinner spinnerSpecialty = dialogView.findViewById(R.id.spinner_specialty);
        EditText edtWorkplace = dialogView.findViewById(R.id.edt_workplace);
        EditText edtExperience = dialogView.findViewById(R.id.edt_experience);
        EditText edtRating = dialogView.findViewById(R.id.edt_rating);
        imgPreviewDoctor = dialogView.findViewById(R.id.img_preview_doctor);
        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);

        // Setup spinners
        String[] degrees = {"BS.", "ThS.BS.", "TS.BS.", "PGS.TS.", "GS.TS."};
        ArrayAdapter<String> degreeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, degrees);
        degreeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDegree.setAdapter(degreeAdapter);

        String[] specialties = {"Nội khoa", "Ngoại khoa", "Nhi khoa", "Sản khoa",
                "Da liễu", "Tim mạch", "Tiêu hóa", "Thần kinh", "Tai mũi họng"};
        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, specialties);
        specialtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(specialtyAdapter);

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Fill data if editing
        if (doctor != null) {
            edtName.setText(doctor.getName());
            edtWorkplace.setText(doctor.getWorkplace());
            edtExperience.setText(String.valueOf(doctor.getExperience()));
            edtRating.setText(String.valueOf(doctor.getRating()));

            // Set spinner positions
            for (int i = 0; i < degrees.length; i++) {
                if (degrees[i].equals(doctor.getDegree())) {
                    spinnerDegree.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < specialties.length; i++) {
                if (specialties[i].equals(doctor.getSpecialty())) {
                    spinnerSpecialty.setSelection(i);
                    break;
                }
            }

            // Display current image
            if (doctor.getImagePath() != null && !doctor.getImagePath().isEmpty()) {
                File imgFile = new File(doctor.getImagePath());
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(doctor.getImagePath());
                    imgPreviewDoctor.setImageBitmap(bitmap);
                }
            } else if (doctor.getImageResource() != 0) {
                imgPreviewDoctor.setImageResource(doctor.getImageResource());
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(doctor == null ? "Thêm bác sĩ" : "Sửa bác sĩ")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String name = edtName.getText().toString().trim();
                String workplace = edtWorkplace.getText().toString().trim();
                String experienceStr = edtExperience.getText().toString().trim();
                String ratingStr = edtRating.getText().toString().trim();

                if (name.isEmpty()) {
                    edtName.setError("Vui lòng nhập tên");
                    return;
                }

                if (workplace.isEmpty()) {
                    edtWorkplace.setError("Vui lòng nhập nơi làm việc");
                    return;
                }

                if (experienceStr.isEmpty()) {
                    edtExperience.setError("Vui lòng nhập kinh nghiệm");
                    return;
                }

                if (ratingStr.isEmpty()) {
                    edtRating.setError("Vui lòng nhập đánh giá");
                    return;
                }

                int experience = 0;
                float rating = 0.0f;

                try {
                    experience = Integer.parseInt(experienceStr);
                    rating = Float.parseFloat(ratingStr);

                    if (rating < 0 || rating > 5) {
                        Toast.makeText(this, "Đánh giá phải từ 0 đến 5!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Kinh nghiệm hoặc đánh giá không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Doctor newDoctor = new Doctor();
                if (doctor != null) {
                    newDoctor.setId(doctor.getId());
                }
                newDoctor.setName(name);
                newDoctor.setDegree(spinnerDegree.getSelectedItem().toString());
                newDoctor.setSpecialty(spinnerSpecialty.getSelectedItem().toString());
                newDoctor.setWorkplace(workplace);
                newDoctor.setExperience(experience);
                newDoctor.setRating(rating);
                newDoctor.setImageResource(R.drawable.doctor_1);
                newDoctor.setImagePath(selectedImagePath);

                if (doctor == null) {
                    new AddDoctorTask(newDoctor).execute();
                } else {
                    new UpdateDoctorTask(newDoctor).execute();
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Save to internal storage
                String fileName = "doctor_" + System.currentTimeMillis() + ".jpg";
                File directory = new File(getFilesDir(), "doctor_images");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                File imageFile = new File(directory, fileName);
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();

                selectedImagePath = imageFile.getAbsolutePath();
                imgPreviewDoctor.setImageBitmap(bitmap);

                Toast.makeText(this, "Đã chọn ảnh", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi khi chọn ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private class AddDoctorTask extends AsyncTask<Void, Void, Boolean> {
        private Doctor doctor;

        AddDoctorTask(Doctor doctor) {
            this.doctor = doctor;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String sql = "INSERT INTO doctors (name, degree, specialty, workplace, rating, experience, image_resource, image_path) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                db.execSQL(sql, new Object[]{
                        doctor.getName(),
                        doctor.getDegree(),
                        doctor.getSpecialty(),
                        doctor.getWorkplace(),
                        doctor.getRating(),
                        doctor.getExperience(),
                        doctor.getImageResource(),
                        doctor.getImagePath()
                });

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error adding doctor: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AdminDoctorManagementActivity.this,
                        "Đã thêm bác sĩ!", Toast.LENGTH_SHORT).show();
                loadDoctors();
            } else {
                Toast.makeText(AdminDoctorManagementActivity.this,
                        "Lỗi khi thêm!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateDoctorTask extends AsyncTask<Void, Void, Boolean> {
        private Doctor doctor;

        UpdateDoctorTask(Doctor doctor) {
            this.doctor = doctor;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String sql = "UPDATE doctors SET name = ?, degree = ?, specialty = ?, " +
                        "workplace = ?, rating = ?, experience = ?, image_path = ? " +
                        "WHERE id = ?";

                db.execSQL(sql, new Object[]{
                        doctor.getName(),
                        doctor.getDegree(),
                        doctor.getSpecialty(),
                        doctor.getWorkplace(),
                        doctor.getRating(),
                        doctor.getExperience(),
                        doctor.getImagePath(),
                        doctor.getId()
                });

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error updating doctor: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AdminDoctorManagementActivity.this,
                        "Đã cập nhật bác sĩ!", Toast.LENGTH_SHORT).show();
                loadDoctors();
            } else {
                Toast.makeText(AdminDoctorManagementActivity.this,
                        "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmDialog(Doctor doctor) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bác sĩ:\n" +
                        doctor.getDegree() + " " + doctor.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new DeleteDoctorTask(doctor).execute();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private class DeleteDoctorTask extends AsyncTask<Void, Void, Boolean> {
        private Doctor doctor;

        DeleteDoctorTask(Doctor doctor) {
            this.doctor = doctor;
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

                db.execSQL("DELETE FROM doctors WHERE id = ?", new Object[]{doctor.getId()});
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error deleting: " + e.getMessage());
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
        String details = "THÔNG TIN CHI TIẾT\n\n" +
                "ID: #" + doctor.getId() + "\n" +
                "Họ tên: " + doctor.getDegree() + " " + doctor.getName() + "\n" +
                "Học vị: " + doctor.getDegree() + "\n" +
                "Chuyên khoa: " + (doctor.getSpecialty() != null ? doctor.getSpecialty() : "Chưa cập nhật") + "\n" +
                "Nơi làm việc: " + (doctor.getWorkplace() != null ? doctor.getWorkplace() : "Chưa cập nhật") + "\n" +
                "Kinh nghiệm: " + doctor.getExperience() + " năm\n" +
                "Đánh giá: " + doctor.getRating() + "/5.0 ⭐\n" +
                "Ảnh: " + (doctor.getImagePath() != null ? "Tùy chỉnh" : "Mặc định");

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết bác sĩ")
                .setMessage(details)
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}