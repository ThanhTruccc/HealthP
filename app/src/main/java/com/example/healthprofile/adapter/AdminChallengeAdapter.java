package com.example.healthprofile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.R;
import com.example.healthprofile.model.Challenge;

import java.io.File;
import java.util.List;

public class AdminChallengeAdapter extends RecyclerView.Adapter<AdminChallengeAdapter.ChallengeViewHolder> {

    private Context context;
    private List<Challenge> challenges;
    private OnChallengeActionListener listener;

    public interface OnChallengeActionListener {
        void onEditChallenge(Challenge challenge, int position);
        void onDeleteChallenge(Challenge challenge, int position);
        void onViewChallengeDetail(Challenge challenge);
    }

    public AdminChallengeAdapter(Context context, List<Challenge> challenges, OnChallengeActionListener listener) {
        this.context = context;
        this.challenges = challenges;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge challenge = challenges.get(position);

        holder.tvTitle.setText(challenge.getTitle());
        holder.tvParticipants.setText(challenge.getParticipantsText());

        // Hiển thị duration
        String duration = challenge.getDuration();
        if (duration == null || duration.isEmpty()) {
            duration = challenge.getDurationText(); // Fallback to timestamp version
        }
        holder.tvDuration.setText(duration);

        holder.tvStatus.setText(challenge.getStatusText());

        // Hiển thị ảnh
        if (challenge.getImagePath() != null && !challenge.getImagePath().isEmpty()) {
            File imageFile = new File(challenge.getImagePath());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(challenge.getImagePath());
                holder.imgChallenge.setImageBitmap(bitmap);
            } else {
                holder.imgChallenge.setImageResource(challenge.getImageResource() != 0
                        ? challenge.getImageResource() : R.drawable.challenge_1);
            }
        } else {
            holder.imgChallenge.setImageResource(challenge.getImageResource() != 0
                    ? challenge.getImageResource() : R.drawable.challenge_1);
        }

        // Set màu status
        int statusColor;
        switch (challenge.getStatusInt()) {
            case 0:
                statusColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case 1:
                statusColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case 2:
                statusColor = context.getResources().getColor(android.R.color.darker_gray);
                break;
            default:
                statusColor = context.getResources().getColor(android.R.color.black);
                break;
        }
        holder.tvStatus.setTextColor(statusColor);

        // Click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditChallenge(challenge, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteChallenge(challenge, position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewChallengeDetail(challenge);
            }
        });
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgChallenge;
        TextView tvTitle, tvParticipants, tvDuration, tvStatus;
        ImageButton btnEdit, btnDelete;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgChallenge = itemView.findViewById(R.id.img_challenge_admin);
            tvTitle = itemView.findViewById(R.id.tv_challenge_title_admin);
            tvParticipants = itemView.findViewById(R.id.tv_participants_admin);
            tvDuration = itemView.findViewById(R.id.tv_duration_admin);
            tvStatus = itemView.findViewById(R.id.tv_status_admin);
            btnEdit = itemView.findViewById(R.id.btn_edit_challenge);
            btnDelete = itemView.findViewById(R.id.btn_delete_challenge);
        }
    }
}