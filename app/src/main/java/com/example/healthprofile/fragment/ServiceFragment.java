package com.example.healthprofile.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.BookAppointmentActivity;
import com.example.healthprofile.MyAppointmentsActivity;
import com.example.healthprofile.R;
import com.example.healthprofile.adapter.DoctorAdapter;
import com.example.healthprofile.model.Doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment hiển thị các dịch vụ y tế và danh sách bác sĩ
 */
public class ServiceFragment extends Fragment {

    private static final String TAG = "ServiceFragment";

    private RecyclerView rvDoctors;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> doctorList;
    private ProgressBar progressBar;
    private CardView cardMyAppointments;
    private CardView cardBookAppointment;

    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "health_profile.db";
    private static final String TABLE_DOCTORS = "doctors";
    private ExecutorService executorService;
    private Handler mainHandler;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);

        db = requireContext().openOrCreateDatabase(DATABASE_NAME,
                android.content.Context.MODE_PRIVATE, null);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews(view);
        setupRecyclerView();
        loadDoctorsFromDatabase();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        rvDoctors = view.findViewById(R.id.rv_doctors);
        progressBar = view.findViewById(R.id.progress_bar);
        cardMyAppointments = view.findViewById(R.id.card_my_appointments);
        cardBookAppointment = view.findViewById(R.id.card_book_appointment);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        doctorList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(getContext(), doctorList);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvDoctors.setLayoutManager(gridLayoutManager);
        rvDoctors.setAdapter(doctorAdapter);
    }

    /**
     * Load danh sách bác sĩ từ database (background thread)
     */
    private void loadDoctorsFromDatabase() {
        // Show progress bar
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Execute in background thread
        executorService.execute(() -> {
            List<Doctor> doctors = getAllDoctors();

            // Update UI on main thread
            mainHandler.post(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (doctors != null && !doctors.isEmpty()) {
                    Log.d(TAG, "Loaded " + doctors.size() + " doctors");
                    doctorList.clear();
                    doctorList.addAll(doctors);
                    doctorAdapter.notifyDataSetChanged();
                } else {
                    Log.w(TAG, "No doctors found");
                    Toast.makeText(getContext(), "Không có bác sĩ nào", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Lấy tất cả bác sĩ từ database
     */
    private List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();

        try {
            // Query danh sách bác sĩ
            String sql = "SELECT * FROM " + TABLE_DOCTORS + " ORDER BY rating DESC";
            Cursor cursor = db.rawQuery(sql, null);

            Log.d(TAG, "Total doctors in database: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    try {
                        Doctor doctor = new Doctor();

                        // ID
                        int idIndex = cursor.getColumnIndex("id");
                        if (idIndex >= 0) {
                            doctor.setId(cursor.getInt(idIndex));
                        }

                        // Name
                        int nameIndex = cursor.getColumnIndex("name");
                        if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                            doctor.setName(cursor.getString(nameIndex));
                        }

                        // Degree
                        int degreeIndex = cursor.getColumnIndex("degree");
                        if (degreeIndex >= 0 && !cursor.isNull(degreeIndex)) {
                            doctor.setDegree(cursor.getString(degreeIndex));
                        }

                        // Specialty
                        int specialtyIndex = cursor.getColumnIndex("specialty");
                        if (specialtyIndex >= 0 && !cursor.isNull(specialtyIndex)) {
                            doctor.setSpecialty(cursor.getString(specialtyIndex));
                        }

                        // Workplace
                        int workplaceIndex = cursor.getColumnIndex("workplace");
                        if (workplaceIndex >= 0 && !cursor.isNull(workplaceIndex)) {
                            doctor.setWorkplace(cursor.getString(workplaceIndex));
                        }

                        // Rating
                        int ratingIndex = cursor.getColumnIndex("rating");
                        if (ratingIndex >= 0 && !cursor.isNull(ratingIndex)) {
                            doctor.setRating(cursor.getFloat(ratingIndex));
                        }

                        // Experience
                        int experienceIndex = cursor.getColumnIndex("experience");
                        if (experienceIndex >= 0 && !cursor.isNull(experienceIndex)) {
                            doctor.setExperience(cursor.getInt(experienceIndex));
                        }

                        // Image Resource (ảnh mặc định)
                        int imageResourceIndex = cursor.getColumnIndex("image_resource");
                        if (imageResourceIndex >= 0 && !cursor.isNull(imageResourceIndex)) {
                            int imageResource = cursor.getInt(imageResourceIndex);
                            if (imageResource > 0) {
                                doctor.setImageResource(imageResource);
                            } else {
                                doctor.setImageResource(R.drawable.doctor_1); // Default
                            }
                        } else {
                            doctor.setImageResource(R.drawable.doctor_1); // Default
                        }

                        // Image Path (ảnh custom từ user upload)
                        int imagePathIndex = cursor.getColumnIndex("image_path");
                        if (imagePathIndex >= 0 && !cursor.isNull(imagePathIndex)) {
                            String imagePath = cursor.getString(imagePathIndex);
                            if (imagePath != null && !imagePath.isEmpty()) {
                                doctor.setImagePath(imagePath);
                                Log.d(TAG, "Doctor " + doctor.getName() + " has custom image: " + imagePath);
                            }
                        }

                        doctors.add(doctor);
                        Log.d(TAG, "Loaded doctor: " + doctor.getName() +
                                ", ID=" + doctor.getId() +
                                ", imageResource=" + doctor.getImageResource() +
                                ", imagePath=" + (doctor.getImagePath() != null ? doctor.getImagePath() : "null"));

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing doctor: " + e.getMessage());
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error loading doctors: " + e.getMessage());
            e.printStackTrace();
        }

        return doctors;
    }

    private void setupClickListeners() {
        // Card "Lịch hẹn của tôi"
        if (cardMyAppointments != null) {
            cardMyAppointments.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MyAppointmentsActivity.class);
                startActivity(intent);
            });
        }

        // Card "Đặt lịch khám"
        if (cardBookAppointment != null) {
            cardBookAppointment.setOnClickListener(v -> {
                if (!doctorList.isEmpty()) {
                    Doctor firstDoctor = doctorList.get(0);
                    Intent intent = new Intent(getActivity(), BookAppointmentActivity.class);
                    intent.putExtra("doctor_id", firstDoctor.getId());
                    intent.putExtra("doctor_name", firstDoctor.getDegree() + " " + firstDoctor.getName());
                    intent.putExtra("doctor_rating", firstDoctor.getRating());
                    intent.putExtra("doctor_experience", firstDoctor.getExperience());
                    intent.putExtra("doctor_image", firstDoctor.getImageResource());
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Đang tải danh sách bác sĩ...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload danh sách bác sĩ khi quay lại fragment
        Log.d(TAG, "onResume: Reloading doctors");
        loadDoctorsFromDatabase();
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