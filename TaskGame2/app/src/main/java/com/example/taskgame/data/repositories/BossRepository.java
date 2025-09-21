package com.example.taskgame.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BossRepository {

    private final FirebaseFirestore db;
    private final MutableLiveData<Boss> bossLiveData;
    public BossRepository(){
        db = FirebaseFirestore.getInstance();
        bossLiveData = new MutableLiveData<>();
    }
    public void createBossIfNotExists() {
        DocumentReference bossRef = db.collection("boss").document("levelBoss");

        bossRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Boss boss = new Boss();
                bossRef.set(boss)
                        .addOnSuccessListener(aVoid ->
                                Log.d("BossRepository", "Boss created"))
                        .addOnFailureListener(e ->
                                Log.e("BossRepository", "Error: ", e));
            } else {
                Log.d("BossRepository", "Boss already created");
            }
        }).addOnFailureListener(e ->
                Log.e("BossRepository", "Error: ", e));
    }

    public MutableLiveData<Boss> getBoss() {
        DocumentReference bossRef = db.collection("boss").document("levelBoss");
        bossRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                bossLiveData.setValue(null);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Boss boss = snapshot.toObject(Boss.class);
                bossLiveData.setValue(boss);
            } else {
                bossLiveData.setValue(null);
            }
        });

        return bossLiveData;
    }
}
