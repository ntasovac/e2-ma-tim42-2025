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
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.Task;
import com.example.taskgame.view.adapters.CalendarAdapter;
import com.example.taskgame.view.viewmodels.CalendarViewModel;
import com.example.taskgame.view.viewmodels.TaskViewModel;

public class CalendarFragment extends Fragment {

    private CalendarViewModel calendarViewModel;
    private CalendarAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCalendar);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CalendarAdapter(task -> {
            // Handle click on a task
            // For example: open details screen
        });
        recyclerView.setAdapter(adapter);

        // Get TaskViewModel first
        TaskViewModel taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // Create CalendarViewModel with TaskViewModel
        calendarViewModel = new CalendarViewModel(taskViewModel);

        // Observe calendar days
        calendarViewModel.getCalendarDaysLiveData().observe(getViewLifecycleOwner(), calendarDays -> {
            adapter.submitList(calendarDays);
        });
    }
}
