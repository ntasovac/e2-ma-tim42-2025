package com.example.taskgame.view.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.Category;

public class CategoryListAdapter extends ListAdapter<Category, CategoryListAdapter.VH> {

    public interface OnClick { void onClick(Category c); }
    private final OnClick onClick;

    public CategoryListAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF = new DiffUtil.ItemCallback<Category>() {
        @Override public boolean areItemsTheSame(@NonNull Category a, @NonNull Category b) { return a.getId() == b.getId(); }
        @Override public boolean areContentsTheSame(@NonNull Category a, @NonNull Category b) {
            return a.getName().equals(b.getName()) && a.getColor() == b.getColor();
        }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Category c = getItem(position);
        h.tvName.setText(c.getName());

        // tint the circle
        GradientDrawable bg = (GradientDrawable) h.vColor.getBackground();
        bg.setColor(c.getColor());

        h.itemView.setOnClickListener(v -> { if (onClick != null) onClick.onClick(c); });
    }

    static class VH extends RecyclerView.ViewHolder {
        final View vColor;
        final TextView tvName;
        VH(@NonNull View itemView) {
            super(itemView);
            vColor = itemView.findViewById(R.id.vColor);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
