package com.example.taskgame.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.example.taskgame.R;
import com.example.taskgame.data.repositories.BossRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.Equipment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BossFightActivity extends AppCompatActivity {

    private final UserRepository userRepository;

    public BossFightActivity(){
        userRepository = new UserRepository();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);
        Button claimPrizeButton = findViewById(R.id.claim_prize_button);

        userRepository.decreaseUsageCountAndRemove();

        claimPrizeButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference bossRef = db.collection("boss").document("levelBoss");
            bossRef.get()
                    .addOnSuccessListener(bossSnap -> {
                        Boss boss = bossSnap.toObject(Boss.class);
                        if (boss == null) {
                            Toast.makeText(this, "Boss data unavailable.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        userRepository.claimPrize(task -> {
                            if (!task.isSuccessful()) {
                                Toast.makeText(this,
                                        "Error claiming prize: " +
                                                (task.getException() != null ? task.getException().getMessage() : ""),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            Equipment equipment = task.getResult();
                            int coins;
                            if (boss.getBonus() == 0) {
                                coins = boss.getCoinReward();
                            } else {
                                coins = boss.getCoinReward() +
                                        (int) (boss.getCoinReward() * (boss.getBonus() / 100.0));
                            }

                            if (equipment != null) {
                                Toast.makeText(this,
                                        "Prize claimed! You won  " + equipment.getName().toUpperCase() +
                                                " and " + coins + " coins!",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this,
                                        "Prize claimed! You won " + coins + " coins!",
                                        Toast.LENGTH_LONG).show();
                            }

                            Intent intent = new Intent(this, HomeActivity.class);
                            intent.putExtra("openFragment", "HomeFragment");
                            startActivity(intent);
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "Failed to load boss info: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }

}