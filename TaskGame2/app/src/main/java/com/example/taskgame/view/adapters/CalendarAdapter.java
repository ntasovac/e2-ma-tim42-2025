package com.example.taskgame.view.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.Task;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends ListAdapter<CalendarAdapter.CalendarDay, CalendarAdapter.VH> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private final OnTaskClickListener onTaskClickListener;

    public CalendarAdapter(OnTaskClickListener onTaskClickListener) {
        super(DIFF_CALLBACK);
        this.onTaskClickListener = onTaskClickListener;
    }

    private static final DiffUtil.ItemCallback<CalendarDay> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CalendarDay>() {
                @Override
                public boolean areItemsTheSame(@NonNull CalendarDay oldItem, @NonNull CalendarDay newItem) {
                    return oldItem.getDate().equals(newItem.getDate());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CalendarDay oldItem, @NonNull CalendarDay newItem) {
                    return oldItem.getTasks().equals(newItem.getTasks());
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CalendarDay calendarDay = getItem(position);
        holder.tvDate.setText(calendarDay.getDate());

        // Clear old tasks
        holder.taskContainer.removeAllViews();

        // Add tasks for the day
        for (Task task : calendarDay.getTasks()) {
            View taskView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_task, holder.taskContainer, false);

            TextView taskNameTextView = taskView.findViewById(R.id.taskName);
            taskNameTextView.setText(task.getName());

            // Set background color (assuming TaskCategory color is an int)
            GradientDrawable bg = (GradientDrawable) taskView.getBackground();
            bg.setColor(task.getCategoryColor());

            taskView.setOnClickListener(v -> onTaskClickListener.onTaskClick(task));

            holder.taskContainer.addView(taskView);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvDate;
        final LinearLayout taskContainer;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            taskContainer = itemView.findViewById(R.id.taskContainer);
        }
    }

    // Model class for one calendar day
    public static class CalendarDay {
        private final String date;
        private final List<Task> tasks;

        public CalendarDay(String date) {
            this.date = date;
            this.tasks = new ArrayList<>();
        }

        public String getDate() {
            return date;
        }

        public List<Task> getTasks() {
            return tasks;
        }

        public void addTask(Task task) {
            this.tasks.add(task);
        }
    }
}
