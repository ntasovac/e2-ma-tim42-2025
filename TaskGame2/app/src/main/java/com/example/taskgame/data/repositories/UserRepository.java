package com.example.taskgame.data.repositories;

import androidx.annotation.NonNull;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

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


    public interface RegisterCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
