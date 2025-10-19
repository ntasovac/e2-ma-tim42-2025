package com.example.taskgame.view.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.taskgame.data.repositories.TaskRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class StatisticsViewModel extends AndroidViewModel {

    private final SharedPreferences prefs;
    private final TaskRepository repository;
    private final MutableLiveData<TaskStats> taskStats = new MutableLiveData<>();
    private final MutableLiveData<Integer> streakCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> taskStreakCount = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> taskCategoryStats = new MutableLiveData<>();
    private final MutableLiveData<Map<Integer, Integer>> weeklyXpStats = new MutableLiveData<>();


    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences("app_prefs", 0);
        repository = new TaskRepository();
    }

    public LiveData<TaskStats> getTaskStats() {
        return taskStats;
    }
    public LiveData<Map<Integer, Integer>> getWeeklyXpStats() {return weeklyXpStats;}

    public void loadTaskStatistics(String userId) {
        final int[] done = {0};
        final int[] active = {0};
        final int[] cancelled = {0};

        repository.getDoneTasks(userId, new TaskRepository.GetTasksCallback() {
            @Override
            public void onSuccess(int doneCount) {
                done[0] = doneCount;
                repository.getActiveTasks(userId, new TaskRepository.GetTasksCallback() {
                    @Override
                    public void onSuccess(int activeCount) {
                        active[0] = activeCount;
                        repository.getCancelledTasks(userId, new TaskRepository.GetTasksCallback() {
                            @Override
                            public void onSuccess(int cancelledCount) {
                                cancelled[0] = cancelledCount;

                                int total = done[0] + active[0] + cancelled[0];
                                taskStats.setValue(new TaskStats(total, done[0], active[0], cancelled[0]));
                            }

                            @Override
                            public void onFailure(Exception e) {
                                taskStats.setValue(new TaskStats(0, 0, 0, 0));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        taskStats.setValue(new TaskStats(0, 0, 0, 0));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                taskStats.setValue(new TaskStats(0, 0, 0, 0));
            }
        });
    }
    public LiveData<Integer> getStreakCount() {
        return streakCount;
    }

    public LiveData<Integer> getTaskStreakCount() {
        return taskStreakCount;
    }

    public LiveData<Map<String, Integer>> getTaskCategoryStats() {
        return taskCategoryStats;
    }


    public void updateStreak() {
        String lastDateString = prefs.getString("last_open_date", null);
        LocalDate currentDate = LocalDate.now();

        if (lastDateString == null) {
            prefs.edit()
                    .putString("last_open_date", currentDate.toString())
                    .putInt("streak_count", 1)
                    .apply();
            streakCount.setValue(1);
            return;
        }

        LocalDate lastDate = LocalDate.parse(lastDateString);
        long daysBetween = ChronoUnit.DAYS.between(lastDate, currentDate);
        int currentStreak;

        if (daysBetween == 0) {
            currentStreak = prefs.getInt("streak_count", 1);
        } else if (daysBetween == 1) {
            currentStreak = prefs.getInt("streak_count", 1) + 1;
        } else {
            currentStreak = 1;
        }

        prefs.edit()
                .putString("last_open_date", currentDate.toString())
                .putInt("streak_count", currentStreak)
                .apply();

        streakCount.setValue(currentStreak);
    }

    public void updateTaskStreak(String userId){
        repository.getTaskStreak(userId, new TaskRepository.GetTasksCallback() {
            @Override
            public void onSuccess(int count) {
                taskStreakCount.setValue(count);
            }

            @Override
            public void onFailure(Exception e) {
                taskStreakCount.setValue(null);
            }
        });
    }
    public void updateTaskCategoryStats(String userId) {
        repository.getDoneTasksAndCategories(userId, new TaskRepository.GetCategoriesCallback() {
            @Override
            public void onSuccess(Map<String, Integer> categories) {
                taskCategoryStats.setValue(categories);
            }

            @Override
            public void onFailure(Exception e) {
                taskCategoryStats.setValue(new HashMap<>());
            }
        });
    }
    public void updateWeeklyXpStats(String userId) {
        repository.getDoneTasksWeek(userId, new TaskRepository.GetDoneTasksWeekCallback() {
            @Override
            public void onSuccess(Map<Integer, Integer> xpPerDay) {
                weeklyXpStats.setValue(xpPerDay);
            }

            @Override
            public void onFailure(Exception e) {
                weeklyXpStats.setValue(new HashMap<>());
            }
        });
    }

    public static class TaskStats {
        public final int total;
        public final int done;
        public final int active;
        public final int cancelled;

        public TaskStats(int total, int done, int active, int cancelled) {
            this.total = total;
            this.done = done;
            this.active = active;
            this.cancelled = cancelled;
        }
    }
}
