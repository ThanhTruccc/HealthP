package com.example.healthprofile.adapter; // Đặt trong thư mục adapter

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R; // Đảm bảo đúng package R
import com.example.healthprofile.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final Context context;

    // Constructor
    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    /**
     * ViewHolder chứa các View cho mỗi item trong danh sách.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFullName, tvEmail, tvPhone, tvRole;

        public UserViewHolder(View itemView) {
            super(itemView);
            // Thay đổi id cho phù hợp với item_user.xml của bạn
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvRole = itemView.findViewById(R.id.tv_role);

            // Có thể thêm OnClickListener ở đây
            itemView.setOnClickListener(v -> {
                // Xử lý sự kiện click item
            });
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tải layout cho từng mục (item_user.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = userList.get(position);

        // Gán dữ liệu cho các View
        holder.tvFullName.setText("Họ tên: " + currentUser.getFullName());
        holder.tvEmail.setText("Email: " + currentUser.getEmail());
        holder.tvPhone.setText("SĐT: " + currentUser.getPhone());
        holder.tvRole.setText("Vai trò: " + currentUser.getRole());

        // Có thể thêm logic màu sắc, hình ảnh dựa trên dữ liệu ở đây
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}