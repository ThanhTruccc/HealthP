package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.HealthRecord;

import java.util.List;

public class MedicalUserRecordAdapter extends RecyclerView.Adapter<MedicalUserRecordAdapter.ViewHolder> {

    private Context context;
    private List<HealthRecord> recordList;
    private OnRecordClickListener listener;

    public interface OnRecordClickListener {
        void onRecordClick(HealthRecord record);
    }

    public MedicalUserRecordAdapter(Context context, List<HealthRecord> recordList, OnRecordClickListener listener) {
        this.context = context;
        this.recordList = recordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_medical_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthRecord record = recordList.get(position);

        // Hiển thị tên bác sĩ
        if (record.getDoctorName() != null && !record.getDoctorName().isEmpty()) {
            holder.tvDoctorName.setText(record.getDoctorName());
        } else {
            holder.tvDoctorName.setText("Bác sĩ");
        }

        // Hiển thị ngày khám
        if (record.getDate() != null && !record.getDate().isEmpty()) {
            holder.tvVisitDate.setText(record.getDate());
        } else {
            holder.tvVisitDate.setText("Chưa có ngày");
        }

        // Hiển thị chẩn đoán
        if (record.getDiagnosis() != null && !record.getDiagnosis().isEmpty()) {
            holder.tvDiagnosis.setText(record.getDiagnosis());
            holder.tvDiagnosis.setVisibility(View.VISIBLE);
        } else {
            holder.tvDiagnosis.setVisibility(View.GONE);
        }

        // Hiển thị triệu chứng
        if (record.getSymptoms() != null && !record.getSymptoms().isEmpty()) {
            holder.tvSymptoms.setText(record.getSymptoms());
            holder.tvSymptoms.setVisibility(View.VISIBLE);
        } else {
            holder.tvSymptoms.setVisibility(View.GONE);
        }

        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecordClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDoctorName, tvVisitDate, tvDiagnosis, tvSymptoms;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvVisitDate = itemView.findViewById(R.id.tv_visit_date);
            tvDiagnosis = itemView.findViewById(R.id.tv_diagnosis);
            tvSymptoms = itemView.findViewById(R.id.tv_symptoms);
        }
    }
}