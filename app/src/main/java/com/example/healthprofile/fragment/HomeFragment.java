package com.example.healthprofile.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.BMICalculatorActivity;
import com.example.healthprofile.BookAppointmentActivity;
import com.example.healthprofile.ChallengeDetailActivity;
import com.example.healthprofile.ExerciseTrackerActivity;
import com.example.healthprofile.HealthGaugeView;
import com.example.healthprofile.MyAppointmentsActivity;
import com.example.healthprofile.R;
import com.example.healthprofile.StepCounterActivity;
import com.example.healthprofile.StepTrackerActivity;
import com.example.healthprofile.UnifiedStepActivity;
import com.example.healthprofile.adapter.ChallengeAdapter;
import com.example.healthprofile.model.Challenge;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements ChallengeAdapter.OnChallengeClickListener {

    private RecyclerView rvChallenges;
    private ChallengeAdapter challengeAdapter;
    private List<Challenge> challengeList;
    private Button btnLearnMore;
    private HealthGaugeView healthGauge;

    private SQLiteDatabase db;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Lấy email từ arguments hoặc SharedPreferences
        if (getArguments() != null) {
            userEmail = getArguments().getString("email", "");
        }

        if (userEmail.isEmpty()) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
            userEmail = prefs.getString("email", "");
        }

        // Mở database
        db = getActivity().openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadChallenges();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        rvChallenges = view.findViewById(R.id.rv_challenges);
        btnLearnMore = view.findViewById(R.id.btn_learn_more);
        healthGauge = view.findViewById(R.id.health_gauge_view);

        // Set initial health score
        if (healthGauge != null) {
            healthGauge.setHealthScore(76);
        }
    }

    private void setupRecyclerView() {
        challengeList = new ArrayList<>();
        challengeAdapter = new ChallengeAdapter(getContext(), challengeList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        rvChallenges.setLayoutManager(layoutManager);
        rvChallenges.setAdapter(challengeAdapter);
    }

    private void setupClickListeners(View view) {
        // Xem tất cả thử thách
        view.findViewById(R.id.tv_view_all_challenges_home).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChallengeDetailActivity.class);
            startActivity(intent);
        });

        // Menu Supply
        View menuSupply = view.findViewById(R.id.menu_supply);
        if (menuSupply != null) {
            menuSupply.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), StepTrackerActivity.class);
                startActivity(intent);
            });
        }

        // Menu Exercise
        View menuExercise = view.findViewById(R.id.menu_exercise);
        if (menuExercise != null) {
            menuExercise.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), UnifiedStepActivity.class);
                startActivity(intent);
            });
        }

        // Menu My Health
        View menuMyHealth = view.findViewById(R.id.menu_my_health);
        if (menuMyHealth != null) {
            menuMyHealth.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), BMICalculatorActivity.class);
                startActivity(intent);
            });
        }

        // Menu Consultant
        View menuConsultant = view.findViewById(R.id.menu_consultant);
        if (menuConsultant != null) {
            menuConsultant.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), BookAppointmentActivity.class);
                startActivity(intent);
            });
        }

        // Nút tìm hiểu thêm
        if (btnLearnMore != null) {
            btnLearnMore.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), BMICalculatorActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadChallenges() {
        new LoadChallengesTask().execute();
    }

    private class LoadChallengesTask extends AsyncTask<Void, Void, List<Challenge>> {
        @Override
        protected List<Challenge> doInBackground(Void... voids) {
            List<Challenge> challenges = new ArrayList<>();

            try {
                // Lấy chỉ 3 thử thách đầu tiên đang active, sắp xếp theo số ngày còn lại
                String sql = "SELECT * FROM challenges WHERE status = 'active' ORDER BY end_date ASC LIMIT 3";
                Cursor cursor = db.rawQuery(sql, null);

                if (cursor.moveToFirst()) {
                    do {
                        Challenge challenge = new Challenge();
                        challenge.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                        challenge.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                        challenge.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                        challenge.setStartDate(cursor.getLong(cursor.getColumnIndexOrThrow("start_date")));
                        challenge.setEndDate(cursor.getLong(cursor.getColumnIndexOrThrow("end_date")));
                        challenge.setDurationDays(cursor.getInt(cursor.getColumnIndexOrThrow("duration_days")));
                        challenge.setParticipants(cursor.getInt(cursor.getColumnIndexOrThrow("participants")));
                        challenge.setRewardPoints(cursor.getInt(cursor.getColumnIndexOrThrow("reward_points")));
                        challenge.setImageResource(cursor.getInt(cursor.getColumnIndexOrThrow("image_resource")));
                        challenge.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));

                        // Kiểm tra user đã tham gia chưa
                        boolean isJoined = checkUserJoinedChallenge(challenge.getId());
                        challenge.setJoined(isJoined);

                        challenges.add(challenge);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return challenges;
        }

        @Override
        protected void onPostExecute(List<Challenge> challenges) {
            challengeList.clear();
            challengeList.addAll(challenges);
            challengeAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Kiểm tra user đã tham gia thử thách chưa
     */
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
        // Mở chi tiết thử thách
        Intent intent = new Intent(getContext(), ChallengeDetailActivity.class);
        intent.putExtra("challenge_id", challenge.getId());
        startActivity(intent);
    }

    @Override
    public void onJoinChallenge(Challenge challenge, int position) {
        if (userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để tham gia!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (challenge.isJoined()) {
            Toast.makeText(getContext(), "Bạn đã tham gia thử thách này!", Toast.LENGTH_SHORT).show();
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
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(getContext(), "Tham gia thử thách thành công!",
                        Toast.LENGTH_SHORT).show();

                // Cập nhật trạng thái
                challenge.setJoined(true);
                challenge.setParticipants(challenge.getParticipants() + 1);
                challengeAdapter.notifyItemChanged(position);
            } else {
                Toast.makeText(getContext(), "Lỗi khi tham gia thử thách!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload challenges khi quay lại fragment
        if (db != null && challengeAdapter != null) {
            loadChallenges();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}