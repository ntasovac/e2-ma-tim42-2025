package com.example.test2.Student2.task;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.test2.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskList extends AppCompatActivity {

    private TaskDatabase taskDb;
    private TaskDAO taskDao;

    private final Executor io = Executors.newSingleThreadExecutor();

    private RadioGroup rgFilter;
    private RadioButton rbOneOff, rbRecurring;
    private RecyclerView rv;
    private SimpleTaskAdapter adapter;

    private Executor executor = Executors.newSingleThreadExecutor();

    private final List<Task> allTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // DB
        taskDb = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task")
                .fallbackToDestructiveMigration()
                .build();
        taskDao = taskDb.taskDAO();
        //taskDao.deleteAll();

        // Views
        rgFilter = findViewById(R.id.rgFilter);
        rbOneOff = findViewById(R.id.rbOneOff);
        rbRecurring = findViewById(R.id.rbRecurring);
        rv = findViewById(R.id.rvTasks);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleTaskAdapter();
        rv.setAdapter(adapter);

        rgFilter.setOnCheckedChangeListener((group, checkedId) -> applyFilter());

        loadAllTasks();
    }

    private void loadAllTasks() {
        io.execute(() -> {
            List<Task> data = taskDao.getAll(); // load ALL tasks
            runOnUiThread(() -> {
                allTasks.clear();
                if (data != null) allTasks.addAll(data);
                applyFilter();
            });
        });
    }

    private void applyFilter() {
        boolean showOneOff = rbOneOff.isChecked();
        List<Task> filtered = new ArrayList<>();
        for (Task t : allTasks) {
            if (showOneOff && !t.isRecurring()) filtered.add(t);
            if (!showOneOff && t.isRecurring()) filtered.add(t);
        }
        adapter.setData(filtered);
    }

    // ---------------- minimal adapter (single TextView rows) ----------------
    static class SimpleTaskAdapter extends RecyclerView.Adapter<SimpleTaskAdapter.VH> {
        private final List<Task> items = new ArrayList<>();

        void setData(List<Task> data) {
            items.clear();
            if (data != null) items.addAll(data);
            notifyDataSetChanged();
        }

        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.widget.TextView tv = new android.widget.TextView(parent.getContext());
            tv.setPadding(16, 16, 16, 16);
            tv.setMaxLines(4);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            return new VH(tv);
        }

        @Override public void onBindViewHolder(VH h, int pos) {
            Task t = items.get(pos);

            int xp = (t.getDifficulty() != null ? t.xpForDifficulty(t.getDifficulty()) : 0)
                    + (t.getImportance() != null ? t.xpForImportance(t.getImportance()) : 0);

            String schedule = t.isRecurring()
                    ? "Ponavljajući • svaka " + (t.getRepeatInterval() != null ? t.getRepeatInterval() : "?")
                    + " " + (t.getRepeatUnit() != null ? t.getRepeatUnit().name() : "?")
                    + " | " + (t.getRepeatStart()) + " → " + (t.getRepeatEnd() == null ? "open" : (t.getRepeatEnd()))
                    : "Jednokratni • " + (t.getExecuteAt() != null ? fmt(t.getExecuteAt()) : "—");

            String text = (t.getName() == null ? "" : t.getName())
                    + (TextUtils.isEmpty(t.getDescription()) ? "" : "\n" + t.getDescription())
                    + "\nXP: " + xp
                    + "\n" + schedule;

            h.tv.setText(text);
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final android.widget.TextView tv;
            VH(android.widget.TextView v) { super(v); tv = v; }
        }

        private static String fmt(Long epochMillis) {
            return epochMillis == null ? "—" : DateFormat.format("yyyy-MM-dd HH:mm", epochMillis).toString();
        }
    }
}
