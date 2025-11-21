package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.BMIRecord;

import java.util.List;

public class BMIHistoryAdapter extends RecyclerView.Adapter<BMIHistoryAdapter.ViewHolder> {

    private Context context;
    private List<BMIRecord> records;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(BMIRecord record);
    }

    public BMIHistoryAdapter(Context context, List<BMIRecord> records, OnItemClickListener listener) {
        this.context = context;
        this.records = records;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bmi_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BMIRecord record = records.get(position);

        holder.tvDate.setText(record.getFormattedDate());
        holder.tvBmiValue.setText(record.getBmiFormatted());
        holder.tvCategory.setText(record.getCategory());
        holder.tvDetails.setText(record.getHeightFormatted() + " • " + record.getWeightFormatted());

        // Set category color
        int categoryColor = getCategoryColor(record.getBmi());
        holder.tvCategory.setTextColor(categoryColor);
        holder.tvBmiValue.setTextColor(categoryColor);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    private int getCategoryColor(float bmi) {
        if (bmi < 18.5) {
            return 0xFF4ECDC4;
        } else if (bmi >= 18.5 && bmi < 25) {
            return 0xFF4CAF50;
        } else if (bmi >= 25 && bmi < 30) {
            return 0xFFFFD93D;
        } else if (bmi >= 30 && bmi < 35) {
            return 0xFFFF8C42;
        } else {
            return 0xFFE74C3C;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDate, tvBmiValue, tvCategory, tvDetails;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_bmi_item);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvBmiValue = itemView.findViewById(R.id.tv_bmi_value);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDetails = itemView.findViewById(R.id.tv_details);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}