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
import com.example.healthprofile.model.User;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private Context context;
    private List<User> users;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEditUser(User user, int position);
        void onDeleteUser(User user, int position);
        void onViewUserDetail(User user);
    }

    public AdminUserAdapter(Context context, List<User> users, OnUserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvFullName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật");
        holder.tvRole.setText(user.isAdmin() ? "Admin" : "User");

        // Set màu cho role
        if (user.isAdmin()) {
            holder.tvRole.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvRole.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        }

        // Click item to view detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewUserDetail(user);
            }
        });

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditUser(user, position);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteUser(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvPhone, tvRole;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tv_user_fullname);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvPhone = itemView.findViewById(R.id.tv_user_phone);
            tvRole = itemView.findViewById(R.id.tv_user_role);
            btnEdit = itemView.findViewById(R.id.btn_edit_user);
            btnDelete = itemView.findViewById(R.id.btn_delete_user);
        }
    }
}