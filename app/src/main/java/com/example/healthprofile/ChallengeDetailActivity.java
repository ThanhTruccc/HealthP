package com.example.healthprofile;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.ChallengeAdapter;
import com.example.healthprofile.model.Challenge;

import java.util.ArrayList;
import java.util.List;

public class ChallengeDetailActivity extends AppCompatActivity implements ChallengeAdapter.OnChallengeClickListener {

    private static final String TAG = "ChallengeDetail";

    private RecyclerView rvAllChallenges;
    private ChallengeAdapter adapter;
    private List<Challenge> challengeList;

    private SQLiteDatabase db;
    private String userEmail;
    private ImageButton btnBack;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        // Lấy email user
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        // Mở database
        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        initViews();
        setupRecyclerView();
        loadAllChallenges();
    }

    private void initViews() {
        rvAllChallenges = findViewById(R.id.rv_all_challenges);
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_total_challenges);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        challengeList = new ArrayList<>();
        adapter = new ChallengeAdapter(this, challengeList, this);

        // Dùng GridLayoutManager 1 cột để hiển thị full width
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        rvAllChallenges.setLayoutManager(gridLayoutManager);
        rvAllChallenges.setAdapter(adapter);
    }

    private void loadAllChallenges() {
        new LoadChallengesTask().execute();
    }

    private class LoadChallengesTask extends AsyncTask<Void, Void, List<Challenge>> {
        @Override
        protected List<Challenge> doInBackground(Void... voids) {
            List<Challenge> challenges = new ArrayList<>();

            try {
                // Lấy TẤT CẢ thử thách đang active, sắp xếp theo số ngày còn lại
                String sql = "SELECT * FROM challenges WHERE status = 'active' ORDER BY end_date ASC";
                Cursor cursor = db.rawQuery(sql, null);

                Log.d(TAG, "Total challenges found: " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    do {
                        try {
                            Challenge challenge = new Challenge();
                            challenge.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                            challenge.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));

                            int descIndex = cursor.getColumnIndex("description");
                            if (descIndex >= 0 && !cursor.isNull(descIndex)) {
                                challenge.setDescription(cursor.getString(descIndex));
                            }

                            int startDateIndex = cursor.getColumnIndex("start_date");
                            if (startDateIndex >= 0 && !cursor.isNull(startDateIndex)) {
                                challenge.setStartDate(cursor.getLong(startDateIndex));
                            }

                            int endDateIndex = cursor.getColumnIndex("end_date");
                            if (endDateIndex >= 0 && !cursor.isNull(endDateIndex)) {
                                challenge.setEndDate(cursor.getLong(endDateIndex));
                            }

                            int durationIndex = cursor.getColumnIndex("duration_days");
                            if (durationIndex >= 0 && !cursor.isNull(durationIndex)) {
                                challenge.setDurationDays(cursor.getInt(durationIndex));
                            }

                            int participantsIndex = cursor.getColumnIndex("participants");
                            if (participantsIndex >= 0 && !cursor.isNull(participantsIndex)) {
                                challenge.setParticipants(cursor.getInt(participantsIndex));
                            }

                            int rewardIndex = cursor.getColumnIndex("reward_points");
                            if (rewardIndex >= 0 && !cursor.isNull(rewardIndex)) {
                                challenge.setRewardPoints(cursor.getInt(rewardIndex));
                            }

                            // FIXED: Load cả image_resource và image_path
                            int imageResourceIndex = cursor.getColumnIndex("image_resource");
                            if (imageResourceIndex >= 0 && !cursor.isNull(imageResourceIndex)) {
                                int imageResource = cursor.getInt(imageResourceIndex);
                                if (imageResource > 0) {
                                    challenge.setImageResource(imageResource);
                                }
                            }

                            int imagePathIndex = cursor.getColumnIndex("image_path");
                            if (imagePathIndex >= 0 && !cursor.isNull(imagePathIndex)) {
                                String imagePath = cursor.getString(imagePathIndex);
                                if (imagePath != null && !imagePath.isEmpty()) {
                                    challenge.setImagePath(imagePath);
                                }
                            }

                            int statusIndex = cursor.getColumnIndex("status");
                            if (statusIndex >= 0 && !cursor.isNull(statusIndex)) {
                                challenge.setStatus(cursor.getString(statusIndex));
                            }

                            // Kiểm tra user đã tham gia chưa
                            boolean isJoined = checkUserJoinedChallenge(challenge.getId());
                            challenge.setJoined(isJoined);

                            challenges.add(challenge);
                            Log.d(TAG, "Loaded: " + challenge.getTitle());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing challenge: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (Exception e) {
                Log.e(TAG, "Error loading challenges: " + e.getMessage());
                e.printStackTrace();
            }

            return challenges;
        }

        @Override
        protected void onPostExecute(List<Challenge> challenges) {
            Log.d(TAG, "Loaded " + challenges.size() + " challenges");
            challengeList.clear();
            challengeList.addAll(challenges);
            adapter.notifyDataSetChanged();

            // Cập nhật tiêu đề
            if (tvTitle != null) {
                tvTitle.setText("Tất cả thử thách (" + challenges.size() + ")");
            }
        }
    }

    private boolean checkUserJoinedChallenge(int challengeId) {
        if (userEmail.isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM user_challenges WHERE user_email = ? AND challenge_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{userEmail, String.valueOf(challengeId)});

        boolean isJoined = false;
        if (cursor.moveToFirst()) {
            isJoined = cursor.getInt(0) > 0;
        }
        cursor.close();

        return isJoined;
    }

    @Override
    public void onChallengeClick(Challenge challenge) {
        // Hiển thị thông tin chi tiết
        showChallengeDetailDialog(challenge);
    }

    @Override
    public void onJoinChallenge(Challenge challenge, int position) {
        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để tham gia!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (challenge.isJoined()) {
            Toast.makeText(this, "Bạn đã tham gia thử thách này!", Toast.LENGTH_SHORT).show();
            return;
        }

        new JoinChallengeTask(challenge, position).execute();
    }

    private class JoinChallengeTask extends AsyncTask<Void, Void, Boolean> {
        private Challenge challenge;
        private int position;

        JoinChallengeTask(Challenge challenge, int position) {
            this.challenge = challenge;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                long currentTime = System.currentTimeMillis();

                // Thêm vào bảng user_challenges
                String sql = "INSERT INTO user_challenges (user_email, challenge_id, challenge_title, joined_date, status, points_earned) " +
                        "VALUES (?, ?, ?, ?, 'ongoing', 0)";
                db.execSQL(sql, new Object[]{userEmail, challenge.getId(), challenge.getTitle(), currentTime});

                // Tăng số người tham gia
                db.execSQL("UPDATE challenges SET participants = participants + 1 WHERE id = ?",
                        new Object[]{challenge.getId()});

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error joining challenge: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ChallengeDetailActivity.this,
                        "Tham gia thử thách thành công!",
                        Toast.LENGTH_SHORT).show();

                // Cập nhật trạng thái
                challenge.setJoined(true);
                challenge.setParticipants(challenge.getParticipants() + 1);
                adapter.notifyItemChanged(position);
            } else {
                Toast.makeText(ChallengeDetailActivity.this,
                        "Lỗi khi tham gia thử thách!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showChallengeDetailDialog(Challenge challenge) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(challenge.getTitle());

        String message = "Mô tả: " + challenge.getDescription() + "\n\n" +
                "Người tham gia: " + challenge.getParticipants() + " người\n" +
                "Thời gian: " + challenge.getDurationDays() + " ngày\n" +
                "Còn lại: " + challenge.getDaysLeft() + " ngày\n" +
                "Phần thưởng: +" + challenge.getRewardPoints() + " điểm\n" +
                "Trạng thái: " + (challenge.isJoined() ? "Đã tham gia" : "Chưa tham gia");

        builder.setMessage(message);
        builder.setPositiveButton("Đóng", null);
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