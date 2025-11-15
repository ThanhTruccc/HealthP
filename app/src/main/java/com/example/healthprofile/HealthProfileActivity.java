package com.example.healthprofile;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthprofile.model.HealthProfile;
import com.google.android.material.textfield.TextInputEditText;

public class HealthProfileActivity extends AppCompatActivity {

    private static final String TAG = "HealthProfileActivity";
    private static final String DATABASE_NAME = "health_profile.db";
    private static final String TABLE_HEALTH_PROFILES = "health_profiles";

    // Reward points constants
    private static final int POINTS_FIRST_UPDATE = 50;
    private static final int POINTS_WEEKLY_UPDATE = 20;
    private static final long WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L;

    private TextInputEditText etFullName, etAge, etHeight, etWeight;
    private TextInputEditText etAllergies, etChronicDiseases, etMedications;
    private TextInputEditText etEmergencyContact, etEmergencyContactName, etNotes;
    private RadioGroup rgGender;
    private Spinner spinnerBloodType, spinnerRhFactor;
    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private SQLiteDatabase db;
    private String userEmail;
    private HealthProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_profile);

        // Mở database
        db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        createTableIfNotExists();

        // Lấy user email
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        initViews();
        setupSpinners();
        loadHealthProfile();
        setupClickListeners();
    }

    private void createTableIfNotExists() {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_HEALTH_PROFILES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT UNIQUE NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "age INTEGER, " +
                "gender TEXT, " +
                "height REAL, " +
                "weight REAL, " +
                "blood_type TEXT, " +
                "rh_factor TEXT, " +
                "allergies TEXT, " +
                "chronic_diseases TEXT, " +
                "medications TEXT, " +
                "emergency_contact TEXT, " +
                "emergency_contact_name TEXT, " +
                "notes TEXT, " +
                "last_updated INTEGER" +
                ")";
        db.execSQL(createTable);
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        etAllergies = findViewById(R.id.et_allergies);
        etChronicDiseases = findViewById(R.id.et_chronic_diseases);
        etMedications = findViewById(R.id.et_medications);
        etEmergencyContact = findViewById(R.id.et_emergency_contact);
        etEmergencyContactName = findViewById(R.id.et_emergency_contact_name);
        etNotes = findViewById(R.id.et_notes);
        rgGender = findViewById(R.id.rg_gender);
        spinnerBloodType = findViewById(R.id.spinner_blood_type);
        spinnerRhFactor = findViewById(R.id.spinner_rh_factor);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupSpinners() {
        // Blood Type Spinner
        String[] bloodTypes = {"Chọn nhóm máu", "A", "B", "AB", "O"};
        ArrayAdapter<String> bloodTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bloodTypes);
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(bloodTypeAdapter);

        // Rh Factor Spinner
        String[] rhFactors = {"Chọn Rh", "+", "-"};
        ArrayAdapter<String> rhAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rhFactors);
        rhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRhFactor.setAdapter(rhAdapter);
    }

    /**
     * Load thông tin hồ sơ hiện tại
     */
    private void loadHealthProfile() {
        new LoadProfileTask().execute();
    }

    private class LoadProfileTask extends AsyncTask<Void, Void, HealthProfile> {
        @Override
        protected void onPreExecute() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected HealthProfile doInBackground(Void... voids) {
            try {
                return getHealthProfileByEmail(userEmail);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(HealthProfile profile) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            currentProfile = profile;
            if (profile != null) {
                populateFields(profile);
            }
        }
    }

    /**
     * Lấy health profile từ database
     */
    private HealthProfile getHealthProfileByEmail(String email) {
        String sql = "SELECT * FROM " + TABLE_HEALTH_PROFILES +
                " WHERE user_email = '" + escapeString(email) + "'";
        Cursor cursor = db.rawQuery(sql, null);

        HealthProfile profile = null;
        if (cursor.moveToFirst()) {
            profile = new HealthProfile();
            profile.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            profile.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
            profile.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            profile.setAge(cursor.getInt(cursor.getColumnIndexOrThrow("age")));
            profile.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
            profile.setHeight(cursor.getFloat(cursor.getColumnIndexOrThrow("height")));
            profile.setWeight(cursor.getFloat(cursor.getColumnIndexOrThrow("weight")));
            profile.setBloodType(cursor.getString(cursor.getColumnIndexOrThrow("blood_type")));
            profile.setRhFactor(cursor.getString(cursor.getColumnIndexOrThrow("rh_factor")));
            profile.setAllergies(cursor.getString(cursor.getColumnIndexOrThrow("allergies")));
            profile.setChronicDiseases(cursor.getString(cursor.getColumnIndexOrThrow("chronic_diseases")));
            profile.setMedications(cursor.getString(cursor.getColumnIndexOrThrow("medications")));
            profile.setEmergencyContact(cursor.getString(cursor.getColumnIndexOrThrow("emergency_contact")));
            profile.setEmergencyContactName(cursor.getString(cursor.getColumnIndexOrThrow("emergency_contact_name")));
            profile.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
            profile.setLastUpdated(cursor.getLong(cursor.getColumnIndexOrThrow("last_updated")));
        }
        cursor.close();

        return profile;
    }

    /**
     * Điền thông tin vào các field
     */
    private void populateFields(HealthProfile profile) {
        etFullName.setText(profile.getFullName());
        etAge.setText(String.valueOf(profile.getAge()));
        etHeight.setText(String.valueOf(profile.getHeight()));
        etWeight.setText(String.valueOf(profile.getWeight()));
        etAllergies.setText(profile.getAllergies());
        etChronicDiseases.setText(profile.getChronicDiseases());
        etMedications.setText(profile.getMedications());
        etEmergencyContact.setText(profile.getEmergencyContact());
        etEmergencyContactName.setText(profile.getEmergencyContactName());
        etNotes.setText(profile.getNotes());

        // Set gender
        if ("Nam".equals(profile.getGender())) {
            rgGender.check(R.id.rb_male);
        } else if ("Nữ".equals(profile.getGender())) {
            rgGender.check(R.id.rb_female);
        }

        // Set blood type
        if (profile.getBloodType() != null) {
            int bloodPosition = getBloodTypePosition(profile.getBloodType());
            if (bloodPosition >= 0) {
                spinnerBloodType.setSelection(bloodPosition);
            }
        }

        // Set Rh factor
        if (profile.getRhFactor() != null) {
            int rhPosition = getRhPosition(profile.getRhFactor());
            if (rhPosition >= 0) {
                spinnerRhFactor.setSelection(rhPosition);
            }
        }
    }

    private int getBloodTypePosition(String bloodType) {
        String[] types = {"Chọn nhóm máu", "A", "B", "AB", "O"};
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(bloodType)) return i;
        }
        return 0;
    }

    private int getRhPosition(String rh) {
        String[] factors = {"Chọn Rh", "+", "-"};
        for (int i = 0; i < factors.length; i++) {
            if (factors[i].equals(rh)) return i;
        }
        return 0;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveHealthProfile());
    }

    /**
     * Lưu thông tin hồ sơ
     */
    private void saveHealthProfile() {
        // Validation
        String fullName = etFullName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }

        if (ageStr.isEmpty()) {
            etAge.setError("Vui lòng nhập tuổi");
            etAge.requestFocus();
            return;
        }

        // Tạo HealthProfile object
        HealthProfile profile = new HealthProfile();
        profile.setUserEmail(userEmail);
        profile.setFullName(fullName);
        profile.setAge(Integer.parseInt(ageStr));

        // Gender
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        RadioButton rbGender = findViewById(selectedGenderId);
        profile.setGender(rbGender != null ? rbGender.getText().toString() : "");

        // Height & Weight
        if (!heightStr.isEmpty()) {
            profile.setHeight(Float.parseFloat(heightStr));
        }
        if (!weightStr.isEmpty()) {
            profile.setWeight(Float.parseFloat(weightStr));
        }

        // Blood type
        String bloodType = spinnerBloodType.getSelectedItem().toString();
        if (!"Chọn nhóm máu".equals(bloodType)) {
            profile.setBloodType(bloodType);
        }

        String rhFactor = spinnerRhFactor.getSelectedItem().toString();
        if (!"Chọn Rh".equals(rhFactor)) {
            profile.setRhFactor(rhFactor);
        }

        // Other info
        profile.setAllergies(etAllergies.getText().toString().trim());
        profile.setChronicDiseases(etChronicDiseases.getText().toString().trim());
        profile.setMedications(etMedications.getText().toString().trim());
        profile.setEmergencyContact(etEmergencyContact.getText().toString().trim());
        profile.setEmergencyContactName(etEmergencyContactName.getText().toString().trim());
        profile.setNotes(etNotes.getText().toString().trim());

        // Save to database
        new SaveProfileTask(profile).execute();
    }

    private class SaveProfileTask extends AsyncTask<Void, Void, SaveResult> {
        private HealthProfile profile;

        SaveProfileTask(HealthProfile profile) {
            this.profile = profile;
        }

        @Override
        protected void onPreExecute() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            btnSave.setEnabled(false);
        }

        @Override
        protected SaveResult doInBackground(Void... voids) {
            try {
                return saveProfileAndCalculateReward(profile);
            } catch (Exception e) {
                Log.e(TAG, "Error saving profile: " + e.getMessage());
                e.printStackTrace();
                return new SaveResult(-1, 0, "");
            }
        }

        @Override
        protected void onPostExecute(SaveResult result) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            btnSave.setEnabled(true);

            if (result.profileId > 0) {
                showSuccessDialog(result.pointsAwarded, result.rewardMessage);
            } else {
                Toast.makeText(HealthProfileActivity.this,
                        "Lỗi khi lưu hồ sơ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Class để lưu kết quả save
     */
    private static class SaveResult {
        long profileId;
        int pointsAwarded;
        String rewardMessage;

        SaveResult(long profileId, int pointsAwarded, String rewardMessage) {
            this.profileId = profileId;
            this.pointsAwarded = pointsAwarded;
            this.rewardMessage = rewardMessage;
        }
    }

    /**
     * Lưu profile và tính điểm thưởng
     */
    private SaveResult saveProfileAndCalculateReward(HealthProfile profile) {
        // Kiểm tra xem profile đã tồn tại chưa và lần cập nhật cuối
        String checkSql = "SELECT id, last_updated FROM " + TABLE_HEALTH_PROFILES +
                " WHERE user_email = '" + escapeString(profile.getUserEmail()) + "'";
        Cursor cursor = db.rawQuery(checkSql, null);

        boolean isFirstUpdate = !cursor.moveToFirst();
        int existingId = -1;
        long lastUpdated = 0;

        if (!isFirstUpdate) {
            existingId = cursor.getInt(0);
            lastUpdated = cursor.getLong(1);
        }
        cursor.close();

        // Lưu profile
        long profileId = insertOrUpdateHealthProfile(profile, isFirstUpdate, existingId);

        if (profileId <= 0) {
            return new SaveResult(-1, 0, "");
        }

        // Tính điểm thưởng
        int pointsAwarded = 0;
        String rewardMessage = "";

        if (isFirstUpdate) {
            // Lần đầu tiên cập nhật hồ sơ: +50 điểm
            pointsAwarded = POINTS_FIRST_UPDATE;
            rewardMessage = "Chúc mừng! Bạn nhận được " + pointsAwarded + " điểm cho lần cập nhật hồ sơ đầu tiên!";
            addRewardPoints(profile.getUserEmail(), "first_health_profile", pointsAwarded,
                    "Cập nhật hồ sơ sức khỏe lần đầu");
            Log.d(TAG, "First update: awarded " + pointsAwarded + " points");

        } else {
            // Kiểm tra xem đã qua 1 tuần chưa
            long currentTime = System.currentTimeMillis();
            long timeSinceLastUpdate = currentTime - lastUpdated;

            if (timeSinceLastUpdate >= WEEK_IN_MILLIS) {
                // Cập nhật định kỳ (sau 1 tuần): +20 điểm
                pointsAwarded = POINTS_WEEKLY_UPDATE;
                rewardMessage = "Bạn nhận được " + pointsAwarded + " điểm cho việc cập nhật hồ sơ định kỳ!";
                addRewardPoints(profile.getUserEmail(), "weekly_health_update", pointsAwarded,
                        "Cập nhật hồ sơ sức khỏe định kỳ");
                Log.d(TAG, "Weekly update: awarded " + pointsAwarded + " points");
            } else {
                // Cập nhật nhưng chưa đủ 1 tuần
                long daysLeft = (WEEK_IN_MILLIS - timeSinceLastUpdate) / (24 * 60 * 60 * 1000L);
                rewardMessage = "Hồ sơ đã được cập nhật! (Cập nhật lại sau " + daysLeft + " ngày để nhận thêm điểm)";
                Log.d(TAG, "Update too soon: " + daysLeft + " days until next reward");
            }
        }

        return new SaveResult(profileId, pointsAwarded, rewardMessage);
    }

    /**
     * Thêm hoặc cập nhật health profile
     */
    private long insertOrUpdateHealthProfile(HealthProfile profile, boolean isFirstUpdate, int existingId) {
        if (!isFirstUpdate && existingId > 0) {
            // Update existing profile
            String sql = "UPDATE " + TABLE_HEALTH_PROFILES + " SET " +
                    "full_name = '" + escapeString(profile.getFullName()) + "', " +
                    "age = " + profile.getAge() + ", " +
                    "gender = '" + escapeString(profile.getGender()) + "', " +
                    "height = " + profile.getHeight() + ", " +
                    "weight = " + profile.getWeight() + ", " +
                    "blood_type = '" + escapeString(profile.getBloodType()) + "', " +
                    "rh_factor = '" + escapeString(profile.getRhFactor()) + "', " +
                    "allergies = '" + escapeString(profile.getAllergies()) + "', " +
                    "chronic_diseases = '" + escapeString(profile.getChronicDiseases()) + "', " +
                    "medications = '" + escapeString(profile.getMedications()) + "', " +
                    "emergency_contact = '" + escapeString(profile.getEmergencyContact()) + "', " +
                    "emergency_contact_name = '" + escapeString(profile.getEmergencyContactName()) + "', " +
                    "notes = '" + escapeString(profile.getNotes()) + "', " +
                    "last_updated = " + System.currentTimeMillis() + " " +
                    "WHERE user_email = '" + escapeString(profile.getUserEmail()) + "'";

            db.execSQL(sql);
            return existingId;
        } else {
            // Insert new profile
            String sql = "INSERT INTO " + TABLE_HEALTH_PROFILES +
                    " (user_email, full_name, age, gender, height, weight, blood_type, rh_factor, " +
                    "allergies, chronic_diseases, medications, emergency_contact, emergency_contact_name, notes, last_updated) " +
                    "VALUES ('" + escapeString(profile.getUserEmail()) + "', " +
                    "'" + escapeString(profile.getFullName()) + "', " +
                    profile.getAge() + ", " +
                    "'" + escapeString(profile.getGender()) + "', " +
                    profile.getHeight() + ", " +
                    profile.getWeight() + ", " +
                    "'" + escapeString(profile.getBloodType()) + "', " +
                    "'" + escapeString(profile.getRhFactor()) + "', " +
                    "'" + escapeString(profile.getAllergies()) + "', " +
                    "'" + escapeString(profile.getChronicDiseases()) + "', " +
                    "'" + escapeString(profile.getMedications()) + "', " +
                    "'" + escapeString(profile.getEmergencyContact()) + "', " +
                    "'" + escapeString(profile.getEmergencyContactName()) + "', " +
                    "'" + escapeString(profile.getNotes()) + "', " +
                    System.currentTimeMillis() + ")";

            db.execSQL(sql);

            Cursor idCursor = db.rawQuery("SELECT last_insert_rowid()", null);
            long id = -1;
            if (idCursor.moveToFirst()) {
                id = idCursor.getLong(0);
            }
            idCursor.close();
            return id;
        }
    }

    /**
     * Thêm điểm thưởng vào bảng reward_points
     */
    private void addRewardPoints(String userEmail, String action, int pointsChange, String description) {
        try {
            // Lấy tổng điểm hiện tại
            int currentPoints = getTotalPoints(userEmail);
            int newPoints = currentPoints + pointsChange;

            String sql = "INSERT INTO reward_points " +
                    "(user_email, points, actionn, points_change, description, timestamp) " +
                    "VALUES ('" + escapeString(userEmail) + "', " +
                    newPoints + ", " +
                    "'" + escapeString(action) + "', " +
                    pointsChange + ", " +
                    "'" + escapeString(description) + "', " +
                    System.currentTimeMillis() + ")";
            db.execSQL(sql);

            Log.d(TAG, "Reward points added successfully: " + pointsChange);
        } catch (Exception e) {
            Log.e(TAG, "Error adding reward points: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy tổng điểm của user
     */
    private int getTotalPoints(String userEmail) {
        try {
            String sql = "SELECT COALESCE(SUM(points_change), 0) FROM reward_points " +
                    "WHERE user_email = '" + escapeString(userEmail) + "'";
            Cursor cursor = db.rawQuery(sql, null);

            int points = 0;
            if (cursor.moveToFirst()) {
                points = cursor.getInt(0);
            }
            cursor.close();
            return Math.max(0, points);
        } catch (Exception e) {
            Log.e(TAG, "Error getting total points: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Escape string để tránh SQL injection
     */
    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    private void showSuccessDialog(int pointsAwarded, String rewardMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thành công");

        String message = "Hồ sơ sức khỏe đã được cập nhật thành công!";
        if (pointsAwarded > 0) {
            message += "\n\n" + rewardMessage;
        } else if (!rewardMessage.isEmpty()) {
            message += "\n\n" + rewardMessage;
        }

        builder.setMessage(message);
        builder.setPositiveButton("Đồng ý", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}