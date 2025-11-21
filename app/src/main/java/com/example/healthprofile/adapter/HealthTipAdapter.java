package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.HealthTip;

import java.util.List;

public class HealthTipAdapter extends RecyclerView.Adapter<HealthTipAdapter.ViewHolder> {

    private Context context;
    private List<HealthTip> tips;
    private OnTipClickListener listener;

    public interface OnTipClickListener {
        void onTipClick(HealthTip tip);
    }

    public HealthTipAdapter(Context context, List<HealthTip> tips, OnTipClickListener listener) {
        this.context = context;
        this.tips = tips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_health_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthTip tip = tips.get(position);

        holder.tvTitle.setText(tip.getTitle());
        holder.tvContent.setText(tip.getContent());
        holder.tvCategory.setText(tip.getCategoryName());

        // Set category color
        holder.cardCategory.setCardBackgroundColor(tip.getCategoryColor());

        // Set icon based on category
        holder.ivCategory.setImageResource(tip.getCategoryIcon());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTipClick(tip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvCategory;
        CardView cardCategory;
        ImageView ivCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvCategory = itemView.findViewById(R.id.tv_category);
            cardCategory = itemView.findViewById(R.id.card_category);
            ivCategory = itemView.findViewById(R.id.iv_category);
        }
    }
}