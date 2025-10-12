package com.example.taskgame;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.taskgame.data.repositories.BossRepository;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BossRepository bossRepository = new BossRepository();
        //bossRepository.createBossIfNotExists();
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel_id";
            String channelName = "General Notifications";
            String channelDescription = "Default channel for FCM notifications";

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(channelDescription);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
