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

public class TaskUpdateFragment extends Fragment {

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

    // Task being edited
    private Task editingTask;

    public static TaskUpdateFragment newInstance(Task task) {
        TaskUpdateFragment f = new TaskUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("task", task); // Task must implement Serializable or Parcelable
        f.setArguments(args);
        return f;
    }

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

        // load Task from args
        if (getArguments() != null) {
            editingTask = (Task) getArguments().getSerializable("task");
        }

        // --- Category dropdown ---
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
        binding.actUnit.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, unitItems));

        // difficulty & importance
        binding.actDifficulty.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, difficultyLabels));
        binding.actImportance.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, importanceLabels));

        // listeners
        binding.actDifficulty.setOnItemClickListener((p1, v1, pos, i) -> { chosenDifficultyXp = difficultyXp[pos]; updateTotalXp(); });
        binding.actImportance.setOnItemClickListener((p2, v2, pos, i) -> { chosenImportanceXp = importanceXp[pos]; updateTotalXp(); });

        // date/time pickers
        binding.tilStartDate.setEndIconOnClickListener(view -> showDatePicker(true));
        binding.etStartDate.setOnClickListener(view -> showDatePicker(true));
        binding.tilEndDate.setEndIconOnClickListener(view -> showDatePicker(false));
        binding.etEndDate.setOnClickListener(view -> showDatePicker(false));
        binding.tilTime.setEndIconOnClickListener(view -> showTimePicker());
        binding.etTime.setOnClickListener(view -> showTimePicker());

        // prefill
        if (editingTask != null) fillForm(editingTask);

        // save/update
        binding.btnSave.setText("Update Task");
        binding.btnSave.setOnClickListener(view -> onUpdate());
    }

    private void fillForm(Task t) {
        binding.etName.setText(t.getName());
        binding.etDesc.setText(t.getDescription());

        startDateUtc = t.getStartDateUtc();
        endDateUtc = t.getEndDateUtc();
        minutesOfDay = t.getTimeOfDayMin();

        if (startDateUtc != null) binding.etStartDate.setText(dateFmt.format(startDateUtc));
        if (endDateUtc != null) binding.etEndDate.setText(dateFmt.format(endDateUtc));
        if (minutesOfDay != null) {
            int h = minutesOfDay / 60, m = minutesOfDay % 60;
            binding.etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        }

        binding.actFrequency.setText(t.getFrequency(), false);
        toggleRepeatingViews("REPEATING".equals(t.getFrequency()));
        if (t.getInterval() != null) binding.etInterval.setText(String.valueOf(t.getInterval()));
        if (t.getUnit() != null) binding.actUnit.setText(t.getUnit(), false);


        binding.actCategory.setText(t.getCategoryName(), false);

        // Difficulty
        for (int i = 0; i < difficultyXp.length; i++) {
            if (difficultyXp[i] == t.getDifficultyXp()) {
                chosenDifficultyXp = difficultyXp[i];
                binding.actDifficulty.setText(difficultyLabels[i], false); // 👈 show label
                break;
            }
        }

        // Importance
        for (int i = 0; i < importanceXp.length; i++) {
            if (importanceXp[i] == t.getImportanceXp()) {
                chosenImportanceXp = importanceXp[i];
                binding.actImportance.setText(importanceLabels[i], false); // 👈 show label
                break;
            }
        }

        // difficulty
        chosenDifficultyXp = t.getDifficultyXp();
        chosenImportanceXp = t.getImportanceXp();
        updateTotalXp();


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

    private void onUpdate() {
        String name = text(binding.etName);
        if (TextUtils.isEmpty(name)) return;
        if (selectedCategory == null) return;
        if (startDateUtc == null) return;
        if (minutesOfDay == null) return;

        String frequency = text(binding.actFrequency);
        Integer interval = null;
        String unit = null;

        if ("REPEATING".equals(frequency)) {
            String iv = text(binding.etInterval);
            if (!TextUtils.isEmpty(iv)) interval = Integer.parseInt(iv);
            unit = text(binding.actUnit);
        }

        editingTask.setName(name);
        editingTask.setDescription(text(binding.etDesc));
        editingTask.setCategoryId(selectedCategory.getId());
        editingTask.setCategoryName(selectedCategory.getName());
        editingTask.setCategoryColor(selectedCategory.getColor());
        editingTask.setFrequency(frequency);
        editingTask.setInterval(interval);
        editingTask.setUnit(unit);
        editingTask.setStartDateUtc(startDateUtc);
        editingTask.setEndDateUtc(endDateUtc);
        editingTask.setTimeOfDayMin(minutesOfDay);
        editingTask.setDifficultyXp(chosenDifficultyXp);
        editingTask.setImportanceXp(chosenImportanceXp);
        editingTask.setTotalXp(chosenDifficultyXp + chosenImportanceXp);

        binding.btnSave.setEnabled(false);
        taskVm.updateTask(editingTask, new TaskViewModel.VoidResult() {


            @Override public void ok() {
                //binding.btnSave.setEnabled(true);
                toast("Task created");
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
                // Optionally: clear form or navigate up
            }
            @Override public void error(Exception e) { binding.btnSave.setEnabled(true); }
        });
    }

    private void toast(String msg) { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); }


    private String text(CharSequence cs) { return cs == null ? "" : cs.toString().trim(); }
    private String text(android.widget.TextView tv) { return text(tv.getText()); }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
