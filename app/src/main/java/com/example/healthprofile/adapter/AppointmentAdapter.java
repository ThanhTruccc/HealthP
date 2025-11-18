package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onCancelAppointment(Appointment appointment, int position);
        void onViewDetail(Appointment appointment);
    }

    public AppointmentAdapter(Context context, List<Appointment> appointments, OnAppointmentActionListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        holder.tvDoctorName.setText(appointment.getDoctorName());
        holder.tvDateTime.setText(appointment.getFormattedDateTime());
        holder.tvPatientName.setText(appointment.getPatientName());
        holder.tvReason.setText(appointment.getReason());
        holder.tvFee.setText(appointment.getFeeFormatted());

        // Show/hide action buttons based on status
        if (appointment.getStatus().equals("completed") || appointment.getStatus().equals("cancelled")) {
            holder.layoutActions.setVisibility(View.GONE);
        } else {
            holder.layoutActions.setVisibility(View.VISIBLE);
        }

        holder.btnCancel.setOnClickListener(v -> showCancelDialog(appointment, position));

        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetail(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    private void showCancelDialog(Appointment appointment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Hủy lịch hẹn");
        builder.setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này không?");

        builder.setPositiveButton("Hủy lịch", (dialog, which) -> {
            if (listener != null) {
                listener.onCancelAppointment(appointment, position);
            }
            Toast.makeText(context, "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    public void updateAppointment(int position, Appointment appointment) {
        appointments.set(position, appointment);
        notifyItemChanged(position);
    }

    public void removeAppointment(int position) {
        appointments.remove(position);
        notifyItemRemoved(position);
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDateTime, tvPatientName, tvReason, tvFee;
        Button btnCancel, btnViewDetail;
        LinearLayout layoutActions;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvFee = itemView.findViewById(R.id.tv_fee);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
            layoutActions = itemView.findViewById(R.id.layout_actions);
        }
    }
}