package com.example.healthprofile.fragment;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.healthprofile.BMICalculatorActivity;
import com.example.healthprofile.HealthProfileActivity;
import com.example.healthprofile.HealthTipsActivity;
import com.example.healthprofile.MedicationReminderActivity;
import com.example.healthprofile.R;
import com.example.healthprofile.model.HealthProfile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.MODE_PRIVATE;

/**
 * Fragment tiện ích với các công cụ hỗ trợ sức khỏe
 */
public class UtilityFragment extends Fragment {

    private CardView cardHealthProfile, cardBMICalculator, cardHealthTips, cardMedicationReminder;
    private TextView tvProfileStatus, tvBmiValue, tvMedicationCount;

    // SQLiteDatabase trực tiếp
    private SQLiteDatabase db;
    private String userEmail;

    // Database constants
    private static final String DATABASE_NAME = "health_profile.db";
    private static final String TABLE_HEALTH_PROFILES = "health_profiles";
    private ExecutorService executorService;
    private Handler mainHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_utility, container, false);

        db = requireContext().openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);

        // Khởi tạo ExecutorService và Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Lấy thông tin user
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        initViews(view);
        loadHealthProfileSummary();
        loadMedicationReminderCount();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        cardHealthProfile = view.findViewById(R.id.card_health_profile);
        cardBMICalculator = view.findViewById(R.id.card_bmi_calculator);
        cardHealthTips = view.findViewById(R.id.card_health_tips);
        cardMedicationReminder = view.findViewById(R.id.card_medication_reminder);
        tvProfileStatus = view.findViewById(R.id.tv_profile_status);
        tvBmiValue = view.findViewById(R.id.tv_bmi_value);
        tvMedicationCount = view.findViewById(R.id.tv_medication_count);
    }

    /**
     * Load tóm tắt thông tin health profile
     */
    private void loadHealthProfileSummary() {
        // Execute in background thread
        executorService.execute(() -> {
            HealthProfile profile = getHealthProfileByEmail(userEmail);

            // Update UI on main thread
            mainHandler.post(() -> {
                if (isAdded() && getContext() != null) {
                    updateProfileUI(profile);
                }
            });
        });
    }

    /**
     * Load số lượng nhắc nhở uống thuốc đang active
     */
    private void loadMedicationReminderCount() {
        executorService.execute(() -> {
            int count = getActiveMedicationCount(userEmail);

            mainHandler.post(() -> {
                if (isAdded() && getContext() != null) {
                    updateMedicationCountUI(count);
                }
            });
        });
    }

    /**
     * Lấy số lượng nhắc nhở đang hoạt động
     */
    private int getActiveMedicationCount(String userEmail) {
        String sql = "SELECT COUNT(*) FROM medication_reminders " +
                "WHERE user_email = '" + escapeString(userEmail) + "' " +
                "AND is_active = 1";
        Cursor cursor = db.rawQuery(sql, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Cập nhật UI số lượng nhắc nhở
     */
    private void updateMedicationCountUI(int count) {
        if (count > 0) {
            tvMedicationCount.setText(count + " nhắc nhở đang hoạt động");
            tvMedicationCount.setVisibility(View.VISIBLE);
            tvMedicationCount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvMedicationCount.setText("Chưa có nhắc nhở");
            tvMedicationCount.setVisibility(View.VISIBLE);
            tvMedicationCount.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    /**
     * Lấy health profile theo email
     */
    private HealthProfile getHealthProfileByEmail(String userEmail) {
        String sql = "SELECT * FROM " + TABLE_HEALTH_PROFILES +
                " WHERE user_email = '" + escapeString(userEmail) + "'";
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
     * Update UI với thông tin profile
     */
    private void updateProfileUI(HealthProfile profile) {
        if (profile != null) {
            tvProfileStatus.setText("Đã cập nhật");
            tvProfileStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            // Hiển thị BMI nếu có
            if (profile.getHeight() > 0 && profile.getWeight() > 0) {
                float bmi = profile.getBMI();
                String category = profile.getBMICategory();

                tvBmiValue.setText(String.format("BMI: %.1f (%s)", bmi, category));
                tvBmiValue.setVisibility(View.VISIBLE);

                // Set màu theo category
                int color = getBMIColor(category);
                tvBmiValue.setTextColor(color);
            } else {
                tvBmiValue.setVisibility(View.GONE);
            }
        } else {
            tvProfileStatus.setText("Chưa cập nhật");
            tvProfileStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            tvBmiValue.setVisibility(View.GONE);
        }
    }

    /**
     * Lấy màu theo BMI category
     */
    private int getBMIColor(String category) {
        if (category == null) return getResources().getColor(android.R.color.darker_gray);

        switch (category) {
            case "Bình thường":
                return getResources().getColor(android.R.color.holo_green_dark);
            case "Thừa cân":
                return getResources().getColor(android.R.color.holo_orange_dark);
            case "Béo phì":
                return getResources().getColor(android.R.color.holo_red_dark);
            case "Thiếu cân":
                return getResources().getColor(android.R.color.holo_blue_dark);
            default:
                return getResources().getColor(android.R.color.darker_gray);
        }
    }

    private void setupClickListeners() {
        // Card Hồ sơ sức khỏe
        cardHealthProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HealthProfileActivity.class);
            startActivity(intent);
        });

        // Card Tính BMI
        cardBMICalculator.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BMICalculatorActivity.class);
            startActivity(intent);
        });

        // Card Mẹo sức khỏe
        cardHealthTips.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HealthTipsActivity.class);
            startActivity(intent);
        });

        // Card Nhắc nhở uống thuốc
        cardMedicationReminder.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MedicationReminderActivity.class);
            startActivity(intent);
        });
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
        // Refresh khi quay lại fragment
        loadHealthProfileSummary();
        loadMedicationReminderCount();
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