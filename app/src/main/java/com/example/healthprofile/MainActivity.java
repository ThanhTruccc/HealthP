package com.example.healthprofile;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    Button btnLogin, btnRegister;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        createTables();
        insertSampleData();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register_1_Activity.class);
                startActivity(intent);
            }
        });
    }

    private void createTables() {
        db.execSQL("CREATE TABLE IF NOT EXISTS user (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "password TEXT NOT NULL, " +
                "fullName TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT, " +
                "role TEXT DEFAULT 'user')");

        db.execSQL("CREATE TABLE IF NOT EXISTS appointments (" +
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
                "fee INTEGER DEFAULT 200000)");

        db.execSQL("CREATE TABLE IF NOT EXISTS doctors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "degree TEXT, " +
                "specialty TEXT, " +
                "workplace TEXT, " +
                "rating REAL DEFAULT 0.0, " +
                "experience INTEGER DEFAULT 0, " +
                "image_resource INTEGER, " +
                "image_path TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS health_profiles (" +
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
                "last_updated INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS reward_points (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "points INTEGER NOT NULL, " +
                "actionn TEXT NOT NULL, " +
                "points_change INTEGER NOT NULL, " +
                "description TEXT, " +
                "timestamp INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS vouchers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "points_required INTEGER NOT NULL, " +
                "discount_percent INTEGER NOT NULL, " +
                "category TEXT, " +
                "image_resource INTEGER, " +
                "is_available INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE IF NOT EXISTS redeemed_vouchers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "voucher_id INTEGER NOT NULL, " +
                "voucher_title TEXT NOT NULL, " +
                "points_used INTEGER NOT NULL, " +
                "voucher_code TEXT NOT NULL, " +
                "redeemed_date INTEGER NOT NULL, " +
                "status TEXT DEFAULT 'active')");

        // Bảng challenges (Thử thách)
        db.execSQL("CREATE TABLE IF NOT EXISTS challenges (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "start_date INTEGER NOT NULL, " +
                "end_date INTEGER NOT NULL, " +
                "duration_days INTEGER NOT NULL, " +
                "participants INTEGER DEFAULT 0, " +
                "reward_points INTEGER DEFAULT 50, " +
                "image_resource INTEGER, " +
                "status TEXT DEFAULT 'active')");

        // Bảng user_challenges (Lịch sử tham gia thử thách - dùng cho HomeFragment)
        db.execSQL("CREATE TABLE IF NOT EXISTS user_challenges (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "challenge_id INTEGER NOT NULL, " +
                "challenge_title TEXT NOT NULL, " +
                "joined_date INTEGER NOT NULL, " +
                "status TEXT DEFAULT 'ongoing', " +
                "completed_date INTEGER, " +
                "points_earned INTEGER DEFAULT 0, " +
                "FOREIGN KEY (challenge_id) REFERENCES challenges(id))");

        // Bảng challenge_participants (Danh sách người tham gia - dùng cho ChallengeAdapter)
        db.execSQL("CREATE TABLE IF NOT EXISTS challenge_participants (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "challenge_id INTEGER NOT NULL, " +
                "challenge_title TEXT NOT NULL, " +
                "joined_date INTEGER NOT NULL, " +
                "status TEXT DEFAULT 'active', " +
                "completed_date INTEGER, " +
                "points_earned INTEGER DEFAULT 0, " +
                "UNIQUE(user_email, challenge_id))");

        // Bảng medication_reminders (Nhắc nhở uống thuốc)
        db.execSQL("CREATE TABLE IF NOT EXISTS medication_reminders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "medication_name TEXT NOT NULL, " +
                "dosage TEXT, " +
                "frequency TEXT NOT NULL, " +
                "time1 TEXT, " +
                "time2 TEXT, " +
                "time3 TEXT, " +
                "start_date TEXT, " +
                "end_date TEXT, " +
                "notes TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "created_at INTEGER, " +
                "updated_at INTEGER)");
    }

    private void insertSampleData() {
        // Insert sample users
        Cursor check = db.rawQuery("SELECT COUNT(*) FROM user", null);
        if (check.moveToFirst() && check.getInt(0) == 0) {
            db.execSQL("INSERT INTO user (password, fullName, email, phone, role) VALUES " +
                    "('admin123', 'Quản Trị Viên', 'admin@health.com', '0900000000', 'admin')");
            db.execSQL("INSERT INTO user (password, fullName, email, phone, role) VALUES " +
                    "('user123', 'Lê Thành An', 'thanhan@gmail.com', '0901234567', 'user')");
            db.execSQL("INSERT INTO user (password, fullName, email, phone, role) VALUES " +
                    "('user123', 'Trần Quý Nhân', 'tranquynhan@gmail.com', '0912345678', 'user')");
            db.execSQL("INSERT INTO user (password, fullName, email, phone, role) VALUES " +
                    "('user123', 'Nguyễn Ngọc Huy', 'huyngoc@gmail.com', '0923456789', 'user')");
            db.execSQL("INSERT INTO user (password, fullName, email, phone, role) VALUES " +
                    "('user123', 'Phạm Thị Dung', 'phamthidung@gmail.com', '0934567890', 'user')");
        }
        check.close();

        // Insert sample doctors
        int doctor1 = R.drawable.doctor_1;
        int doctor2 = R.drawable.doctor_2;
        int doctor3 = R.drawable.doctor_3;
        int doctor4 = R.drawable.doctor_4;
        int doctor5 = R.drawable.doctor_5;

        Cursor checkDoctors = db.rawQuery("SELECT COUNT(*) FROM doctors", null);
        if (checkDoctors.moveToFirst() && checkDoctors.getInt(0) == 0) {
            db.execSQL("INSERT INTO doctors (name, degree, specialty, workplace, rating, experience, image_resource, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Phạm Thị Xuân Mai", "PGS.TS.", "Nội khoa", "Bệnh viện Chợ Rẫy", 4.8, 12, doctor1, null});
            db.execSQL("INSERT INTO doctors (name, degree, specialty, workplace, rating, experience, image_resource, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Nguyễn Văn Hoàng", "TS.BS.", "Tai mũi họng", "Bệnh viện Đại học Y Dược TP.HCM", 4.5, 8, doctor2, null});
            db.execSQL("INSERT INTO doctors (name, degree, specialty, workplace, rating, experience, image_resource, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Lê Thị Thu Hà", "ThS.BS.", "Nhi khoa", "Bệnh viện Nhi Đồng 1", 4.7, 10, doctor3, null});
            db.execSQL("INSERT INTO doctors (name, degree, specialty, workplace, rating, experience, image_resource, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Lê Quang Huy", "ThS.BS.", "Tim mạch", "Bệnh viện Tim Tâm Đức", 4.7, 10, doctor4, null});
            db.execSQL("INSERT INTO doctors (name, degree, specialty, workplace, rating, experience, image_resource, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Phạm Thị Xuân Vy", "PGS.TS.", "Ngoại khoa", "Bệnh viện 115", 4.8, 12, doctor5, null});
        }
        checkDoctors.close();

        // Insert sample vouchers
        Cursor checkVoucher = db.rawQuery("SELECT COUNT(*) FROM vouchers", null);
        if (checkVoucher.moveToFirst() && checkVoucher.getInt(0) == 0) {
            db.execSQL("INSERT INTO vouchers (title, description, points_required, discount_percent, category) VALUES " +
                    "('Giảm 10%', 'Voucher giảm 10% cho dịch vụ khám tổng quát', 100, 10, 'Sức khỏe')");
            db.execSQL("INSERT INTO vouchers (title, description, points_required, discount_percent, category) VALUES " +
                    "('Giảm 20%', 'Voucher giảm 20% cho xét nghiệm máu', 200, 20, 'Xét nghiệm')");
            db.execSQL("INSERT INTO vouchers (title, description, points_required, discount_percent, category) VALUES " +
                    "('Miễn phí 1 buổi tư vấn', 'Voucher miễn phí 1 lần tư vấn dinh dưỡng', 300, 100, 'Dinh dưỡng')");
        }
        checkVoucher.close();

        // Insert sample challenges với thời gian thực tế
        Cursor checkChallenges = db.rawQuery("SELECT COUNT(*) FROM challenges", null);
        if (checkChallenges.moveToFirst() && checkChallenges.getInt(0) == 0) {
            long currentTime = System.currentTimeMillis();
            long dayInMillis = 24 * 60 * 60 * 1000L;

            // Thử thách 1: Hiến máu nhân đạo (bắt đầu hôm nay, kết thúc sau 30 ngày)
            long startDate1 = currentTime;
            long endDate1 = currentTime + (30 * dayInMillis);
            db.execSQL("INSERT INTO challenges (title, description, start_date, end_date, duration_days, participants, reward_points, image_resource, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Hiến máu nhân đạo", "Tham gia hiến máu cứu người, mỗi đơn vị máu là một cơ hội sống cho người khác",
                            startDate1, endDate1, 30, 245, 100, R.drawable.challenge_blood, "active"});

            // Thử thách 2: Một giờ không rác (bắt đầu hôm nay, kết thúc sau 15 ngày)
            long startDate2 = currentTime;
            long endDate2 = currentTime + (15 * dayInMillis);
            db.execSQL("INSERT INTO challenges (title, description, start_date, end_date, duration_days, participants, reward_points, image_resource, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Một giờ không rác", "Mỗi ngày dành 1 giờ thu gom rác, bảo vệ môi trường sống xanh sạch đẹp",
                            startDate2, endDate2, 15, 532, 50, R.drawable.challenge_clean, "active"});

            // Thử thách 3: Chia sẻ yêu thương (bắt đầu hôm nay, kết thúc sau 21 ngày)
            long startDate3 = currentTime;
            long endDate3 = currentTime + (21 * dayInMillis);
            db.execSQL("INSERT INTO challenges (title, description, start_date, end_date, duration_days, participants, reward_points, image_resource, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Chia sẻ yêu thương", "Chia sẻ bữa ăn, quần áo, học phí cho người có hoàn cảnh khó khăn",
                            startDate3, endDate3, 21, 189, 75, R.drawable.challenge_share, "active"});

            // Thử thách 4: 10,000 bước mỗi ngày (bắt đầu hôm nay, kết thúc sau 30 ngày)
            long startDate4 = currentTime;
            long endDate4 = currentTime + (30 * dayInMillis);
            db.execSQL("INSERT INTO challenges (title, description, start_date, end_date, duration_days, participants, reward_points, image_resource, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"10,000 bước mỗi ngày", "Thử thách đi bộ 10,000 bước mỗi ngày để cải thiện sức khỏe",
                            startDate4, endDate4, 30, 876, 80, R.drawable.challenge_walk, "active"});

            // Thử thách 5: Uống đủ nước (bắt đầu hôm nay, kết thúc sau 14 ngày)
            long startDate5 = currentTime;
            long endDate5 = currentTime + (14 * dayInMillis);
            db.execSQL("INSERT INTO challenges (title, description, start_date, end_date, duration_days, participants, reward_points, image_resource, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Uống đủ 2 lít nước mỗi ngày", "Duy trì uống đủ 2 lít nước mỗi ngày để cơ thể khỏe mạnh",
                            startDate5, endDate5, 14, 1024, 60, R.drawable.challenge_water, "active"});

            // Thử thách 6: Thiền 15 phút mỗi ngày (bắt đầu hôm nay, kết thúc sau 21 ngày)
            long startDate6 = currentTime;
            long endDate6 = currentTime + (21 * dayInMillis);
            db.execSQL("INSERT INTO challenges (title, description, start_date, end_date, duration_days, participants, reward_points, image_resource, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{"Thiền 15 phút mỗi ngày", "Thực hành thiền định 15 phút mỗi ngày để giảm stress và cải thiện tinh thần",
                            startDate6, endDate6, 21, 456, 70, R.drawable.challenge_meditation, "active"});
        }
        checkChallenges.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}