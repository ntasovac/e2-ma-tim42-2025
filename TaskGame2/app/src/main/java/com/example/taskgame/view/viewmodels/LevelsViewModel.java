package com.example.taskgame.view.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class LevelsViewModel extends ViewModel {

    private final MutableLiveData<User> userLiveData;
    private final UserRepository userRepository;
    private ListenerRegistration listenerRegistration;

    public LevelsViewModel(){
        userRepository = new UserRepository();
        userLiveData = userRepository.getCurrentUser();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            listenerRegistration = db.collection("users")
                    .document(firebaseUser.getUid())
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            Log.w("ProfileVM", "Listen failed.", e);
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            User user = snapshot.toObject(User.class);
                            if (user != null) {
                                userLiveData.setValue(user);
                            }
                        }
                    });
        }
    }
    public LiveData<User> getUser() {
        return userLiveData;
    }

    public void setUser(User u) {
        userLiveData.setValue(u);
    }

    public int getProgressPercent() {
        User u = userLiveData.getValue();
        if (u == null || u.getLevelThreshold() <= 0) return 0;
        int percent = (int) ((u.getExperience() * 100f) / u.getLevelThreshold());
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return percent;
    }
}
