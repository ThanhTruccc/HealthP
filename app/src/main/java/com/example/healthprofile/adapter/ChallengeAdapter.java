package com.example.healthprofile.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.Challenge;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ViewHolder> {

    private static final String TAG = "ChallengeAdapter";

    private Context context;
    private List<Challenge> challenges;
    private OnChallengeClickListener listener;

    // Database
    private SQLiteDatabase db;
    private ExecutorService executorService;
    private Handler mainHandler;

    public interface OnChallengeClickListener {
        void onChallengeClick(Challenge challenge);
        void onJoinChallenge(Challenge challenge, int position);
    }

    public ChallengeAdapter(Context context, List<Challenge> challenges, OnChallengeClickListener listener) {
        this.context = context;
        this.challenges = challenges;
        this.listener = listener;

        // Mở database
        this.db = context.openOrCreateDatabase("health_profile.db", Context.MODE_PRIVATE, null);

        // Khởi tạo ExecutorService
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_challenge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Challenge challenge = challenges.get(position);

        // Title
        holder.tvTitle.setText(challenge.getTitle());

        // Participants
        holder.tvParticipants.setText(challenge.getParticipantsText());

        // Duration - Hiển thị start và end date
        String duration = "";
        if (challenge.getStartDateStr() != null && challenge.getEndDateStr() != null
                && !challenge.getStartDateStr().isEmpty() && !challenge.getEndDateStr().isEmpty()) {
            duration = challenge.getStartDateStr() + " - " + challenge.getEndDateStr();
        } else {
            duration = challenge.getDurationText();
        }
        holder.tvDuration.setText(duration);

        // Reward Points
        int rewardPoints = challenge.getRewardPoints() > 0 ? challenge.getRewardPoints() : 50;
        holder.tvReward.setText("+" + rewardPoints + " điểm");

        // Days Left
        int daysLeft = calculateDaysLeft(challenge);
        Log.d(TAG, "Challenge: " + challenge.getTitle() + ", Days left: " + daysLeft);

        if (daysLeft <= 0) {
            holder.tvDaysLeft.setText("Đã kết thúc");
            holder.tvDaysLeft.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvDaysLeft.setText("Còn " + daysLeft + " ngày");
            holder.tvDaysLeft.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }

        // Image
        if (challenge.getImagePath() != null && !challenge.getImagePath().isEmpty()) {
            File imageFile = new File(challenge.getImagePath());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(challenge.getImagePath());
                holder.imgChallenge.setImageBitmap(bitmap);
            } else {
                holder.imgChallenge.setImageResource(challenge.getImageResource() != 0
                        ? challenge.getImageResource() : R.drawable.challenge_1);
            }
        } else {
            holder.imgChallenge.setImageResource(challenge.getImageResource() != 0
                    ? challenge.getImageResource() : R.drawable.challenge_1);
        }

        // Lấy email user
        SharedPreferences prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("email", "");

        // Kiểm tra user đã tham gia chưa
        if (!userEmail.isEmpty()) {
            checkUserJoined(challenge.getId(), userEmail, holder.btnJoin, position, daysLeft);
        } else {
            // Nếu chưa đăng nhập, vẫn cho phép click nút
            updateButtonState(holder.btnJoin, false, daysLeft);
        }

        // Click vào card để xem chi tiết
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChallengeClick(challenge);
            }
        });

        // Click nút tham gia
        holder.btnJoin.setOnClickListener(v -> {
            Log.d(TAG, "Button clicked! User: " + userEmail + ", Joined: " + challenge.isJoined() + ", Days left: " + daysLeft);

            if (userEmail.isEmpty()) {
                Toast.makeText(context, "Vui lòng đăng nhập để tham gia!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (daysLeft <= 0) {
                Toast.makeText(context, "Thử thách đã kết thúc!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (challenge.isJoined()) {
                Toast.makeText(context, "Bạn đã tham gia thử thách này!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tham gia thử thách
            Log.d(TAG, "Attempting to join challenge...");
            joinChallenge(challenge, userEmail, holder.btnJoin, position);
        });
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    /**
     * Tính số ngày còn lại
     */
    private int calculateDaysLeft(Challenge challenge) {
        try {
            long endDate = challenge.getEndDate();

            // Nếu có endDate timestamp
            if (endDate > 0) {
                long currentTime = System.currentTimeMillis();
                long timeLeft = endDate - currentTime;

                if (timeLeft < 0) {
                    return 0;
                }

                int days = (int) (timeLeft / (24 * 60 * 60 * 1000L));
                Log.d(TAG, "Days calculated from timestamp: " + days);
                return days;
            }

            // Nếu có endDateStr (format dd/MM/yyyy)
            if (challenge.getEndDateStr() != null && !challenge.getEndDateStr().isEmpty()) {
                String[] parts = challenge.getEndDateStr().split("/");
                if (parts.length == 3) {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1; // Month is 0-based
                    int year = Integer.parseInt(parts[2]);

                    java.util.Calendar endCal = java.util.Calendar.getInstance();
                    endCal.set(year, month, day, 23, 59, 59);

                    java.util.Calendar nowCal = java.util.Calendar.getInstance();

                    long diffMillis = endCal.getTimeInMillis() - nowCal.getTimeInMillis();
                    int days = (int) (diffMillis / (24 * 60 * 60 * 1000L));
                    Log.d(TAG, "Days calculated from string: " + days);
                    return Math.max(0, days);
                }
            }

            // Fallback: Sử dụng getDaysLeft() của Challenge model
            int days = challenge.getDaysLeft();
            Log.d(TAG, "Days from model: " + days);
            return days;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating days left: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Kiểm tra user đã tham gia thử thách chưa (background thread)
     */
    private void checkUserJoined(int challengeId, String userEmail, Button btnJoin, int position, int daysLeft) {
        executorService.execute(() -> {
            boolean joined = isUserJoined(challengeId, userEmail);

            mainHandler.post(() -> {
                if (position < challenges.size()) {
                    Challenge challenge = challenges.get(position);
                    challenge.setJoined(joined);
                    updateButtonState(btnJoin, joined, daysLeft);
                }
            });
        });
    }

    private boolean isUserJoined(int challengeId, String userEmail) {
        try {
            String sql = "SELECT COUNT(*) FROM challenge_participants " +
                    "WHERE challenge_id = " + challengeId + " " +
                    "AND user_email = '" + escapeString(userEmail) + "' " +
                    "AND status = 'active'";
            Cursor cursor = db.rawQuery(sql, null);

            boolean joined = false;
            if (cursor.moveToFirst()) {
                joined = cursor.getInt(0) > 0;
            }
            cursor.close();

            Log.d(TAG, "User joined check: " + joined + " for challenge " + challengeId);
            return joined;
        } catch (Exception e) {
            Log.e(TAG, "Error checking user joined: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật trạng thái button
     */
    private void updateButtonState(Button button, boolean joined, int daysLeft) {
        if (joined) {
            button.setText("Đã tham gia");
            button.setEnabled(false);
            button.setBackgroundResource(R.drawable.btn_gray_background);
        } else if (daysLeft <= 0) {
            button.setText("Đã kết thúc");
            button.setEnabled(false);
            button.setBackgroundResource(R.drawable.btn_gray_background);
        } else {
            button.setText("Tham gia");
            button.setEnabled(true);
            button.setBackgroundResource(R.drawable.btn_primary_background);
            button.setClickable(true);
        }

        Log.d(TAG, "Button state - Joined: " + joined + ", Days: " + daysLeft + ", Enabled: " + button.isEnabled());
    }

    /**
     * Tham gia thử thách (background thread)
     */
    private void joinChallenge(Challenge challenge, String userEmail, Button btnJoin, int position) {
        // Disable button ngay lập tức để tránh double click
        btnJoin.setEnabled(false);

        executorService.execute(() -> {
            boolean success = insertParticipant(challenge, userEmail);

            mainHandler.post(() -> {
                if (success) {
                    Toast.makeText(context, "Tham gia thành công! +50 điểm",
                            Toast.LENGTH_SHORT).show();

                    // Cập nhật UI
                    if (position < challenges.size()) {
                        Challenge c = challenges.get(position);
                        c.setJoined(true);
                        c.setParticipants(c.getParticipants() + 1);
                        updateButtonState(btnJoin, true, calculateDaysLeft(c));
                        notifyItemChanged(position);
                    }
                } else {
                    Toast.makeText(context, "Bạn đã tham gia thử thách này rồi!",
                            Toast.LENGTH_SHORT).show();
                    btnJoin.setEnabled(true);
                }
            });
        });
    }

    private boolean insertParticipant(Challenge challenge, String userEmail) {
        try {
            // Kiểm tra xem đã tham gia chưa
            if (isUserJoined(challenge.getId(), userEmail)) {
                Log.d(TAG, "User already joined");
                return false;
            }

            // Thêm vào bảng challenge_participants
            long timestamp = System.currentTimeMillis();
            String insertSql = "INSERT INTO challenge_participants " +
                    "(user_email, challenge_id, challenge_title, joined_date, status) " +
                    "VALUES ('" + escapeString(userEmail) + "', " +
                    challenge.getId() + ", " +
                    "'" + escapeString(challenge.getTitle()) + "', " +
                    timestamp + ", 'active')";
            db.execSQL(insertSql);

            // Cập nhật số lượng participants
            String updateSql = "UPDATE challenges SET participants = participants + 1 " +
                    "WHERE id = " + challenge.getId();
            db.execSQL(updateSql);

            // THÊM ĐIỂM THƯỞNG - 50 điểm khi tham gia thử thách
            int rewardPoints = challenge.getRewardPoints() > 0 ? challenge.getRewardPoints() : 50;
            addRewardPoints(userEmail, "join_challenge", rewardPoints,
                    "Tham gia thử thách: " + challenge.getTitle());

            Log.d(TAG, "Successfully joined challenge and added " + rewardPoints + " points");

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error joining challenge: " + e.getMessage());
            e.printStackTrace();
            return false;
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

            Log.d(TAG, "Reward points added successfully");
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

    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imgChallenge;
        TextView tvTitle, tvParticipants, tvDuration, tvReward, tvDaysLeft;
        Button btnJoin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_challenge);
            imgChallenge = itemView.findViewById(R.id.img_challenge);
            tvTitle = itemView.findViewById(R.id.tv_challenge_title);
            tvParticipants = itemView.findViewById(R.id.tv_participants);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvReward = itemView.findViewById(R.id.tv_reward);
            tvDaysLeft = itemView.findViewById(R.id.tv_days_left);
            btnJoin = itemView.findViewById(R.id.btn_join_challenge);
        }
    }
}