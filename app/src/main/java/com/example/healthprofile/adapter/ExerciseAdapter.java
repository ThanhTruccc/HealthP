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
import com.example.healthprofile.model.Exercise;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private Context context;
    private List<Exercise> exerciseList;
    private OnExerciseActionListener listener;

    public interface OnExerciseActionListener {
        void onEdit(Exercise exercise);
        void onDelete(Exercise exercise);
    }

    public ExerciseAdapter(Context context, List<Exercise> exerciseList, OnExerciseActionListener listener) {
        this.context = context;
        this.exerciseList = exerciseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);

        // Set exercise type and icon
        holder.tvExerciseType.setText(exercise.getExerciseType());
        holder.ivExerciseIcon.setImageResource(getExerciseIcon(exercise.getExerciseType()));

        // Set duration
        holder.tvDuration.setText(exercise.getDurationMinutes() + " phút");

        // Set calories
        holder.tvCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", exercise.getCaloriesBurned()));

        // Set date (format friendly)
        holder.tvDate.setText(formatDate(exercise.getDate()));

        // Set notes
        if (exercise.getNotes() != null && !exercise.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(exercise.getNotes());
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Set listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(exercise);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    private int getExerciseIcon(String type) {
        switch (type) {
            case "Chạy bộ":
                return android.R.drawable.ic_media_play;
            case "Đi bộ":
                return android.R.drawable.ic_menu_directions;
            case "Đạp xe":
            case "Bơi lội":
                return android.R.drawable.ic_menu_compass;
            case "Yoga":
                return android.R.drawable.ic_menu_mylocation;
            case "Gym":
            case "Tập tạ":
                return android.R.drawable.ic_menu_preferences;
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);

            // Check if today
            Calendar today = Calendar.getInstance();
            Calendar exerciseDate = Calendar.getInstance();
            exerciseDate.setTime(date);

            if (today.get(Calendar.YEAR) == exerciseDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == exerciseDate.get(Calendar.DAY_OF_YEAR)) {
                return "Hôm nay";
            }

            // Check if yesterday
            today.add(Calendar.DAY_OF_YEAR, -1);
            if (today.get(Calendar.YEAR) == exerciseDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == exerciseDate.get(Calendar.DAY_OF_YEAR)) {
                return "Hôm qua";
            }

            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivExerciseIcon;
        TextView tvExerciseType, tvDuration, tvCalories, tvDate, tvNotes;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExerciseIcon = itemView.findViewById(R.id.iv_exercise_icon);
            tvExerciseType = itemView.findViewById(R.id.tv_exercise_type);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvDate = itemView.findViewById(R.id.tv_exercise_date);
            tvNotes = itemView.findViewById(R.id.tv_exercise_notes);
            btnEdit = itemView.findViewById(R.id.btn_edit_exercise);
            btnDelete = itemView.findViewById(R.id.btn_delete_exercise);
        }
    }

    private static class Calendar {
        private static final int YEAR = 1;
        private static final int DAY_OF_YEAR = 6;

        static Calendar getInstance() {
            return new Calendar();
        }

        void setTime(Date date) {}

        int get(int field) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            return cal.get(field);
        }

        void add(int field, int amount) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(field, amount);
        }
    }
}