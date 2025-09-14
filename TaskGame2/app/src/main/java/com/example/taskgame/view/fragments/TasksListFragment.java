package com.example.taskgame.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskgame.databinding.FragmentTasksListBinding;
import com.example.taskgame.domain.models.Task;
import com.example.taskgame.view.adapters.TaskRowAdapter;
import com.example.taskgame.view.viewmodels.TaskViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TasksListFragment extends Fragment {

    private FragmentTasksListBinding binding;
    private TaskViewModel vm;
    private final TaskRowAdapter adapter = new TaskRowAdapter(new TaskRowAdapter.Listener() {
        @Override public void onTaskClicked(Task t) { /* TODO: open details */ }
        @Override public void onChangeStatus(Task t, String s) {
            t.setStatus(s);
            vm.updateTask(t, new TaskViewModel.VoidResult() { @Override public void ok() { } @Override public void error(Exception e) { }});
        }
    });

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle b) {
        binding = FragmentTasksListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        vm = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        vm.startObserving();

        binding.rvList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvList.setAdapter(adapter);

        binding.chips.setOnCheckedChangeListener((group, id) -> apply(vm.getTasks().getValue()));
        vm.getTasks().observe(getViewLifecycleOwner(), this::apply);
    }

    private void apply(List<Task> all) {
        if (all == null) all = new ArrayList<>();

        // only current & future
        Calendar today = Calendar.getInstance(); zeroTime(today);
        long todayMs = today.getTimeInMillis();

        List<Task> list = new ArrayList<>();
        for (Task t : all) {
            boolean inFutureOrToday;
            if ("ONE_TIME".equals(t.getFrequency())) {
                inFutureOrToday = t.getStartDateUtc() >= todayMs;
            } else {
                // repeating: visible if end null or ends today/future
                inFutureOrToday = (t.getEndDateUtc() == null) || (t.getEndDateUtc() >= todayMs);
            }
            if (!inFutureOrToday) continue;

            int checkedId = binding.chips.getCheckedChipId();
            boolean passType = (checkedId == binding.chAll.getId())
                    || (checkedId == binding.chOneTime.getId() && "ONE_TIME".equals(t.getFrequency()))
                    || (checkedId == binding.chRepeating.getId() && "REPEATING".equals(t.getFrequency()));
            if (passType) list.add(t);
        }

        // sort: nearest first (by startDate then time)
        Collections.sort(list, (a, b) -> {
            int cmp = Long.compare(a.getStartDateUtc(), b.getStartDateUtc());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getTimeOfDayMin(), b.getTimeOfDayMin());
        });

        adapter.submitList(list);
    }

    private static void zeroTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
