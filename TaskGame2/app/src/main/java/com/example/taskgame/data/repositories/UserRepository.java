package com.example.taskgame.data.repositories;

import androidx.annotation.NonNull;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(String email, String password, User user, RegisterCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if(firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if(emailTask.isSuccessful()) {
                                            db.collection("users").document(firebaseUser.getUid()).set(user)
                                                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                                                    .addOnFailureListener(callback::onFailure);
                                        } else {
                                            callback.onFailure(emailTask.getException());
                                        }
                                    });
                        } else {
                            callback.onFailure(new Exception("FirebaseUser is null"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }


    public interface RegisterCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
