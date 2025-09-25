package com.example.taskgame.view.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class FriendsProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData;
    private ListenerRegistration listenerRegistration;
    public FriendsProfileViewModel(SavedStateHandle savedStateHandle) {
        userRepository = new UserRepository();
        String email = savedStateHandle.get("friendEmail");
        userLiveData = userRepository.getUserByEmail(email);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            listenerRegistration = db.collection("users")
                    .document(firebaseUser.getUid())
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            Log.w("FriendsProfileVM", "Listen failed.", e);
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
}
