package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.MedicationReminder;

import java.util.List;

public class MedicationReminderAdapter extends RecyclerView.Adapter<MedicationReminderAdapter.ViewHolder> {

    private Context context;
    private List<MedicationReminder> reminderList;
    private OnReminderActionListener listener;

    public interface OnReminderActionListener {
        void onEdit(MedicationReminder reminder);
        void onDelete(MedicationReminder reminder);
        void onToggleActive(MedicationReminder reminder);
    }

    public MedicationReminderAdapter(Context context, List<MedicationReminder> reminderList, OnReminderActionListener listener) {
        this.context = context;
        this.reminderList = reminderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationReminder reminder = reminderList.get(position);

        // Set medication name and dosage
        holder.tvMedicationName.setText(reminder.getMedicationName());

        if (reminder.getDosage() != null && !reminder.getDosage().isEmpty()) {
            holder.tvDosage.setVisibility(View.VISIBLE);
            holder.tvDosage.setText(reminder.getDosage());
        } else {
            holder.tvDosage.setVisibility(View.GONE);
        }

        // Set times
        if (reminder.getTime1() != null && !reminder.getTime1().isEmpty()) {
            holder.time1Container.setVisibility(View.VISIBLE);
            holder.tvTime1.setText(reminder.getTime1());
        } else {
            holder.time1Container.setVisibility(View.GONE);
        }

        if (reminder.getTime2() != null && !reminder.getTime2().isEmpty()) {
            holder.time2Container.setVisibility(View.VISIBLE);
            holder.tvTime2.setText(reminder.getTime2());
        } else {
            holder.time2Container.setVisibility(View.GONE);
        }

        if (reminder.getTime3() != null && !reminder.getTime3().isEmpty()) {
            holder.time3Container.setVisibility(View.VISIBLE);
            holder.tvTime3.setText(reminder.getTime3());
        } else {
            holder.time3Container.setVisibility(View.GONE);
        }

        // Set notes
        if (reminder.getNotes() != null && !reminder.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(reminder.getNotes());
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Set active status
        holder.switchActive.setChecked(reminder.isActive());

        // Apply alpha based on active status
        float alpha = reminder.isActive() ? 1.0f : 0.5f;
        holder.tvMedicationName.setAlpha(alpha);
        holder.tvDosage.setAlpha(alpha);
        holder.tvTime1.setAlpha(alpha);
        holder.tvTime2.setAlpha(alpha);
        holder.tvTime3.setAlpha(alpha);

        // Set listeners
        holder.switchActive.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleActive(reminder);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(reminder);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicationName, tvDosage;
        TextView tvTime1, tvTime2, tvTime3;
        TextView tvNotes;
        LinearLayout time1Container, time2Container, time3Container;
        SwitchCompat switchActive;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvTime1 = itemView.findViewById(R.id.tv_time1);
            tvTime2 = itemView.findViewById(R.id.tv_time2);
            tvTime3 = itemView.findViewById(R.id.tv_time3);
            tvNotes = itemView.findViewById(R.id.tv_notes);
            time1Container = itemView.findViewById(R.id.time1_container);
            time2Container = itemView.findViewById(R.id.time2_container);
            time3Container = itemView.findViewById(R.id.time3_container);
            switchActive = itemView.findViewById(R.id.switch_active);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}