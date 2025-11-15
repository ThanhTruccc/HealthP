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

import com.example.healthprofile.adapter.AdminChallengeAdapter;
import com.example.healthprofile.model.Challenge;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminChallengeManagementActivity extends AppCompatActivity
        implements AdminChallengeAdapter.OnChallengeActionListener {

    private static final String TAG = "AdminChallengeMgmt";
    private static final int PICK_IMAGE_REQUEST = 1002;

    private RecyclerView recyclerView;
    private AdminChallengeAdapter adapter;
    private List<Challenge> challengeList;
    private List<Challenge> filteredList;

    private EditText edtSearch;
    private LinearLayout tvEmpty;
    private ProgressBar progressBar;
    private TextView tvTotalChallenges;
    private ImageButton btnBack;
    private FloatingActionButton fabAddChallenge;

    private SQLiteDatabase db;

    private ImageView imgPreviewChallenge;
    private String selectedImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_challenge_management);

        db = openOrCreateDatabase("health_profile.db", MODE_PRIVATE, null);

        initViews();
        setupRecyclerView();
        setupSearch();
        loadChallenges();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_challenges_admin);
        edtSearch = findViewById(R.id.edt_search_challenge);
        tvEmpty = findViewById(R.id.tv_empty_challenges);
        progressBar = findViewById(R.id.progress_bar_challenges);
        tvTotalChallenges = findViewById(R.id.tv_total_challenges);
        btnBack = findViewById(R.id.btn_back_challenges);
        fabAddChallenge = findViewById(R.id.fab_add_challenge);

        btnBack.setOnClickListener(v -> finish());
        fabAddChallenge.setOnClickListener(v -> showAddChallengeDialog());
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        challengeList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AdminChallengeAdapter(this, filteredList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChallenges(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterChallenges(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(challengeList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Challenge challenge : challengeList) {
                String title = challenge.getTitle() != null ? challenge.getTitle().toLowerCase() : "";
                String description = challenge.getDescription() != null ? challenge.getDescription().toLowerCase() : "";

                if (title.contains(lowerQuery) || description.contains(lowerQuery)) {
                    filteredList.add(challenge);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void loadChallenges() {
        new LoadChallengesTask().execute();
    }

    private class LoadChallengesTask extends AsyncTask<Void, Void, List<Challenge>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Challenge> doInBackground(Void... voids) {
            List<Challenge> challenges = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            try {
                String sql = "SELECT * FROM challenges ORDER BY id DESC";
                Cursor cursor = db.rawQuery(sql, null);

                Log.d(TAG, "Total challenges: " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    do {
                        try {
                            Challenge challenge = new Challenge();
                            challenge.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                            challenge.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));

                            int descIndex = cursor.getColumnIndex("description");
                            if (descIndex >= 0) {
                                challenge.setDescription(cursor.getString(descIndex));
                            }

                            int participantsIndex = cursor.getColumnIndex("participants");
                            if (participantsIndex >= 0) {
                                challenge.setParticipants(cursor.getInt(participantsIndex));
                            }

                            // Load start_date và end_date (có thể là Long hoặc String)
                            int startDateIndex = cursor.getColumnIndex("start_date");
                            int endDateIndex = cursor.getColumnIndex("end_date");

                            if (startDateIndex >= 0 && endDateIndex >= 0) {
                                try {
                                    // Thử load dạng timestamp
                                    long startTimestamp = cursor.getLong(startDateIndex);
                                    long endTimestamp = cursor.getLong(endDateIndex);

                                    challenge.setStartDate(startTimestamp);
                                    challenge.setEndDate(endTimestamp);

                                    // Convert to string for display
                                    challenge.setStartDate(sdf.format(new Date(startTimestamp)));
                                    challenge.setEndDate(sdf.format(new Date(endTimestamp)));
                                } catch (Exception e) {
                                    // Nếu không phải timestamp, thử load dạng string
                                    String startStr = cursor.getString(startDateIndex);
                                    String endStr = cursor.getString(endDateIndex);
                                    challenge.setStartDate(startStr);
                                    challenge.setEndDate(endStr);
                                }
                            }

                            int durationIndex = cursor.getColumnIndex("duration_days");
                            if (durationIndex >= 0) {
                                challenge.setDurationDays(cursor.getInt(durationIndex));
                            }

                            int rewardIndex = cursor.getColumnIndex("reward_points");
                            if (rewardIndex >= 0) {
                                challenge.setRewardPoints(cursor.getInt(rewardIndex));
                            }

                            int imageResourceIndex = cursor.getColumnIndex("image_resource");
                            if (imageResourceIndex >= 0) {
                                challenge.setImageResource(cursor.getInt(imageResourceIndex));
                            }

                            int imagePathIndex = cursor.getColumnIndex("image_path");
                            if (imagePathIndex >= 0 && !cursor.isNull(imagePathIndex)) {
                                challenge.setImagePath(cursor.getString(imagePathIndex));
                            }

                            // Load status
                            int statusIndex = cursor.getColumnIndex("status");
                            if (statusIndex >= 0) {
                                String statusStr = cursor.getString(statusIndex);
                                // Convert string status to int for admin UI
                                int statusInt = 1; // default active
                                if ("upcoming".equals(statusStr)) {
                                    statusInt = 0;
                                } else if ("active".equals(statusStr)) {
                                    statusInt = 1;
                                } else if ("completed".equals(statusStr)) {
                                    statusInt = 2;
                                }
                                challenge.setStatus(statusInt);
                                challenge.setStatus(statusStr);
                            }

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
            progressBar.setVisibility(View.GONE);

            challengeList.clear();
            challengeList.addAll(challenges);

            filteredList.clear();
            filteredList.addAll(challenges);

            adapter.notifyDataSetChanged();

            tvTotalChallenges.setText("Tổng: " + challengeList.size() + " thử thách");
            updateEmptyState();
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
    public void onEditChallenge(Challenge challenge, int position) {
        Toast.makeText(this, "Chức năng edit đang phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteChallenge(Challenge challenge, int position) {
        showDeleteConfirmDialog(challenge);
    }

    @Override
    public void onViewChallengeDetail(Challenge challenge) {
        showChallengeDetailDialog(challenge);
    }

    private void showAddChallengeDialog() {
        Toast.makeText(this, "Thử thách đã được thêm sẵn trong MainActivity. Vui lòng restart app để xem!",
                Toast.LENGTH_LONG).show();
    }

    private void showDeleteConfirmDialog(Challenge challenge) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa thử thách:\n" + challenge.getTitle() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new DeleteChallengeTask(challenge).execute();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private class DeleteChallengeTask extends AsyncTask<Void, Void, Boolean> {
        private Challenge challenge;

        DeleteChallengeTask(Challenge challenge) {
            this.challenge = challenge;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (challenge.getImagePath() != null && !challenge.getImagePath().isEmpty()) {
                    File imageFile = new File(challenge.getImagePath());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }

                db.execSQL("DELETE FROM challenges WHERE id = ?", new Object[]{challenge.getId()});
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
                Toast.makeText(AdminChallengeManagementActivity.this,
                        "Đã xóa thử thách!", Toast.LENGTH_SHORT).show();
                loadChallenges();
            } else {
                Toast.makeText(AdminChallengeManagementActivity.this,
                        "Lỗi khi xóa!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showChallengeDetailDialog(Challenge challenge) {
        String details = "THÔNG TIN CHI TIẾT\n\n" +
                "ID: #" + challenge.getId() + "\n" +
                "Tiêu đề: " + challenge.getTitle() + "\n" +
                "Mô tả: " + (challenge.getDescription() != null ? challenge.getDescription() : "Chưa có") + "\n" +
                "Người tham gia: " + challenge.getParticipants() + "\n" +
                "Thời gian: " + challenge.getDuration() + "\n" +
                "Phần thưởng: +" + challenge.getRewardPoints() + " điểm\n" +
                "Trạng thái: " + challenge.getStatusText();

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết thử thách")
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