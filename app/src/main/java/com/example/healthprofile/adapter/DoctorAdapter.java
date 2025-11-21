package com.example.healthprofile.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.BookAppointmentActivity;
import com.example.healthprofile.R;
import com.example.healthprofile.model.Doctor;

import java.io.File;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private static final String TAG = "DoctorAdapter";
    private Context context;
    private List<Doctor> doctors;

    public DoctorAdapter(Context context, List<Doctor> doctors) {
        this.context = context;
        this.doctors = doctors;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);

        holder.tvDoctorName.setText(doctor.getName());
        holder.tvDoctorDegree.setText(doctor.getDegree());
        holder.tvRating.setText(doctor.getRatingText());
        holder.tvExperience.setText(doctor.getExperienceText());

        // FIXED: Load ảnh - ưu tiên image_path trước
        loadDoctorImage(holder.ivDoctorAvatar, doctor);

        // Nút đặt lịch khám
        holder.btnBookConsultation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookAppointmentActivity.class);
                intent.putExtra("doctor_id", doctor.getId());
                intent.putExtra("doctor_name", doctor.getDegree() + " " + doctor.getName());
                intent.putExtra("doctor_rating", doctor.getRating());
                intent.putExtra("doctor_experience", doctor.getExperience());
                intent.putExtra("doctor_image", doctor.getImageResource());
                context.startActivity(intent);
            }
        });

        // Nút xem chi tiết
        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDoctorDetailDialog(doctor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    /**
     * Load ảnh bác sĩ - ưu tiên custom image từ image_path
     */
    private void loadDoctorImage(ImageView imageView, Doctor doctor) {
        // Kiểm tra có ảnh custom không
        if (doctor.getImagePath() != null && !doctor.getImagePath().isEmpty()) {
            File imageFile = new File(doctor.getImagePath());

            if (imageFile.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(doctor.getImagePath());
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        Log.d(TAG, "Loaded custom image for: " + doctor.getName());
                        return;
                    } else {
                        Log.w(TAG, "Failed to decode bitmap for: " + doctor.getName());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading custom image: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "Image file not found: " + doctor.getImagePath());
            }
        }

        // Fallback: dùng ảnh mặc định từ resources
        int imageResource = doctor.getImageResource();
        if (imageResource != 0) {
            imageView.setImageResource(imageResource);
            Log.d(TAG, "Loaded default image for: " + doctor.getName());
        } else {
            imageView.setImageResource(R.drawable.doctor_1);
            Log.d(TAG, "Loaded fallback image for: " + doctor.getName());
        }
    }

    /**
     * Hiển thị dialog chi tiết thông tin bác sĩ
     */
    private void showDoctorDetailDialog(Doctor doctor) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_doctor_detail, null);

        ImageView imgDoctor = dialogView.findViewById(R.id.img_doctor_detail);
        TextView tvName = dialogView.findViewById(R.id.tv_doctor_name_detail);
        TextView tvDegree = dialogView.findViewById(R.id.tv_doctor_degree_detail);
        TextView tvSpecialty = dialogView.findViewById(R.id.tv_doctor_specialty_detail);
        TextView tvWorkplace = dialogView.findViewById(R.id.tv_doctor_workplace_detail);
        TextView tvExperience = dialogView.findViewById(R.id.tv_doctor_experience_detail);
        TextView tvRating = dialogView.findViewById(R.id.tv_doctor_rating_detail);

        // Hiển thị ảnh bác sĩ - ưu tiên custom image
        loadDoctorImage(imgDoctor, doctor);

        // Hiển thị thông tin chi tiết
        tvName.setText(doctor.getName());
        tvDegree.setText("Học vị: " + (doctor.getDegree() != null ? doctor.getDegree() : "Chưa cập nhật"));
        tvSpecialty.setText("Chuyên khoa: " + (doctor.getSpecialty() != null ? doctor.getSpecialty() : "Chưa cập nhật"));
        tvWorkplace.setText("Nơi làm việc: " + (doctor.getWorkplace() != null ? doctor.getWorkplace() : "Chưa cập nhật"));
        tvExperience.setText("Kinh nghiệm: " + doctor.getExperience() + " năm");
        tvRating.setText("Đánh giá: " + String.format("%.1f", doctor.getRating()) + "/5.0 ⭐");

        // Hiển thị dialog
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Đặt lịch khám", (dialogInterface, which) -> {
                    // Chuyển đến màn hình đặt lịch
                    Intent intent = new Intent(context, BookAppointmentActivity.class);
                    intent.putExtra("doctor_id", doctor.getId());
                    intent.putExtra("doctor_name", doctor.getDegree() + " " + doctor.getName());
                    intent.putExtra("doctor_rating", doctor.getRating());
                    intent.putExtra("doctor_experience", doctor.getExperience());
                    intent.putExtra("doctor_image", doctor.getImageResource());
                    context.startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .create();

        dialog.show();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDoctorAvatar;
        TextView tvDoctorDegree;
        TextView tvDoctorName;
        TextView tvRating;
        TextView tvExperience;
        ImageButton btnMore;
        Button btnBookConsultation;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDoctorAvatar = itemView.findViewById(R.id.iv_doctor_avatar);
            tvDoctorDegree = itemView.findViewById(R.id.tv_doctor_degree);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvExperience = itemView.findViewById(R.id.tv_experience);
            btnMore = itemView.findViewById(R.id.btn_more);
            btnBookConsultation = itemView.findViewById(R.id.btn_book_consultation);
        }
    }
}