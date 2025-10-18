package com.example.taskgame.view.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class StatisticsViewModel extends AndroidViewModel {

    private final SharedPreferences prefs;
    private final MutableLiveData<Integer> streakCount = new MutableLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences("app_prefs", 0);
    }

    public LiveData<Integer> getStreakCount() {
        return streakCount;
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
}
