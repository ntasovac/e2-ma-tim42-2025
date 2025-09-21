package com.example.taskgame;

import android.app.Application;

import com.example.taskgame.data.repositories.BossRepository;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BossRepository bossRepository = new BossRepository();
        bossRepository.createBossIfNotExists();
    }
}
