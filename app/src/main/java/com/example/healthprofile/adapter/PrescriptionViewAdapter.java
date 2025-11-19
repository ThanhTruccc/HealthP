package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.PrescriptionItem;

import java.util.List;

public class PrescriptionViewAdapter extends RecyclerView.Adapter<PrescriptionViewAdapter.ViewHolder> {

    private Context context;
    private List<PrescriptionItem> prescriptions;

    public PrescriptionViewAdapter(Context context, List<PrescriptionItem> prescriptions) {
        this.context = context;
        this.prescriptions = prescriptions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrescriptionItem item = prescriptions.get(position);

        holder.tvMedicationName.setText(item.getMedicationName());
        holder.tvDosage.setText("Liều lượng: " + item.getDosage());
        holder.tvFrequency.setText("Tần suất: " + item.getFrequency());
        holder.tvDuration.setText("Thời gian: " + item.getDuration());

        if (item.getQuantity() > 0) {
            holder.tvQuantity.setText("Số lượng: " + item.getQuantity());
            holder.tvQuantity.setVisibility(View.VISIBLE);
        } else {
            holder.tvQuantity.setVisibility(View.GONE);
        }

        if (item.getInstructions() != null && !item.getInstructions().isEmpty()) {
            holder.tvInstructions.setText("Hướng dẫn: " + item.getInstructions());
            holder.tvInstructions.setVisibility(View.VISIBLE);
        } else {
            holder.tvInstructions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return prescriptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicationName, tvDosage, tvFrequency, tvDuration, tvQuantity, tvInstructions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvInstructions = itemView.findViewById(R.id.tv_instructions);
        }
    }
}