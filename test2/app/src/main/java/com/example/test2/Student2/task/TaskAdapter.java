package com.example.test2.Student2.task;

import android.graphics.Color;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test2.Student2.category.Category;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    /** Provide the category for a given categoryId (so adapter doesn’t hit DB directly). */
    public interface CategoryResolver {
        @Nullable Category resolve(int categoryId);
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final CategoryResolver categoryResolver;
    private final OnTaskClickListener clickListener;

    public TaskAdapter(List<Task> initial,
                       CategoryResolver categoryResolver,
                       OnTaskClickListener clickListener) {
        if (initial != null) tasks.addAll(initial);
        this.categoryResolver = categoryResolver;
        this.clickListener = clickListener;
    }

    // -------- public helpers --------
    public void setItems(List<Task> items) {
        tasks.clear();
        if (items != null) tasks.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(Task t) {
        if (t == null) return;
        tasks.add(t);
        notifyItemInserted(tasks.size() - 1);
    }

    public Task getItem(int position) {
        return (position >= 0 && position < tasks.size()) ? tasks.get(position) : null;
    }

    // -------- RecyclerView.Adapter --------
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
        tv.setPadding(16, 16, 16, 16);
        tv.setMaxLines(5);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task t = tasks.get(position);

        // Resolve category (name & color) from categoryId
        Category cat = categoryResolver != null ? categoryResolver.resolve(t.getCategoryId()) : null;
        String catName = (cat != null && !TextUtils.isEmpty(cat.name)) ? cat.name : "—";
        String catColor = (cat != null && !TextUtils.isEmpty(cat.colorHex)) ? cat.colorHex : "#EEEEEE";

        // Compute XP from enums (since enums don’t contain xp field)
        int xp = xpForDifficulty(t.getDifficulty()) + xpForImportance(t.getImportance());

        // Build schedule string
        String schedule = t.isRecurring()
                ? "Ponavljajući • svaka " + (t.getRepeatInterval() != null ? t.getRepeatInterval() : "?")
                + " " + (t.getRepeatUnit() != null ? t.getRepeatUnit().name() : "?")
                + " | " + (t.getRepeatStart()) + " → " + (t.getRepeatEnd() == null ? "open" : (t.getRepeatEnd()))
                : "Jednokratni • " + (t.getExecuteAt() != null ? fmt(t.getExecuteAt()) : "—");

        String difficulty = t.getDifficulty() != null ? t.getDifficulty().name() : "N/A";
        String importance = t.getImportance() != null ? t.getImportance().name() : "N/A";
        String status = t.getStatus() != null ? t.getStatus().name() : "N/A";

        String text = safe(t.getName())
                + (TextUtils.isEmpty(t.getDescription()) ? "" : "\n" + t.getDescription())
                + "\nKategorija: " + catName + "   |   XP: " + xp
                + "\nTežina: " + difficulty + "   |   Bitnost: " + importance
                + "\nStatus: " + status
                + "\n" + schedule;

        holder.textView.setText(text);

        try {
            holder.textView.setBackgroundColor(Color.parseColor(catColor));
        } catch (IllegalArgumentException e) {
            holder.textView.setBackgroundColor(Color.parseColor("#EEEEEE"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onTaskClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // -------- ViewHolder --------
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        ViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    // -------- small utils --------
    private static String fmt(Long epochMillis) {
        return epochMillis == null ? "—" : DateFormat.format("yyyy-MM-dd HH:mm", epochMillis).toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static int xpForDifficulty(Task.Difficulty d) {
        if (d == null) return 0;
        switch (d) {
            case VERY_EASY: return 1;
            case EASY:      return 3;
            case HARD:      return 7;
            case EXTREME:   return 20;
            default:        return 0;
        }
    }

    private static int xpForImportance(Task.Importance i) {
        if (i == null) return 0;
        switch (i) {
            case NORMAL:         return 1;
            case IMPORTANT:      return 3;
            case VERY_IMPORTANT: return 10;
            case SPECIAL:        return 100;
            default:             return 0;
        }
    }
}
