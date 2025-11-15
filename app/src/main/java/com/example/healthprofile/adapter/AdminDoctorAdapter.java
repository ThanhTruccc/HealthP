package com.example.healthprofile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.Doctor;

import java.io.File;
import java.util.List;

public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.ViewHolder> {

    private Context context;
    private List<Doctor> doctors;
    private OnDoctorActionListener listener;

    public interface OnDoctorActionListener {
        void onEditDoctor(Doctor doctor, int position);
        void onDeleteDoctor(Doctor doctor, int position);
        void onViewDoctorDetail(Doctor doctor);
    }

    public AdminDoctorAdapter(Context context, List<Doctor> doctors, OnDoctorActionListener listener) {
        this.context = context;
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);

        holder.tvName.setText(doctor.getName());
        holder.tvDegree.setText(doctor.getDegree());
        holder.tvExperience.setText(doctor.getExperience() + " năm");
        holder.tvRating.setText(String.format("%.1f ⭐", doctor.getRating()));

        // Hiển thị ảnh - ưu tiên ảnh tùy chỉnh
        if (doctor.getImagePath() != null && !doctor.getImagePath().isEmpty()) {
            // Load ảnh từ internal storage
            File imgFile = new File(doctor.getImagePath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.imgDoctor.setImageBitmap(bitmap);
            } else {
                // Fallback to default
                holder.imgDoctor.setImageResource(R.drawable.doctor_1);
            }
        } else {
            // Dùng ảnh mặc định từ resource
            if (doctor.getImageResource() != 0) {
                holder.imgDoctor.setImageResource(doctor.getImageResource());
            } else {
                holder.imgDoctor.setImageResource(R.drawable.doctor_1);
            }
        }

        // Click item to view detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDoctorDetail(doctor);
            }
        });

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditDoctor(doctor, position);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteDoctor(doctor, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDoctor;
        TextView tvName, tvDegree, tvExperience, tvRating;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDoctor = itemView.findViewById(R.id.img_doctor_admin);
            tvName = itemView.findViewById(R.id.tv_doctor_name_admin);
            tvDegree = itemView.findViewById(R.id.tv_doctor_degree_admin);
            tvExperience = itemView.findViewById(R.id.tv_doctor_experience_admin);
            tvRating = itemView.findViewById(R.id.tv_doctor_rating_admin);
            btnEdit = itemView.findViewById(R.id.btn_edit_doctor);
            btnDelete = itemView.findViewById(R.id.btn_delete_doctor);
        }
    }
}