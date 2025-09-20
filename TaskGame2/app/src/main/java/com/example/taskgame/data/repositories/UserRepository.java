package com.example.taskgame.data.repositories;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.activities.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public MutableLiveData<User> getCurrentUser() {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            liveData.setValue(user);
                        } else {
                            liveData.setValue(null);
                        }
                    })
                    .addOnFailureListener(e -> liveData.setValue(null));
        } else {
            liveData.setValue(null);
        }

        return liveData;
    }

    public void changePassword(String currentPassword, String newPassword, ChangePasswordCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure(new Exception("No authenticated user"));
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(updateTask.getException());
                    }
                });
            } else {
                callback.onFailure(new Exception("Current password is incorrect"));
            }
        });
    }
    public void buyEquipment(Equipment equipment, OnCompleteListener<Object> listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            if (!snapshot.exists()) {
                Log.e("buyEquipment", "User document does not exist!");
                throw new FirebaseFirestoreException(
                        "User not found",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            long coins = snapshot.getLong("coins");

            List<Map<String, Object>> equipmentList =
                    (List<Map<String, Object>>) snapshot.get("equipment");

            if (coins < equipment.getPrice()) {
                throw new FirebaseFirestoreException(
                        "Not enough coins",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            transaction.update(userRef, "coins", coins - equipment.getPrice());

            Map<String, Object> newEquipment = new HashMap<>();
            newEquipment.put("name", equipment.getName());
            newEquipment.put("type", equipment.getType().toString());
            newEquipment.put("price", equipment.getPrice());
            newEquipment.put("description", equipment.getDescription());
            newEquipment.put("image", equipment.getImage());
            newEquipment.put("effectAmount", equipment.getEffectAmount());
            newEquipment.put("usageCount", equipment.getUsageCount());
            newEquipment.put("isActivated", equipment.isActivated());
            newEquipment.put("isEffectPermanent", equipment.isEffectPermanent());

            if (equipmentList == null) {
                equipmentList = new ArrayList<>();
            }
            equipmentList.add(newEquipment);

            transaction.update(userRef, "equipment", equipmentList);

            return null;
        }).addOnCompleteListener(listener);
    }

    public void activateEquipment(int equipmentIndex, Equipment activatedEquipment, OnCompleteListener<Object> listener, Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            if (!snapshot.exists()) {
                Log.e("activateEquipment", "User document does not exist!");
                throw new FirebaseFirestoreException(
                        "User not found",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            List<Map<String, Object>> equipmentList =
                    (List<Map<String, Object>>) snapshot.get("equipment");

            if (equipmentList == null || equipmentList.isEmpty() || equipmentIndex >= equipmentList.size()) {
                Log.e("activateEquipment", "Invalid equipment index!");
                throw new FirebaseFirestoreException(
                        "Invalid equipment index",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }
            Map<String, Object> eq = equipmentList.get(equipmentIndex);
            eq.put("isActivated", true);

            transaction.update(userRef, "equipment", equipmentList);

            long powerPoints = snapshot.getLong("powerPoints");
            long successfulAttackChance = snapshot.getLong("successfulAttackChance");
            long attackCount = snapshot.getLong("attackCount");
            String name = activatedEquipment.getName();
            Number effect = (Number) eq.get("effectAmount");
            long effectAmount = effect != null ? effect.longValue() : 0;
            boolean gainedAttack = false;
            switch (name){
                case "One-time potion I":
                    powerPoints = (long)(powerPoints + powerPoints*(effectAmount/100.0));
                    transaction.update(userRef, "powerPoints", powerPoints);
                    break;
                case "One-time potion II":
                    powerPoints = (long)(powerPoints + powerPoints*(effectAmount/100.0));
                    transaction.update(userRef, "powerPoints", powerPoints);
                    break;
                case "Permanent potion I":
                    powerPoints = (long)(powerPoints + powerPoints*(effectAmount/100.0));
                    transaction.update(userRef, "powerPoints", powerPoints);
                    break;
                case "Permanent potion II":
                    powerPoints = (long)(powerPoints + powerPoints*(effectAmount/100.0));
                    transaction.update(userRef, "powerPoints", powerPoints);
                    break;
                case "Gloves":
                    powerPoints = (long)(powerPoints + powerPoints*(effectAmount/100.0));
                    transaction.update(userRef, "powerPoints", powerPoints);
                    break;
                case "Shield":
                    successfulAttackChance = (long)(successfulAttackChance + successfulAttackChance*(effectAmount/100.0));
                    transaction.update(userRef, "successfulAttackChance", successfulAttackChance);
                    break;
                case "Boots":
                    Random random = new Random();
                    int number = random.nextInt(100) + 1;
                    if(number <= effectAmount) {
                        transaction.update(userRef, "attackCount", attackCount + 1);
                        gainedAttack = true;
                    }else{
                        gainedAttack= false;
                    }
                    break;
                default:
                    throw new IllegalStateException("Invalid equipment: " + activatedEquipment.getName());
            }
            return gainedAttack;
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if ("Boots".equals(activatedEquipment.getName())) {
                    Boolean gainedAttack = (Boolean) task.getResult();
                    if (Boolean.TRUE.equals(gainedAttack)) {
                        Toast.makeText(context, "You gained an additional attack", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "You didn't gain an additional attack", Toast.LENGTH_SHORT).show();
                    }
                }
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
    public interface ChangePasswordCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
