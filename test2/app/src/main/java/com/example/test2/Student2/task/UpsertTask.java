// src/main/java/com/example/test2/task/UpsertTask.java
package com.example.test2.Student2.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.test2.R;
import com.example.test2.Student2.category.Category;
import com.example.test2.Student2.category.CategoryDAO;
import com.example.test2.Student2.category.CategoryDatabase;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UpsertTask extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id"; // int

    private TaskDatabase taskDb;
    private TaskDAO taskDao;
    private CategoryDatabase categoryDb;
    private CategoryDAO categoryDao;
    private final Executor io = Executors.newSingleThreadExecutor();

    // UI
    private TextView tvTitle;
    private EditText etName, etDescription, etRepeatInterval, etExecuteAt, etRepeatStart, etRepeatEnd;
    private Button btnStartDate, btnEndDate;
    private Spinner spCategory, spDifficulty, spImportance, spRepeatUnit;
    private CheckBox cbRecurring;
    private Button btnSave, btnBack;

    // Data
    private Integer editingId = null;
    private Task loadedTask;
    private List<Category> allCategories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upsert_task);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // DB
        taskDb = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task").build();
        taskDao = taskDb.taskDAO();
        categoryDb = Room.databaseBuilder(getApplicationContext(), CategoryDatabase.class, "category").build();
        categoryDao = categoryDb.categoryDAO();

        // Bind views
        tvTitle = findViewById(R.id.tvTitle);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        spDifficulty = findViewById(R.id.spDifficulty);
        spImportance = findViewById(R.id.spImportance);
        cbRecurring = findViewById(R.id.cbRecurring);
        etRepeatInterval = findViewById(R.id.etRepeatInterval);
        spRepeatUnit = findViewById(R.id.spRepeatUnit);
        etExecuteAt = findViewById(R.id.etExecuteAt);
        //etRepeatStart = findViewById(R.id.etRepeatStart);
        //etRepeatEnd = findViewById(R.id.etRepeatEnd);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Load categories
        io.execute(() -> {
            allCategories = categoryDao.getAll();
            runOnUiThread(() -> {
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        allCategories.stream().map(c -> c.name).toArray(String[]::new)
                );
                catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategory.setAdapter(catAdapter);
            });
        });

        btnStartDate = findViewById(R.id.datePickerStart);

        btnStartDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    this, // current Activity context
                    (view, year1, month1, dayOfMonth) -> {
                        month1 = month1 + 1; // adjust because month is 0-based
                        btnStartDate.setText(dayOfMonth + "/" + month1 + "/" + year1);
                    },
                    year, month, day
            );
            dialog.show();
        });

        btnEndDate = findViewById(R.id.datePickerEnd);

        btnEndDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    this, // current Activity context
                    (view, year1, month1, dayOfMonth) -> {
                        month1 = month1 + 1; // adjust because month is 0-based
                        btnEndDate.setText(dayOfMonth + "/" + month1 + "/" + year1);
                    },
                    year, month, day
            );
            dialog.show();
        });



        // Enum spinners
        spDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.Difficulty.values()));
        spImportance.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.Importance.values()));
        spRepeatUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.RepeatUnit.values()));

        // Edit or Add
        int id = getIntent() != null ? getIntent().getIntExtra(EXTRA_TASK_ID, -1) : -1;
        if (id > 0) {
            editingId = id;
            tvTitle.setText("Edit Task");
            loadTask(id);
        } else {
            tvTitle.setText("Add Task");
        }

        btnSave.setOnClickListener(v -> saveTask());
    }

    private void loadTask(int id) {
        io.execute(() -> {
            Task task = taskDao.getById(id);
            Category selected = null;
            if (task != null) {
                selected = categoryDao.getById(task.getCategoryId());
            }
            Category finalSelected = selected;
            runOnUiThread(() -> {
                if (task == null) {
                    Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                loadedTask = task;

                etName.setText(task.getName());
                etDescription.setText(task.getDescription());

                // Set spinner to the matching category
                if (allCategories != null && finalSelected != null) {
                    for (int i = 0; i < allCategories.size(); i++) {
                        if (allCategories.get(i).id == finalSelected.id) {
                            spCategory.setSelection(i);
                            break;
                        }
                    }
                }

                spDifficulty.setSelection(task.getDifficulty().ordinal());
                spImportance.setSelection(task.getImportance().ordinal());
                cbRecurring.setChecked(task.isRecurring());
                etRepeatInterval.setText(task.getRepeatInterval() != null ? String.valueOf(task.getRepeatInterval()) : "");
                if (task.getRepeatUnit() != null) spRepeatUnit.setSelection(task.getRepeatUnit().ordinal());

                etExecuteAt.setText(task.getExecuteAt() != null ? String.valueOf(task.getExecuteAt()) : "");
                etRepeatStart.setText(task.getRepeatStart() != null ? String.valueOf(task.getRepeatStart()) : "");
                etRepeatEnd.setText(task.getRepeatEnd() != null ? String.valueOf(task.getRepeatEnd()) : "");
            });
        });
    }

    private void saveTask() {
        // Validate
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (allCategories == null || allCategories.isEmpty()) {
            Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show();
            return;
        }
        int catIndex = spCategory.getSelectedItemPosition();
        if (catIndex < 0 || catIndex >= allCategories.size()) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            return;
        }
        Category selectedCat = allCategories.get(spCategory.getSelectedItemPosition());
        Task.Difficulty diff = (Task.Difficulty) spDifficulty.getSelectedItem();
        Task.Importance imp = (Task.Importance) spImportance.getSelectedItem();

        boolean recurring = cbRecurring.isChecked();
        Integer repeatInterval = parseIntOrNull(etRepeatInterval.getText().toString());
        Task.RepeatUnit repeatUnit = (Task.RepeatUnit) spRepeatUnit.getSelectedItem();

        Long executeAtMs   = parseLongOrNull(etExecuteAt.getText().toString());
        //Long repeatStartMs = parseLongOrNull(etRepeatStart.getText().toString());
        //Long repeatEndMs   = parseLongOrNull(etRepeatEnd.getText().toString());;

        Task task = (editingId != null) ? (loadedTask != null ? loadedTask : new Task()) : new Task();
        if (editingId != null) task.setId(editingId);

        task.setName(etName.getText().toString().trim());
        task.setDescription(etDescription.getText().toString());
        task.setCategoryId(selectedCat.id); // << set FK
        task.setDifficulty(diff);
        task.setImportance(imp);
        task.setRecurring(recurring);
        task.setRepeatInterval(repeatInterval);
        task.setRepeatUnit(repeatUnit);
        task.setExecuteAt(executeAtMs);
        task.setRepeatStart(btnStartDate.getText().toString());
        task.setRepeatEnd(btnEndDate.getText().toString());

        io.execute(() -> {
            taskDao.upsert(task);
            runOnUiThread(() -> {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    // ---------- helpers ----------
    private static Integer parseIntOrNull(String s) {
        try { return (s == null || s.isEmpty()) ? null : Integer.parseInt(s); }
        catch (NumberFormatException e) { return null; }
    }

    private static Long parseLongOrNull(String s) {
        try { return (s == null || s.isEmpty()) ? null : Long.parseLong(s); }
        catch (NumberFormatException e) { return null; }
    }
}
