package com.example.taskgame.data.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;
    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        if (auth.getCurrentUser() != null) {
            userLiveData.postValue(auth.getCurrentUser());
        }
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

    public void login(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
                            userLiveData.postValue(auth.getCurrentUser());
                        } else {
                            errorLiveData.postValue("Please verify your email before login.");
                            auth.signOut();
                        }
                    } else {
                        errorLiveData.postValue(
                                task.getException() != null ? task.getException().getMessage() : "Unknown error"
                        );
                    }
                });
    }


    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public interface RegisterCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
