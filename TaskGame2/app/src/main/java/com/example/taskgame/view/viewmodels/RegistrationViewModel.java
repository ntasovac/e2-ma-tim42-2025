package com.example.taskgame.view.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrationViewModel extends ViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedAvatar = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final UserRepository userRepository = new UserRepository();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<String> getUsername() { return username; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getPassword() { return password; }
    public LiveData<String> getConfirmPassword() { return confirmPassword; }
    public LiveData<Integer> getSelectedAvatar() { return selectedAvatar; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getRegistrationSuccess() { return registrationSuccess; }

    public void setUsername(String value) { username.setValue(value); }
    public void setEmail(String value) { email.setValue(value); }
    public void setPassword(String value) { password.setValue(value); }
    public void setConfirmPassword(String value) { confirmPassword.setValue(value); }
    public void setSelectedAvatar(int value) { selectedAvatar.setValue(value); }

    public void register() {
        String username = this.username.getValue() != null ? this.username.getValue() : "";
        String email = this.email.getValue() != null ? this.email.getValue() : "";
        String password = this.password.getValue() != null ? this.password.getValue() : "";
        String confirmPassword = this.confirmPassword.getValue() != null ? this.confirmPassword.getValue() : "";
        int avatar = this.selectedAvatar.getValue() != null ? this.selectedAvatar.getValue() : 1;

        if(username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            message.setValue("Please fill all fields");
            return;
        }

        if(!password.equals(confirmPassword)) {
            message.setValue("Passwords do not match");
            return;
        }

        User newUser = new User(username, email, password, avatar);

        userRepository.registerUser(email, password, newUser, new UserRepository.RegisterCallback() {
            @Override
            public void onSuccess() {
                message.setValue("Registered successfully! Please check your email to activate your account.");
                registrationSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                message.setValue("Registration failed: " + e.getMessage());
            }
        });


    }
}
