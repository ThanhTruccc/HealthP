package com.example.healthprofile;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.ExerciseAdapter;
import com.example.healthprofile.model.Exercise;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExerciseTrackerActivity extends AppCompatActivity {

    private RecyclerView rvExercises;
    private FloatingActionButton fabAdd;
    private ImageButton btnBack;
    private LinearLayout emptyState;
    private TextView tvTotalDuration, tvTotalCalories, tvWeeklyGoal;

    private SQLiteDatabase db;
    private String userEmail;
    private ExerciseAdapter adapter;
    private List<Exercise> exerciseList;

    // Exercise types với calories burn rate (per minute)
    private static final String[] EXERCISE_TYPES = {
            "Chạy bộ", "Đi bộ", "Đạp xe", "Bơi lội", "Yoga",
            "Gym", "Cầu lông", "Bóng đá", "Bóng rổ", "Khiêu vũ",
            "Leo núi", "Aerobic", "HIIT", "Tập tạ", "Khác"
    };

    private static final double[] CALORIES_PER_MINUTE = {
            10.0, 4.0, 8.0, 11.0, 3.0,
            7.0, 7.0, 9.0, 8.0, 5.0,
            9.0, 8.0, 12.0, 6.0, 5.0
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tracker);

        // Lấy email
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        // Mở database
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);
        createTableIfNotExists();

        initViews();
        loadExercises();
        updateStatistics();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS exercises (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "exercise_type TEXT NOT NULL, " +
                "duration_minutes INTEGER NOT NULL, " +
                "calories_burned REAL, " +
                "date TEXT NOT NULL, " +
                "notes TEXT, " +
                "created_at INTEGER)";
        db.execSQL(sql);
    }

    private void initViews() {
        rvExercises = findViewById(R.id.rv_exercises);
        fabAdd = findViewById(R.id.fab_add_exercise);
        btnBack = findViewById(R.id.btn_back_exercise);
        emptyState = findViewById(R.id.empty_state_exercise);
        tvTotalDuration = findViewById(R.id.tv_total_duration);
        tvTotalCalories = findViewById(R.id.tv_total_calories);
        tvWeeklyGoal = findViewById(R.id.tv_weekly_goal);

        btnBack.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showAddExerciseDialog());

        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        exerciseList = new ArrayList<>();
        adapter = new ExerciseAdapter(this, exerciseList, new ExerciseAdapter.OnExerciseActionListener() {
            @Override
            public void onEdit(Exercise exercise) {
                showEditExerciseDialog(exercise);
            }

            @Override
            public void onDelete(Exercise exercise) {
                deleteExercise(exercise);
            }
        });
        rvExercises.setAdapter(adapter);
    }

    private void loadExercises() {
        exerciseList.clear();

        // Lấy exercises của 30 ngày gần nhất
        String sql = "SELECT * FROM exercises " +
                "WHERE user_email = '" + escapeString(userEmail) + "' " +
                "ORDER BY date DESC, created_at DESC " +
                "LIMIT 100";

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise();
                exercise.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                exercise.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
                exercise.setExerciseType(cursor.getString(cursor.getColumnIndexOrThrow("exercise_type")));
                exercise.setDurationMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes")));
                exercise.setCaloriesBurned(cursor.getDouble(cursor.getColumnIndexOrThrow("calories_burned")));
                exercise.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                exercise.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                exercise.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (exerciseList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvExercises.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvExercises.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatistics() {
        // Tính tổng thời gian và calories tuần này
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String weekStart = sdf.format(calendar.getTime());

        String sql = "SELECT SUM(duration_minutes) as total_duration, " +
                "SUM(calories_burned) as total_calories " +
                "FROM exercises " +
                "WHERE user_email = '" + escapeString(userEmail) + "' " +
                "AND date >= '" + weekStart + "'";

        Cursor cursor = db.rawQuery(sql, null);

        int totalDuration = 0;
        double totalCalories = 0;

        if (cursor.moveToFirst()) {
            totalDuration = cursor.getInt(0);
            totalCalories = cursor.getDouble(1);
        }
        cursor.close();

        // Cập nhật UI
        tvTotalDuration.setText(totalDuration + " phút");
        tvTotalCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", totalCalories));

        // Mục tiêu: 150 phút/tuần (WHO khuyến nghị)
        int weeklyGoal = 150;
        int progress = (int) ((totalDuration * 100.0) / weeklyGoal);
        tvWeeklyGoal.setText(progress + "% mục tiêu tuần");
    }

    private void showAddExerciseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_exercise, null);

        Spinner spinnerType = dialogView.findViewById(R.id.spinner_exercise_type);
        TextInputEditText etDuration = dialogView.findViewById(R.id.et_duration);
        TextInputEditText etDate = dialogView.findViewById(R.id.et_exercise_date);
        TextInputEditText etNotes = dialogView.findViewById(R.id.et_exercise_notes);
        TextView tvCaloriesPreview = dialogView.findViewById(R.id.tv_calories_preview);

        // Setup spinner
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, EXERCISE_TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Setup date picker
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(Calendar.getInstance().getTime()));
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> showDatePicker(etDate));

        // Preview calories khi nhập duration
        etDuration.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    try {
                        int duration = Integer.parseInt(s.toString());
                        int typeIndex = spinnerType.getSelectedItemPosition();
                        double calories = duration * CALORIES_PER_MINUTE[typeIndex];
                        tvCaloriesPreview.setText(String.format(Locale.getDefault(),
                                "≈ %.0f calories", calories));
                        tvCaloriesPreview.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        tvCaloriesPreview.setVisibility(View.GONE);
                    }
                } else {
                    tvCaloriesPreview.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm hoạt động vận động")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String durationStr = etDuration.getText().toString().trim();
                String date = etDate.getText().toString().trim();
                String notes = etNotes.getText().toString().trim();

                if (durationStr.isEmpty()) {
                    etDuration.setError("Vui lòng nhập thời gian");
                    return;
                }

                try {
                    int duration = Integer.parseInt(durationStr);
                    if (duration <= 0) {
                        etDuration.setError("Thời gian phải lớn hơn 0");
                        return;
                    }

                    String type = EXERCISE_TYPES[spinnerType.getSelectedItemPosition()];
                    double calories = duration * CALORIES_PER_MINUTE[spinnerType.getSelectedItemPosition()];

                    Exercise exercise = new Exercise();
                    exercise.setUserEmail(userEmail);
                    exercise.setExerciseType(type);
                    exercise.setDurationMinutes(duration);
                    exercise.setCaloriesBurned(calories);
                    exercise.setDate(date);
                    exercise.setNotes(notes);

                    if (saveExercise(exercise)) {
                        Toast.makeText(this, "Đã lưu hoạt động vận động", Toast.LENGTH_SHORT).show();
                        loadExercises();
                        updateStatistics();
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    etDuration.setError("Thời gian không hợp lệ");
                }
            });
        });

        dialog.show();
    }

    private void showEditExerciseDialog(Exercise exercise) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_exercise, null);

        Spinner spinnerType = dialogView.findViewById(R.id.spinner_exercise_type);
        TextInputEditText etDuration = dialogView.findViewById(R.id.et_duration);
        TextInputEditText etDate = dialogView.findViewById(R.id.et_exercise_date);
        TextInputEditText etNotes = dialogView.findViewById(R.id.et_exercise_notes);
        TextView tvCaloriesPreview = dialogView.findViewById(R.id.tv_calories_preview);

        // Setup spinner
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, EXERCISE_TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Fill existing data
        for (int i = 0; i < EXERCISE_TYPES.length; i++) {
            if (EXERCISE_TYPES[i].equals(exercise.getExerciseType())) {
                spinnerType.setSelection(i);
                break;
            }
        }
        etDuration.setText(String.valueOf(exercise.getDurationMinutes()));
        etDate.setText(exercise.getDate());
        etNotes.setText(exercise.getNotes());

        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> showDatePicker(etDate));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa hoạt động vận động")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String durationStr = etDuration.getText().toString().trim();

                if (durationStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int duration = Integer.parseInt(durationStr);
                    String type = EXERCISE_TYPES[spinnerType.getSelectedItemPosition()];
                    double calories = duration * CALORIES_PER_MINUTE[spinnerType.getSelectedItemPosition()];

                    exercise.setExerciseType(type);
                    exercise.setDurationMinutes(duration);
                    exercise.setCaloriesBurned(calories);
                    exercise.setDate(etDate.getText().toString().trim());
                    exercise.setNotes(etNotes.getText().toString().trim());

                    if (updateExercise(exercise)) {
                        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                        loadExercises();
                        updateStatistics();
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();

        // Parse current date if exists
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            calendar.setTime(sdf.parse(editText.getText().toString()));
        } catch (Exception e) {
            // Use current date
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editText.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private boolean saveExercise(Exercise exercise) {
        try {
            String sql = "INSERT INTO exercises " +
                    "(user_email, exercise_type, duration_minutes, calories_burned, date, notes, created_at) " +
                    "VALUES ('" + escapeString(exercise.getUserEmail()) + "', " +
                    "'" + escapeString(exercise.getExerciseType()) + "', " +
                    exercise.getDurationMinutes() + ", " +
                    exercise.getCaloriesBurned() + ", " +
                    "'" + escapeString(exercise.getDate()) + "', " +
                    "'" + escapeString(exercise.getNotes()) + "', " +
                    System.currentTimeMillis() + ")";
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateExercise(Exercise exercise) {
        try {
            String sql = "UPDATE exercises SET " +
                    "exercise_type = '" + escapeString(exercise.getExerciseType()) + "', " +
                    "duration_minutes = " + exercise.getDurationMinutes() + ", " +
                    "calories_burned = " + exercise.getCaloriesBurned() + ", " +
                    "date = '" + escapeString(exercise.getDate()) + "', " +
                    "notes = '" + escapeString(exercise.getNotes()) + "' " +
                    "WHERE id = " + exercise.getId();
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteExercise(Exercise exercise) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa hoạt động")
                .setMessage("Bạn có chắc muốn xóa hoạt động này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try {
                        db.execSQL("DELETE FROM exercises WHERE id = " + exercise.getId());
                        Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                        loadExercises();
                        updateStatistics();
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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