package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.HealthRecord;

import java.util.List;

public class DoctorMedicalRecordAdapter extends RecyclerView.Adapter<DoctorMedicalRecordAdapter.ViewHolder> {

    private Context context;
    private List<HealthRecord> records;
    private OnRecordClickListener listener;

    public interface OnRecordClickListener {
        void onRecordClick(HealthRecord record);
    }

    public DoctorMedicalRecordAdapter(Context context, List<HealthRecord> records, OnRecordClickListener listener) {
        this.context = context;
        this.records = records;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_medical_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthRecord record = records.get(position);

        // Lấy tên bệnh nhân từ notes (đã lưu tạm ở loadMedicalRecords)
        String patientName = "Không rõ";
        if (record.getNotes() != null && record.getNotes().startsWith("Patient: ")) {
            patientName = record.getNotes().replace("Patient: ", "");
        }

        holder.tvPatientName.setText(patientName);
        holder.tvVisitDate.setText(formatDate(record.getDate()));
        holder.tvDiagnosis.setText(record.getDiagnosis() != null ? record.getDiagnosis() : "Chưa có chẩn đoán");
        holder.tvComplaint.setText(record.getSymptoms() != null ? record.getSymptoms() : "Không có triệu chứng");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecordClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    private String formatDate(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        try {
            String[] parts = date.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {
            return date;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvVisitDate, tvDiagnosis, tvComplaint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvVisitDate = itemView.findViewById(R.id.tv_visit_date);
            tvDiagnosis = itemView.findViewById(R.id.tv_diagnosis);
            tvComplaint = itemView.findViewById(R.id.tv_complaint);
        }
    }
}