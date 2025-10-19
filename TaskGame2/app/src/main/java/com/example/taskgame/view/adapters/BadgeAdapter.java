package com.example.taskgame.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.Badge;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final Context context;
    private final List<Badge> badges;

    public BadgeAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);
        holder.badgeName.setText(badge.getName());
        holder.badgeCount.setText(" " + badge.getCount());
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        TextView badgeName, badgeCount;

        BadgeViewHolder(View itemView) {
            super(itemView);
            badgeName = itemView.findViewById(R.id.badgeName);
            badgeCount = itemView.findViewById(R.id.badgeCount);
        }
    }
}
