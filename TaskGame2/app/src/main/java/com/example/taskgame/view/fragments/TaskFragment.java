package com.example.taskgame.view.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskgame.databinding.FragmentTaskCreateBinding;
import com.example.taskgame.domain.models.Category;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.Task;
import com.example.taskgame.view.viewmodels.CategoryViewModel;
import com.example.taskgame.view.viewmodels.TaskViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment {

    private FragmentTaskCreateBinding binding;
    private TaskViewModel taskVm;
    private CategoryViewModel catVm;

    // Category state
    private final List<Category> categories = new ArrayList<>();
    private Category selectedCategory = null;

    // Frequency / unit
    private final String[] frequencyItems = {"ONE_TIME", "REPEATING"};
    private final String[] unitItems = {"DAY", "WEEK"};

    // XP mappings
    private final String[] difficultyLabels = {"Veoma lak (1)", "Lak (3)", "Težak (7)", "Ekstremno težak (20)"};
    private final int[] difficultyXp = {1, 3, 7, 20};
    private final String[] importanceLabels = {"Normalan (1)", "Važan (3)", "Ekstremno važan (10)", "Specijalan (100)"};
    private final int[] importanceXp = {1, 3, 10, 100};
    private int chosenDifficultyXp = difficultyXp[0];
    private int chosenImportanceXp = importanceXp[0];

    // Date/time state
    private Long startDateUtc = null;
    private Long endDateUtc = null;      // only for repeating
    private Integer minutesOfDay = null; // 0..1439

    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static TaskFragment newInstance() { return new TaskFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTaskCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        taskVm = new ViewModelProvider(this).get(TaskViewModel.class);
        catVm  = new ViewModelProvider(this).get(CategoryViewModel.class);

        // --- Category dropdown from Firestore ---
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        binding.actCategory.setAdapter(catAdapter);

        catVm.getCategories().observe(getViewLifecycleOwner(), list -> {
            categories.clear();
            categories.addAll(list);
            List<String> names = new ArrayList<>();
            for (Category c : list) names.add(c.getName());
            catAdapter.clear();
            catAdapter.addAll(names);
            catAdapter.notifyDataSetChanged();
        });
        catVm.startObserving();

        binding.actCategory.setOnItemClickListener((parent, view, position, id) -> selectedCategory = categories.get(position));

        // --- Frequency / unit ---
        binding.actFrequency.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, frequencyItems));
        binding.actFrequency.setText(frequencyItems[0], false);
        toggleRepeatingViews(false);
        binding.actFrequency.setOnItemClickListener((p, vv, pos, i) -> toggleRepeatingViews("REPEATING".equals(frequencyItems[pos])));

        binding.actUnit.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, unitItems));

        // --- Difficulty / Importance ---
        binding.actDifficulty.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, difficultyLabels));
        binding.actDifficulty.setText(difficultyLabels[0], false);
        binding.actDifficulty.setOnItemClickListener((p1, v1, pos, i) -> { chosenDifficultyXp = difficultyXp[pos]; updateTotalXp(); });

        binding.actImportance.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, importanceLabels));
        binding.actImportance.setText(importanceLabels[0], false);
        binding.actImportance.setOnItemClickListener((p2, v2, pos, i) -> { chosenImportanceXp = importanceXp[pos]; updateTotalXp(); });

        updateTotalXp();

        // --- Date pickers ---
        binding.tilStartDate.setEndIconOnClickListener(view -> showDatePicker(true));
        binding.etStartDate.setOnClickListener(view -> showDatePicker(true));
        binding.tilEndDate.setEndIconOnClickListener(view -> showDatePicker(false));
        binding.etEndDate.setOnClickListener(view -> showDatePicker(false));

        // --- Time picker ---
        binding.tilTime.setEndIconOnClickListener(view -> showTimePicker());
        binding.etTime.setOnClickListener(view -> showTimePicker());

        // --- Save ---
        binding.btnSave.setOnClickListener(view -> onSave());
    }

    private void toggleRepeatingViews(boolean repeating) {
        binding.tilInterval.setVisibility(repeating ? View.VISIBLE : View.GONE);
        binding.tilUnit.setVisibility(repeating ? View.VISIBLE : View.GONE);
        binding.tilEndDate.setVisibility(repeating ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker(boolean start) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(start ? "Start date" : "End date")
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selection);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long dayMillis = cal.getTimeInMillis();

            if (start) {
                startDateUtc = dayMillis;
                binding.etStartDate.setText(dateFmt.format(cal.getTime()));
            } else {
                endDateUtc = dayMillis;
                binding.etEndDate.setText(dateFmt.format(cal.getTime()));
            }
        });
        picker.show(getParentFragmentManager(), start ? "startDate" : "endDate");
    }

    private void showTimePicker() {
        MaterialTimePicker tp = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(9)
                .setMinute(0)
                .setTitleText("Time of day")
                .build();
        tp.addOnPositiveButtonClickListener(v -> {
            minutesOfDay = tp.getHour() * 60 + tp.getMinute();
            binding.etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", tp.getHour(), tp.getMinute()));
        });
        tp.show(getParentFragmentManager(), "time");
    }

    private void updateTotalXp() {
        binding.tvTotalXp.setText("Total XP: " + (chosenDifficultyXp + chosenImportanceXp));
    }

    private void onSave() {
        String name = text(binding.etName);
        if (TextUtils.isEmpty(name)) { toast("Name is required"); return; }
        if (selectedCategory == null) { toast("Select category"); return; }
        if (startDateUtc == null) { toast("Pick start date"); return; }
        if (minutesOfDay == null) { toast("Pick time"); return; }

        String frequency = text(binding.actFrequency); // "ONE_TIME" or "REPEATING"
        Integer interval = null;
        String unit = null;

        if ("REPEATING".equals(frequency)) {
            String iv = text(binding.etInterval);
            if (TextUtils.isEmpty(iv)) { toast("Interval must be >= 1"); return; }
            try {
                int ivInt = Integer.parseInt(iv);
                if (ivInt < 1) { toast("Interval must be >= 1"); return; }
                interval = ivInt;
            } catch (NumberFormatException ex) { toast("Interval must be a number"); return; }
            unit = text(binding.actUnit);
            if (TextUtils.isEmpty(unit)) { toast("Select unit"); return; }
        }

        Task t = new Task();
        String userId = SessionManager.getInstance().getUserId();
        long userLongId = Long.parseLong(userId);
        int userLevel = SessionManager.getInstance().getUser().getLevel();
        t.setUserId(userLongId);
        t.setLevel(userLevel);

        t.setName(name);
        t.setDescription(text(binding.etDesc));

        t.setCategoryId(selectedCategory.getId());
        t.setCategoryName(selectedCategory.getName());
        t.setCategoryColor(selectedCategory.getColor());

        t.setFrequency(frequency);
        t.setInterval(interval);
        t.setUnit(unit);
        t.setStartDateUtc(startDateUtc);
        t.setEndDateUtc(endDateUtc);
        t.setTimeOfDayMin(minutesOfDay);

        t.setDifficultyXp(chosenDifficultyXp);
        t.setImportanceXp(chosenImportanceXp);
        t.setTotalXp(chosenDifficultyXp + chosenImportanceXp);
        t.setCreatedAtUtc(System.currentTimeMillis());

        binding.btnSave.setEnabled(false);
        taskVm.createTask(t, new TaskViewModel.Result() {
            @Override public void ok(String id) {
                binding.btnSave.setEnabled(true);
                toast("Task created");
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
                // Optionally: clear form or navigate up
            }
            @Override public void error(Exception e) {
                binding.btnSave.setEnabled(true);
                toast(e != null && e.getMessage() != null ? e.getMessage() : "Save failed");
            }
        });
    }

    private String text(CharSequence cs) { return cs == null ? "" : cs.toString().trim(); }
    private String text(android.widget.TextView tv) { return text(tv.getText()); }
    private void toast(String msg) { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
