package com.example.healthprofile.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.Appointment;

import java.util.List;

/**
 * Adapter cho Admin quản lý appointments với nhiều actions
 */
public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private OnAdminAppointmentActionListener listener;

    public interface OnAdminAppointmentActionListener {
        void onConfirmAppointment(Appointment appointment, int position);
        void onCompleteAppointment(Appointment appointment, int position);
        void onCancelAppointment(Appointment appointment, int position);
        void onDeleteAppointment(Appointment appointment, int position);
        void onViewDetail(Appointment appointment);
    }

    public AdminAppointmentAdapter(Context context, List<Appointment> appointments, OnAdminAppointmentActionListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        // Set basic info
        holder.tvPatientName.setText(appointment.getPatientName());
        holder.tvPhone.setText(appointment.getPhone());
        holder.tvDoctorName.setText("BS: " + appointment.getDoctorName());
        holder.tvDateTime.setText(appointment.getDate() + " - " + appointment.getTime());
        holder.tvReason.setText(appointment.getReason());
        holder.tvStatus.setText(appointment.getStatusText());
        holder.tvFee.setText(appointment.getFeeFormatted());

        // Set status color
        setStatusColor(holder.tvStatus, appointment.getStatus());

        // Show/hide buttons based on status
        setupButtons(holder, appointment, position);

        // Click to view detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetail(appointment);
            }
        });
    }

    private void setStatusColor(TextView tvStatus, String status) {
        switch (status) {
            case "pending":
                tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "confirmed":
                tvStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "completed":
                tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "cancelled":
                tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
                break;
        }
    }

    private void setupButtons(ViewHolder holder, Appointment appointment, int position) {
        String status = appointment.getStatus();

        // Reset visibility
        holder.btnConfirm.setVisibility(View.GONE);
        holder.btnComplete.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.VISIBLE);
        holder.btnDelete.setVisibility(View.VISIBLE);

        // Setup buttons based on status
        if (status.equals("pending")) {
            // Pending: Show Confirm
            holder.btnConfirm.setVisibility(View.VISIBLE);
            holder.btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirmAppointment(appointment, position);
                }
            });
        } else if (status.equals("confirmed")) {
            // Confirmed: Show Complete
            holder.btnComplete.setVisibility(View.VISIBLE);
            holder.btnComplete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCompleteAppointment(appointment, position);
                }
            });
        } else if (status.equals("completed") || status.equals("cancelled")) {
            // Completed/Cancelled: Only show Delete
            holder.btnCancel.setVisibility(View.GONE);
        }

        // Cancel button
        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelAppointment(appointment, position);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteAppointment(appointment, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvPhone, tvDoctorName, tvDateTime, tvReason, tvStatus, tvFee;
        Button btnConfirm, btnComplete, btnCancel, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name_admin);
            tvPhone = itemView.findViewById(R.id.tv_phone_admin);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name_admin);
            tvDateTime = itemView.findViewById(R.id.tv_date_time_admin);
            tvReason = itemView.findViewById(R.id.tv_reason_admin);
            tvStatus = itemView.findViewById(R.id.tv_status_admin);
            tvFee = itemView.findViewById(R.id.tv_fee_admin);
            btnConfirm = itemView.findViewById(R.id.btn_confirm_admin);
            btnComplete = itemView.findViewById(R.id.btn_complete_admin);
            btnCancel = itemView.findViewById(R.id.btn_cancel_admin);
            btnDelete = itemView.findViewById(R.id.btn_delete_admin);
        }
    }
}