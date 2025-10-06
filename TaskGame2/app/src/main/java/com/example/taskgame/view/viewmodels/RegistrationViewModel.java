package com.example.taskgame.view.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.AllianceRepository;
import com.example.taskgame.data.repositories.SpecialEquipmentRepository;
import com.example.taskgame.data.repositories.UserEquipmentRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Alliance;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.SpecialEquipment;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.domain.models.UserEquipment;

import java.util.ArrayList;
import java.util.List;

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
        //GiveUserEqipment();
        //createAlliance();
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


    public void createAlliance(){
        AllianceRepository allianceRepo = new AllianceRepository();

// Define participants
        List<String> participants = new ArrayList<>();
        participants.add("1759088611"); // leader
        participants.add("1759088864");
        participants.add("1759089126");

// Create alliance object
        Alliance alliance = new Alliance();
        alliance.setLeaderId("1759088611");  // leader
        alliance.setParticipantIds(participants);
        alliance.setSpecialMissionActive(false); // default no mission
        alliance.setSpecialBossId(null);        // no boss yet

// Save to Firestore
        allianceRepo.create(alliance, new AllianceRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                System.out.println("✅ Alliance created with ID: " + id);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to create alliance: " + e.getMessage());
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

// 1. Sword of Flames
        SpecialEquipment swordOfFlames = new SpecialEquipment(
                "eq_sword_flames",
                "Sword of Flames",
                "WEAPON",
                15,
                0.10,
                "A blazing sword that increases your power and rewards."
        );

        repo.create(swordOfFlames, new SpecialEquipmentRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                System.out.println("✅ Sword of Flames created with ID: " + id);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to create Sword of Flames: " + e.getMessage());
            }
        });

// 2. Shadow Cloak
        SpecialEquipment shadowCloak = new SpecialEquipment(
                "eq_shadow_cloak",
                "Shadow Cloak",
                "CLOTHING",
                5,
                0.20,
                "A cloak that grants stealth and more loot."
        );

        repo.create(shadowCloak, new SpecialEquipmentRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                System.out.println("✅ Shadow Cloak created with ID: " + id);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to create Shadow Cloak: " + e.getMessage());
            }
        });

// 3. War Hammer
        SpecialEquipment warHammer = new SpecialEquipment(
                "eq_war_hammer",
                "War Hammer",
                "WEAPON",
                25,
                0.05,
                "A heavy hammer that deals massive blows."
        );

        repo.create(warHammer, new SpecialEquipmentRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                System.out.println("✅ War Hammer created with ID: " + id);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to create War Hammer: " + e.getMessage());
            }
        });

// 4. Golden Crown
        SpecialEquipment goldenCrown = new SpecialEquipment(
                "eq_golden_crown",
                "Golden Crown",
                "CLOTHING",
                0,
                0.50,
                "A crown symbolizing wealth, greatly boosts coin rewards."
        );

        repo.create(goldenCrown, new SpecialEquipmentRepository.CreateCallback() {
            @Override
            public void onSuccess(String id) {
                System.out.println("✅ Golden Crown created with ID: " + id);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("❌ Failed to create Golden Crown: " + e.getMessage());
            }
        });



    }
}
