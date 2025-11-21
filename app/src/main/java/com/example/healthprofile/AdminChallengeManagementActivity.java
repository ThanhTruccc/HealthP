package com.example.healthprofile;

import android.app.DatePickerDialog;
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
import java.util.Calendar;
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
    private Challenge editingChallenge = null;

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

                            int startDateIndex = cursor.getColumnIndex("start_date");
                            int endDateIndex = cursor.getColumnIndex("end_date");

                            if (startDateIndex >= 0 && endDateIndex >= 0) {
                                try {
                                    long startTimestamp = cursor.getLong(startDateIndex);
                                    long endTimestamp = cursor.getLong(endDateIndex);

                                    challenge.setStartDate(startTimestamp);
                                    challenge.setEndDate(endTimestamp);

                                    challenge.setStartDate(sdf.format(new Date(startTimestamp)));
                                    challenge.setEndDate(sdf.format(new Date(endTimestamp)));
                                } catch (Exception e) {
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

                            int statusIndex = cursor.getColumnIndex("status");
                            if (statusIndex >= 0) {
                                String statusStr = cursor.getString(statusIndex);
                                int statusInt = 1;
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

            tvTotalChallenges.setText("Tổng: " + challengeList.size());
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
        editingChallenge = challenge;
        selectedImagePath = challenge.getImagePath();
        showAddEditDialog(challenge);
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
        editingChallenge = null;
        selectedImagePath = null;
        showAddEditDialog(null);
    }

    private void showAddEditDialog(Challenge challenge) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_challenge, null);

        EditText edtTitle = dialogView.findViewById(R.id.edt_challenge_title);
        EditText edtDescription = dialogView.findViewById(R.id.edt_challenge_description);
        EditText edtStartDate = dialogView.findViewById(R.id.edt_start_date);
        EditText edtEndDate = dialogView.findViewById(R.id.edt_end_date);
        EditText edtRewardPoints = dialogView.findViewById(R.id.edt_reward_points);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinner_status);
        imgPreviewChallenge = dialogView.findViewById(R.id.img_preview_challenge);
        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);

        // Setup status spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Sắp diễn ra", "Đang diễn ra", "Đã kết thúc"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        // Date pickers
        edtStartDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                edtStartDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        edtEndDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                edtEndDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Fill data if editing
        if (challenge != null) {
            edtTitle.setText(challenge.getTitle());
            edtDescription.setText(challenge.getDescription());
            edtStartDate.setText(challenge.getStartDateStr());
            edtEndDate.setText(challenge.getEndDateStr());
            edtRewardPoints.setText(String.valueOf(challenge.getRewardPoints()));
            spinnerStatus.setSelection(challenge.getStatusInt());

            if (challenge.getImagePath() != null && !challenge.getImagePath().isEmpty()) {
                File imageFile = new File(challenge.getImagePath());
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(challenge.getImagePath());
                    imgPreviewChallenge.setImageBitmap(bitmap);
                }
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(challenge == null ? "Thêm thử thách" : "Sửa thử thách")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String title = edtTitle.getText().toString().trim();
                String description = edtDescription.getText().toString().trim();
                String startDate = edtStartDate.getText().toString().trim();
                String endDate = edtEndDate.getText().toString().trim();
                String rewardPointsStr = edtRewardPoints.getText().toString().trim();
                int status = spinnerStatus.getSelectedItemPosition();

                if (title.isEmpty()) {
                    edtTitle.setError("Vui lòng nhập tiêu đề");
                    return;
                }

                if (startDate.isEmpty()) {
                    edtStartDate.setError("Vui lòng chọn ngày bắt đầu");
                    return;
                }

                if (endDate.isEmpty()) {
                    edtEndDate.setError("Vui lòng chọn ngày kết thúc");
                    return;
                }

                int rewardPoints = 0;
                if (!rewardPointsStr.isEmpty()) {
                    try {
                        rewardPoints = Integer.parseInt(rewardPointsStr);
                    } catch (NumberFormatException e) {
                        edtRewardPoints.setError("Điểm không hợp lệ");
                        return;
                    }
                }

                try {
                    Date start = sdf.parse(startDate);
                    Date end = sdf.parse(endDate);

                    if (end.before(start)) {
                        Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long durationMillis = end.getTime() - start.getTime();
                    int durationDays = (int) (durationMillis / (24 * 60 * 60 * 1000));

                    Challenge newChallenge = new Challenge();
                    if (challenge != null) {
                        newChallenge.setId(challenge.getId());
                    }
                    newChallenge.setTitle(title);
                    newChallenge.setDescription(description);
                    newChallenge.setStartDate(start.getTime());
                    newChallenge.setEndDate(end.getTime());
                    newChallenge.setDurationDays(durationDays);
                    newChallenge.setRewardPoints(rewardPoints);
                    newChallenge.setStatus(status);
                    newChallenge.setImagePath(selectedImagePath);

                    if (challenge == null) {
                        new AddChallengeTask(newChallenge).execute();
                    } else {
                        new UpdateChallengeTask(newChallenge).execute();
                    }

                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
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
                String fileName = "challenge_" + System.currentTimeMillis() + ".jpg";
                File directory = new File(getFilesDir(), "challenge_images");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                File imageFile = new File(directory, fileName);
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();

                selectedImagePath = imageFile.getAbsolutePath();
                imgPreviewChallenge.setImageBitmap(bitmap);

                Toast.makeText(this, "Đã chọn ảnh", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi khi chọn ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private class AddChallengeTask extends AsyncTask<Void, Void, Boolean> {
        private Challenge challenge;

        AddChallengeTask(Challenge challenge) {
            this.challenge = challenge;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String statusStr = challenge.getStatusInt() == 0 ? "upcoming" :
                        challenge.getStatusInt() == 1 ? "active" : "completed";

                String sql = "INSERT INTO challenges (title, description, start_date, end_date, " +
                        "duration_days, reward_points, status, image_path, participants) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";

                db.execSQL(sql, new Object[]{
                        challenge.getTitle(),
                        challenge.getDescription(),
                        challenge.getStartDate(),
                        challenge.getEndDate(),
                        challenge.getDurationDays(),
                        challenge.getRewardPoints(),
                        statusStr,
                        challenge.getImagePath()
                });

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error adding challenge: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AdminChallengeManagementActivity.this,
                        "Đã thêm thử thách!", Toast.LENGTH_SHORT).show();
                loadChallenges();
            } else {
                Toast.makeText(AdminChallengeManagementActivity.this,
                        "Lỗi khi thêm!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateChallengeTask extends AsyncTask<Void, Void, Boolean> {
        private Challenge challenge;

        UpdateChallengeTask(Challenge challenge) {
            this.challenge = challenge;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String statusStr = challenge.getStatusInt() == 0 ? "upcoming" :
                        challenge.getStatusInt() == 1 ? "active" : "completed";

                String sql = "UPDATE challenges SET title = ?, description = ?, start_date = ?, " +
                        "end_date = ?, duration_days = ?, reward_points = ?, status = ?, image_path = ? " +
                        "WHERE id = ?";

                db.execSQL(sql, new Object[]{
                        challenge.getTitle(),
                        challenge.getDescription(),
                        challenge.getStartDate(),
                        challenge.getEndDate(),
                        challenge.getDurationDays(),
                        challenge.getRewardPoints(),
                        statusStr,
                        challenge.getImagePath(),
                        challenge.getId()
                });

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error updating challenge: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AdminChallengeManagementActivity.this,
                        "Đã cập nhật thử thách!", Toast.LENGTH_SHORT).show();
                loadChallenges();
            } else {
                Toast.makeText(AdminChallengeManagementActivity.this,
                        "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show();
            }
        }
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