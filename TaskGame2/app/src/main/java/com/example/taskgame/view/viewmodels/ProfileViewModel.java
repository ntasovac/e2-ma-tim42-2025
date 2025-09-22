package com.example.taskgame.view.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData;
    private final MutableLiveData<String> currentPassword = new MutableLiveData<>();
    private final MutableLiveData<String> newPassword = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> changePasswordSuccess = new MutableLiveData<>();

    private ListenerRegistration listenerRegistration;

    public LiveData<String> getCurrentPassword() { return currentPassword; }
    public LiveData<String> getNewPassword() { return newPassword; }
    public LiveData<String> getConfirmPassword() { return confirmPassword; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getchangePasswordSuccess() { return changePasswordSuccess; }

    public void setCurrentPassword(String value) { currentPassword.setValue(value); }
    public void setNewPassword(String value) { newPassword.setValue(value); }
    public void setConfirmPassword(String value) { confirmPassword.setValue(value); }

    public ProfileViewModel() {
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
    public void changePassword(){
        String currentPassword = this.currentPassword.getValue() != null ? this.currentPassword.getValue() : "";
        String newPassword = this.newPassword.getValue() != null ? this.newPassword.getValue() : "";
        String confirmPassword = this.confirmPassword.getValue() != null ? this.confirmPassword.getValue() : "";

        if(!newPassword.equals(confirmPassword)) {
            message.setValue("Passwords do not match");
            return;
        }
        userRepository.changePassword(currentPassword, newPassword, new UserRepository.ChangePasswordCallback() {
            @Override
            public void onSuccess() {
                message.setValue("Password changed!");
                changePasswordSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                message.setValue("Registration failed: " + e.getMessage());
            }
        });
    }
    public void activateEquipment(Context context, int equipmentIndex, Equipment activatedEquipment, OnCompleteListener<Object> listener) {
        userRepository.activateEquipment(equipmentIndex, activatedEquipment, listener, context);
    }
    public void upgradeEquipment(int equipmentIndex, OnCompleteListener<Object> listener){
        userRepository.upgradeEquipment(equipmentIndex, listener);
    }
}