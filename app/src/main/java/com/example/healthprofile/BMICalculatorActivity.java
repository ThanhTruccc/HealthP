package com.example.healthprofile;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.healthprofile.model.BMIRecord;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class BMICalculatorActivity extends AppCompatActivity {

    private RadioGroup rgGender;
    private TextInputEditText etAge, etHeight, etWeight;
    private Slider sliderHeight, sliderWeight;
    private Button btnCalculate, btnViewHistory;
    private CardView cardResult;
    private TextView tvBmiValue, tvBmiCategory, tvBmiDescription, tvAdvice;
    private ImageButton btnBack;

    private boolean isUpdatingHeight = false;
    private boolean isUpdatingWeight = false;

    private SQLiteDatabase db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        // Lấy email từ SharedPreferences
        userEmail = getSharedPreferences("HealthProfile", MODE_PRIVATE)
                .getString("userEmail", "");

        initDatabase();
        initViews();
        setupSliders();
        setupClickListeners();
    }

    private void initDatabase() {
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        // Tạo bảng bmi_records nếu chưa có
        db.execSQL("CREATE TABLE IF NOT EXISTS bmi_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "gender TEXT, " +
                "age INTEGER, " +
                "height REAL NOT NULL, " +
                "weight REAL NOT NULL, " +
                "bmi REAL NOT NULL, " +
                "category TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL)");
    }

    private void initViews() {
        rgGender = findViewById(R.id.rg_gender);
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        sliderHeight = findViewById(R.id.slider_height);
        sliderWeight = findViewById(R.id.slider_weight);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnViewHistory = findViewById(R.id.btn_view_history);
        cardResult = findViewById(R.id.card_result);
        tvBmiValue = findViewById(R.id.tv_bmi_value);
        tvBmiCategory = findViewById(R.id.tv_bmi_category);
        tvBmiDescription = findViewById(R.id.tv_bmi_description);
        tvAdvice = findViewById(R.id.tv_advice);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupSliders() {
        // Height slider
        sliderHeight.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                if (!isUpdatingHeight) {
                    isUpdatingHeight = true;
                    etHeight.setText(String.valueOf((int) value));
                    isUpdatingHeight = false;
                }
            }
        });

        // Height text input
        etHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdatingHeight && s.length() > 0) {
                    try {
                        isUpdatingHeight = true;
                        float height = Float.parseFloat(s.toString());
                        if (height >= 100 && height <= 220) {
                            sliderHeight.setValue(height);
                        }
                        isUpdatingHeight = false;
                    } catch (NumberFormatException e) {
                        isUpdatingHeight = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Weight slider
        sliderWeight.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                if (!isUpdatingWeight) {
                    isUpdatingWeight = true;
                    etWeight.setText(String.format(Locale.US, "%.1f", value));
                    isUpdatingWeight = false;
                }
            }
        });

        // Weight text input
        etWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdatingWeight && s.length() > 0) {
                    try {
                        isUpdatingWeight = true;
                        float weight = Float.parseFloat(s.toString());
                        if (weight >= 30 && weight <= 150) {
                            sliderWeight.setValue(weight);
                        }
                        isUpdatingWeight = false;
                    } catch (NumberFormatException e) {
                        isUpdatingWeight = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBMI();
            }
        });

        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BMICalculatorActivity.this, BMIHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void calculateBMI() {
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        // Validation
        if (ageStr.isEmpty()) {
            etAge.setError("Vui lòng nhập tuổi");
            etAge.requestFocus();
            return;
        }

        if (heightStr.isEmpty()) {
            etHeight.setError("Vui lòng nhập chiều cao");
            etHeight.requestFocus();
            return;
        }

        if (weightStr.isEmpty()) {
            etWeight.setError("Vui lòng nhập cân nặng");
            etWeight.requestFocus();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float height = Float.parseFloat(heightStr);
            float weight = Float.parseFloat(weightStr);

            if (age < 2 || age > 120) {
                etAge.setError("Tuổi không hợp lệ");
                etAge.requestFocus();
                return;
            }

            if (height < 50 || height > 250) {
                etHeight.setError("Chiều cao không hợp lệ");
                etHeight.requestFocus();
                return;
            }

            if (weight < 10 || weight > 300) {
                etWeight.setError("Cân nặng không hợp lệ");
                etWeight.requestFocus();
                return;
            }

            // Calculate BMI
            float heightInMeters = height / 100;
            float bmi = weight / (heightInMeters * heightInMeters);

            // Get gender
            String gender = rgGender.getCheckedRadioButtonId() == R.id.rb_male ? "Nam" : "Nữ";

            // Save to database
            saveBMIRecord(gender, age, height, weight, bmi);

            // Display result
            displayResult(bmi);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBMIRecord(String gender, int age, float height, float weight, float bmi) {
        BMIRecord record = new BMIRecord(gender, age, height, weight);

        try {
            db.execSQL("INSERT INTO bmi_records (user_email, gender, age, height, weight, bmi, category, timestamp) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{
                            userEmail,
                            gender,
                            age,
                            height,
                            weight,
                            bmi,
                            record.getCategory(),
                            System.currentTimeMillis()
                    });

            Toast.makeText(this, "Đã lưu kết quả BMI", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayResult(float bmi) {
        cardResult.setVisibility(View.VISIBLE);
        tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));

        BMICategory category = getBMICategory(bmi);

        tvBmiCategory.setText(category.getName());
        tvBmiCategory.setTextColor(category.getColor());

        tvBmiDescription.setText(category.getDescription());
        tvAdvice.setText(category.getAdvice());

        // Scroll to result
        cardResult.post(new Runnable() {
            @Override
            public void run() {
                cardResult.requestFocus();
            }
        });
    }

    private BMICategory getBMICategory(float bmi) {
        if (bmi < 18.5) {
            return new BMICategory(
                    "Thiếu cân",
                    0xFF4ECDC4,
                    "Bạn đang thiếu cân. Cân nặng của bạn thấp hơn mức khuyến nghị.",
                    "• Tăng cường chế độ ăn uống dinh dưỡng\n• Ăn nhiều bữa nhỏ trong ngày\n• Tập luyện để tăng cơ bắp\n• Tham khảo ý kiến bác sĩ dinh dưỡng"
            );
        } else if (bmi >= 18.5 && bmi < 25) {
            return new BMICategory(
                    "Bình thường",
                    0xFF4CAF50,
                    "Tuyệt vời! Bạn có cân nặng lý tưởng. Hãy duy trì lối sống lành mạnh.",
                    "• Duy trì chế độ ăn uống cân bằng\n• Tập thể dục đều đặn 30 phút/ngày\n• Uống đủ 2 lít nước mỗi ngày\n• Ngủ đủ giấc 7-8 tiếng/đêm"
            );
        } else if (bmi >= 25 && bmi < 30) {
            return new BMICategory(
                    "Thừa cân",
                    0xFFFFD93D,
                    "Bạn đang thừa cân. Cần chú ý điều chỉnh lối sống để cải thiện sức khỏe.",
                    "• Giảm lượng calo nạp vào\n• Tăng cường hoạt động thể chất\n• Hạn chế đồ ăn nhanh và đồ ngọt\n• Ăn nhiều rau xanh và trái cây"
            );
        } else if (bmi >= 30 && bmi < 35) {
            return new BMICategory(
                    "Béo phì độ I",
                    0xFFFF8C42,
                    "Bạn đang béo phì độ I. Cần có kế hoạch giảm cân nghiêm túc.",
                    "• Tham khảo ý kiến bác sĩ chuyên khoa\n• Xây dựng kế hoạch ăn uống khoa học\n• Tập luyện có hướng dẫn từ HLV\n• Theo dõi cân nặng định kỳ"
            );
        } else {
            return new BMICategory(
                    "Béo phì độ II",
                    0xFFE74C3C,
                    "Bạn đang béo phì độ II. Nguy cơ sức khỏe cao, cần tư vấn y tế ngay.",
                    "• KHẨN CẤP: Tham khảo bác sĩ ngay\n• Có thể cần can thiệp y tế\n• Xét nghiệm sức khỏe toàn diện\n• Theo dõi các bệnh lý liên quan"
            );
        }
    }

    // Inner class for BMI Category
    private static class BMICategory {
        private String name;
        private int color;
        private String description;
        private String advice;

        public BMICategory(String name, int color, String description, String advice) {
            this.name = name;
            this.color = color;
            this.description = description;
            this.advice = advice;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public String getDescription() {
            return description;
        }

        public String getAdvice() {
            return advice;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}