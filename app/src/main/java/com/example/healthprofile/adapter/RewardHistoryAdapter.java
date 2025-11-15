package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.RewardPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RewardHistoryAdapter extends RecyclerView.Adapter<RewardHistoryAdapter.ViewHolder> {

    private Context context;
    private List<RewardPoint> rewardList;
    private SimpleDateFormat dateFormat;

    public RewardHistoryAdapter(Context context, List<RewardPoint> rewardList) {
        this.context = context;
        this.rewardList = rewardList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RewardPoint reward = rewardList.get(position);

        // Hiển thị action/description
        if (reward.getActionn() != null && !reward.getActionn().isEmpty()) {
            holder.tvAction.setText(reward.getActionn());
        } else {
            holder.tvAction.setText("Hoạt động");
        }

        // Hiển thị mô tả
        if (reward.getDescription() != null && !reward.getDescription().isEmpty()) {
            holder.tvDescription.setText(reward.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Hiển thị thời gian
        String timeStr = dateFormat.format(new Date(reward.getTimestamp()));
        holder.tvTime.setText(timeStr);

        // Hiển thị điểm thay đổi với màu sắc
        int pointsChange = reward.getPointsChange();
        if (pointsChange > 0) {
            holder.tvPoints.setText("+" + pointsChange + " điểm");
            holder.tvPoints.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if (pointsChange < 0) {
            holder.tvPoints.setText(pointsChange + " điểm");
            holder.tvPoints.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvPoints.setText("0 điểm");
            holder.tvPoints.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Hiển thị tổng điểm sau giao dịch
        holder.tvTotalAfter.setText("Tổng: " + reward.getPoints() + " điểm");
    }

    @Override
    public int getItemCount() {
        return rewardList != null ? rewardList.size() : 0;
    }

    public void updateData(List<RewardPoint> newList) {
        this.rewardList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction;
        TextView tvDescription;
        TextView tvTime;
        TextView tvPoints;
        TextView tvTotalAfter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tv_reward_action);
            tvDescription = itemView.findViewById(R.id.tv_reward_description);
            tvTime = itemView.findViewById(R.id.tv_reward_time);
            tvPoints = itemView.findViewById(R.id.tv_reward_points);
            tvTotalAfter = itemView.findViewById(R.id.tv_reward_total_after);
        }
    }
}