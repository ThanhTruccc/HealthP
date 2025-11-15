package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.RedeemedVoucher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RedeemedVoucherAdapter extends RecyclerView.Adapter<RedeemedVoucherAdapter.RedeemedViewHolder> {

    private Context context;
    private List<RedeemedVoucher> redeemedVouchers;
    private SimpleDateFormat dateFormat;

    public RedeemedVoucherAdapter(Context context, List<RedeemedVoucher> redeemedVouchers) {
        this.context = context;
        this.redeemedVouchers = redeemedVouchers;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public RedeemedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_redeemed_voucher, parent, false);
        return new RedeemedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RedeemedViewHolder holder, int position) {
        RedeemedVoucher voucher = redeemedVouchers.get(position);

        holder.tvTitle.setText(voucher.getVoucherTitle());
        holder.tvCode.setText("Mã: " + voucher.getVoucherCode());
        holder.tvPoints.setText("-" + voucher.getPointsUsed() + " điểm");
        holder.tvDate.setText(dateFormat.format(new Date(voucher.getRedeemedDate())));

        // Set status
        String status = voucher.getStatus();
        if ("active".equals(status)) {
            holder.tvStatus.setText("Đang hoạt động");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if ("used".equals(status)) {
            holder.tvStatus.setText("Đã sử dụng");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else if ("expired".equals(status)) {
            holder.tvStatus.setText("Hết hạn");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return redeemedVouchers.size();
    }

    static class RedeemedViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCode, tvPoints, tvDate, tvStatus;

        public RedeemedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_redeemed_title);
            tvCode = itemView.findViewById(R.id.tv_voucher_code);
            tvPoints = itemView.findViewById(R.id.tv_points_used);
            tvDate = itemView.findViewById(R.id.tv_redeemed_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}