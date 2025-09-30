package com.example.taskgame.data.repositories;

import androidx.annotation.NonNull;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(User user, final RegisterCallback callback) {
        db.collection("users")
                .document(String.valueOf(user.getId()))
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    // 🔹 New function: get user by id
    public void getUserById(String userId, final GetUserCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }


    // 🔹 New callback interface
    public interface GetUserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public void updateUserFields(String userId, int level, int xp, int pp, final RegisterCallback cb) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Level", level);
        updates.put("XP", xp);
        updates.put("PP", pp);

        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cb.onSuccess();
                    } else {
                        cb.onFailure(task.getException());
                    }
                });
    }


    public interface RegisterCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
