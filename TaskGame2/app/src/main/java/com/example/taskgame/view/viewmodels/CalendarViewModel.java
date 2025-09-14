package com.example.taskgame.view.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.domain.models.Task;
import com.example.taskgame.view.adapters.CalendarAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarViewModel extends ViewModel {

    private final TaskViewModel taskViewModel;
    private final MutableLiveData<List<CalendarAdapter.CalendarDay>> calendarDaysLiveData = new MutableLiveData<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Initialize CalendarViewModel with TaskViewModel
    public CalendarViewModel(TaskViewModel taskViewModel) {
        this.taskViewModel = taskViewModel;
        observeTasks();
    }

    // Expose the list of calendar days with tasks
    public LiveData<List<CalendarAdapter.CalendarDay>> getCalendarDaysLiveData() {
        return calendarDaysLiveData;
    }

    private void observeTasks() {
        taskViewModel.getTasks().observeForever(tasks -> {
            if (tasks != null) {
                generateCalendarDays(tasks);
            }
        });
    }

    private void generateCalendarDays(List<Task> tasks) {
        Calendar calendar = Calendar.getInstance();
        List<CalendarAdapter.CalendarDay> calendarDays = new ArrayList<>();

        // Generate the days for the month (30 days for this example, you can adjust it)
        for (int i = 1; i <= 30; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            String dateString = dateFormat.format(calendar.getTime());
            CalendarAdapter.CalendarDay calendarDay = new CalendarAdapter.CalendarDay(dateString);

            // Add tasks for this specific day
            for (Task task : tasks) {
                if (isTaskOnDay(task, calendar)) {
                    calendarDay.addTask(task);
                }
            }

            calendarDays.add(calendarDay);
        }

        calendarDaysLiveData.setValue(calendarDays);
    }

    private boolean isTaskOnDay(Task task, Calendar calendar) {
        // Check if the task is happening on the current calendar day
        long startOfDay = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        return task.getStartDateUtc() >= startOfDay && task.getStartDateUtc() < endOfDay;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        taskViewModel.getTasks().observeForever(tasks -> {
            if (tasks != null) {
                generateCalendarDays(tasks);
            }
        });
    }
}
