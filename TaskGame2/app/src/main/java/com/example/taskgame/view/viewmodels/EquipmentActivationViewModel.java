package com.example.taskgame.view.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class EquipmentActivationViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData;
    private ListenerRegistration listenerRegistration;

    public EquipmentActivationViewModel(){
        userRepository = new UserRepository();
        userLiveData = userRepository.getCurrentUser();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            listenerRegistration = db.collection("users")
                    .document(firebaseUser.getUid())
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
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
    public void activateEquipment(Context context, int equipmentIndex, Equipment activatedEquipment, OnCompleteListener<Object> listener) {
        userRepository.activateEquipment(equipmentIndex, activatedEquipment, listener, context);
    }
}
