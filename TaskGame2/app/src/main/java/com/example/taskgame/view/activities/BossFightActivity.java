package com.example.taskgame.view.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.taskgame.R;

public class BossFightActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new com.example.taskgame.view.fragments.BossFightFragment())
                    .commit();
        }
    }
}
