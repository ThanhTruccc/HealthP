package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.PrescriptionItem;

import java.util.List;

public class PrescriptionItemAdapter extends RecyclerView.Adapter<PrescriptionItemAdapter.ViewHolder> {

    private Context context;
    private List<PrescriptionItem> prescriptionList;
    private OnDeleteClickListener deleteListener;

    // Interface để xử lý sự kiện xóa
    public interface OnDeleteClickListener {
        void onDeleteClick(PrescriptionItem item);
    }

    public PrescriptionItemAdapter(Context context, List<PrescriptionItem> prescriptionList,
                                   OnDeleteClickListener deleteListener) {
        this.context = context;
        this.prescriptionList = prescriptionList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrescriptionItem item = prescriptionList.get(position);

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

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicationName, tvDosage, tvFrequency, tvDuration, tvQuantity, tvInstructions;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvInstructions = itemView.findViewById(R.id.tv_instructions);
            btnDelete = itemView.findViewById(R.id.btn_delete_prescription);
        }
    }
}