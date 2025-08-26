package com.example.taskgame.view.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.User;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final LiveData<User> userLiveData;
    private final MutableLiveData<String> currentPassword = new MutableLiveData<>();
    private final MutableLiveData<String> newPassword = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> changePasswordSuccess = new MutableLiveData<>();

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
}