package com.example.taskgame.view.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.SpecialEquipmentRepository;
import com.example.taskgame.data.repositories.UserEquipmentRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.SpecialEquipment;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.domain.models.UserEquipment;

public class RegistrationViewModel extends ViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedAvatar = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final UserRepository userRepository = new UserRepository();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();

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

    public void login(int userNumber) {
        //CreateTestEquipment();
        GiveUserEqipment();
        String userId = String.valueOf(userNumber);

        if(userNumber == 1){
            userId = "1759088611";
        } else if(userNumber == 2){
            userId = "1759088864";
        } else{
            userId = "1759089126";
        }

        String finalUserId = userId;
        userRepository.getUserById(userId, new UserRepository.GetUserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    // Save in session
                    SessionManager.getInstance().setUserData(user);

                    message.setValue("Logged in as User " + finalUserId);
                    registrationSuccess.setValue(true); // reuse same LiveData to navigate
                } else {
                    message.setValue("User " + finalUserId + " not found");
                }
            }

            @Override
            public void onFailure(Exception e) {
                message.setValue("Login failed: " + e.getMessage());
            }
        });
    }

    public void register() {
        String user = username.getValue() != null ? username.getValue() : "";
        String email = this.email.getValue() != null ? this.email.getValue() : "";
        String password = this.password.getValue() != null ? this.password.getValue() : "";
        String confirmPassword = this.confirmPassword.getValue() != null ? this.confirmPassword.getValue() : "";
        int avatar = this.selectedAvatar.getValue() != null ? this.selectedAvatar.getValue() : 1;

        if(user.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            message.setValue("Please fill all fields");
            return;
        }

        if(!password.equals(confirmPassword)) {
            message.setValue("Passwords do not match");
            return;
        }
        User newUser = new User();
        newUser.setId((Long)(System.currentTimeMillis() / 1000));
        newUser.setUsername(user);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setAvatar(avatar);

        userRepository.registerUser(newUser, new UserRepository.RegisterCallback() {
            @Override
            public void onSuccess() {
                message.setValue("Registered successfully!");
                registrationSuccess.setValue(true);

            }

            @Override
            public void onFailure(Exception e) {
                message.setValue("Registration failed: " + e.getMessage());
            }
        });
    }


    public void GiveUserEqipment(){
        UserEquipmentRepository repo = new UserEquipmentRepository();
        String userId = "1759088611";

// Equipment IDs
        String[] equipmentIds = {
                "GKGjb7iz37pJdbikaLzg",
                "AMqIopIsGHtywqSEUgbt",
                "3itA28EZas2PzZBAiVoc"
        };

// Assign all 3 pieces of equipment to this user
        for (String eqId : equipmentIds) {
            UserEquipment ue = new UserEquipment(userId, eqId, false); // start as inactive
            repo.add(userId, ue, new UserEquipmentRepository.VoidCallback() {
                @Override
                public void onSuccess() {
                    System.out.println("✅ Equipment " + eqId + " assigned to user " + userId);
                }

                @Override
                public void onFailure(Exception e) {
                    System.err.println("❌ Failed to assign equipment " + eqId + ": " + e.getMessage());
                }
            });
        }

    }
    public void CreateTestEquipment(){
        SpecialEquipmentRepository repo = new SpecialEquipmentRepository();

// Sword of Flames
        SpecialEquipment sword = new SpecialEquipment(
                null,                                // Firestore id
                "Sword of Flames",                   // name
                "weapon",                            // type
                20,                                  // bonusPP
                0,                                   // bonusCoinPercent
                "A blazing sword that increases your power by 20 PP." // description
        );

// Golden Armor
        SpecialEquipment armor = new SpecialEquipment(
                null,
                "Golden Armor",
                "armor",
                10,
                10,
                "Heavy golden armor that grants +10 PP and +10% coins from rewards."
        );

// Lucky Amulet
        SpecialEquipment amulet = new SpecialEquipment(
                null,
                "Lucky Amulet",
                "accessory",
                0,
                25,
                "A mysterious charm that increases your coin gain by 25%."
        );

// Save into Firestore
        repo.create(sword, new SpecialEquipmentRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                System.out.println("✅ Sword saved with id: " + id);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to save Sword: " + e.getMessage());
            }
        });

        repo.create(armor, new SpecialEquipmentRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                System.out.println("✅ Armor saved with id: " + id);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to save Armor: " + e.getMessage());
            }
        });

        repo.create(amulet, new SpecialEquipmentRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                System.out.println("✅ Amulet saved with id: " + id);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to save Amulet: " + e.getMessage());
            }
        });

    }
}
