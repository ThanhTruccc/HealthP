package com.example.healthprofile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.Voucher;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private Context context;
    private List<Voucher> vouchers;
    private OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onRedeemClick(Voucher voucher);
    }

    public VoucherAdapter(Context context, List<Voucher> vouchers, OnVoucherClickListener listener) {
        this.context = context;
        this.vouchers = vouchers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);

        holder.tvTitle.setText(voucher.getTitle());
        holder.tvDescription.setText(voucher.getDescription());
        holder.tvDiscount.setText("-" + voucher.getDiscountPercent() + "%");
        holder.tvPoints.setText(voucher.getPointsRequired() + " điểm");

        if (voucher.getImageResource() != 0) {
            holder.ivVoucher.setImageResource(voucher.getImageResource());
        }

        holder.btnRedeem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRedeemClick(voucher);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVoucher;
        TextView tvTitle, tvDescription, tvDiscount, tvPoints;
        Button btnRedeem;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVoucher = itemView.findViewById(R.id.iv_voucher);
            tvTitle = itemView.findViewById(R.id.tv_voucher_title);
            tvDescription = itemView.findViewById(R.id.tv_voucher_description);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvPoints = itemView.findViewById(R.id.tv_points_required);
            btnRedeem = itemView.findViewById(R.id.btn_redeem);
        }
    }
}