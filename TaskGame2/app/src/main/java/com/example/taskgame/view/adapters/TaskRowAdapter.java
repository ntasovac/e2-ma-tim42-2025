package com.example.taskgame.view.adapters;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.Task;

import java.util.Locale;

public class TaskRowAdapter extends ListAdapter<Task, TaskRowAdapter.VH> {

    public interface Listener {
        void onTaskClicked(Task t);
        void onChangeStatus(Task t, String newStatus);

        void onDelete(Task t);
    }

    private final Listener listener;

    public TaskRowAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    static final DiffUtil.ItemCallback<Task> DIFF = new DiffUtil.ItemCallback<Task>() {
        @Override public boolean areItemsTheSame(@NonNull Task a, @NonNull Task b) { return a.getId()!=null && a.getId().equals(b.getId()); }
        @Override public boolean areContentsTheSame(@NonNull Task a, @NonNull Task b) { return a.equals(b); }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_row, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Task t = getItem(pos);

        // color stripe
        h.vColor.setBackgroundColor(t.getCategoryColor());

        // time
        int m = t.getTimeOfDayMin();
        String time = String.format(Locale.getDefault(), "%02d:%02d", (m/60)%24, m%60);
        h.tvTime.setText(time);

        h.tvTitle.setText(t.getName());
        h.tvCategory.setText(t.getCategoryName() != null ? t.getCategoryName() : "Kategorija");
        h.tvStatus.setText(t.getStatus() != null ? t.getStatus() : "ACTIVE");

        h.itemView.setOnClickListener(v -> { if (listener!=null) listener.onTaskClicked(t); });
        h.btnMore.setOnClickListener(v -> showMenu(h.btnMore, t));
    }

    private void showMenu(View anchor, Task t) {
        if ("DONE".equals(t.getStatus()) || "CANCELLED".equals(t.getStatus())) {
            return; // do nothing
        }

        PopupMenu pm = new PopupMenu(anchor.getContext(), anchor);
        MenuInflater inf = pm.getMenuInflater();
        inf.inflate(R.menu.menu_task_row, pm.getMenu());
        pm.setOnMenuItemClickListener(mi -> {
            String s = null;
            int id = mi.getItemId();
            if (id == R.id.action_active) s = "ACTIVE";
            else if (id == R.id.action_done) s = "DONE";
            else if (id == R.id.action_cancelled) s = "CANCELLED";
            else if (id == R.id.action_paused) s = "PAUSED";
            else if (id == R.id.action_delete) {
                // 🔹 Special case: delete task entirely
                if (listener != null) listener.onDelete(t);
                return true;
            }
            else if (id == R.id.action_update) {
                // 🔹 Special case: delete task entirely
                if (listener != null) listener.onTaskClicked(t); // 🔹 reuse click for edit
                return true;
            }
            if (s != null && listener != null) listener.onChangeStatus(t, s);
            return true;
        });
        pm.show();
    }

    static class VH extends RecyclerView.ViewHolder {
        View vColor; TextView tvTime, tvTitle, tvCategory, tvStatus; ImageButton btnMore;
        VH(@NonNull View v) {
            super(v);
            vColor = v.findViewById(R.id.vColor);
            tvTime = v.findViewById(R.id.tvTime);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnMore = v.findViewById(R.id.btnMore);
        }
    }
}
