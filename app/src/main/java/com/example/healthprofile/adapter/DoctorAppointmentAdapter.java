package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.Appointment;

import java.util.List;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onViewClick(Appointment appointment);
        void onStartExamClick(Appointment appointment);
    }

    public DoctorAppointmentAdapter(Context context, List<Appointment> appointments, OnAppointmentClickListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        holder.tvPatientName.setText(appointment.getPatientName());
        holder.tvDateTime.setText(appointment.getFormattedDateTime());
        holder.tvReason.setText(appointment.getReason());
        holder.tvStatus.setText(appointment.getStatusText());
        holder.cardStatus.setCardBackgroundColor(appointment.getStatusColor());

        // Show/hide buttons based on status
        if ("confirmed".equals(appointment.getStatus())) {
            holder.btnStartExam.setVisibility(View.VISIBLE);
        } else {
            holder.btnStartExam.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewClick(appointment);
            }
        });

        holder.btnStartExam.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStartExamClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvDateTime, tvReason, tvStatus;
        CardView cardStatus;
        Button btnStartExam;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvStatus = itemView.findViewById(R.id.tv_status);
            cardStatus = itemView.findViewById(R.id.card_status);
            btnStartExam = itemView.findViewById(R.id.btn_start_exam);
        }
    }
}