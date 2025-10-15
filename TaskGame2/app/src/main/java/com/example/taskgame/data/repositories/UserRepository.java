package com.example.taskgame.data.repositories;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.taskgame.R;
import com.example.taskgame.domain.enums.EquipmentType;
import com.example.taskgame.domain.enums.Title;
import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.annotation.NonNull;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;


import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private FirebaseFirestore db;
    private final FirebaseAuth auth;
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

    // 🔹 New function: get user by id
    public void getUserById(String userId, final GetUserCallback callback) {
        db.collection("users")
                .whereEqualTo("id", Integer.parseInt(userId)) // or just userId if stored as string
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        User user = querySnapshot.getDocuments().get(0).toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


    // 🔹 New callback interface
    public interface GetUserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public void updateUserFields(String userId, int level, int xp, int pp, final RegisterCallback cb) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Level", level);
        updates.put("XP", xp);
        updates.put("PP", pp);
        System.out.println("🔄 Updating user " + userId + " -> Level: " + level + ", XP: " + xp + ", PP: " + pp);

        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cb.onSuccess();
                    } else {
                        cb.onFailure(task.getException());
                    }
                });
    }

    /** 🔹 Increment user's coins by a certain amount */
    public void incrementCoins(String userId, int amount, final RegisterCallback cb) {
        if (userId == null || userId.isEmpty()) {
            cb.onFailure(new IllegalArgumentException("User ID is missing"));
            return;
        }

        db.collection("users").document(userId)
                .update("coins", FieldValue.increment(amount))
                .addOnSuccessListener(aVoid -> {
                    System.out.println("✅ User " + userId + " coins increased by " + amount);
                    cb.onSuccess();
                })
                .addOnFailureListener(e -> {
                    System.err.println("❌ Failed to increase coins for user " + userId + ": " + e.getMessage());
                    cb.onFailure(e);
                });
    }


    public void giveBossRewards(String userId, int bonusCoins) {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                System.err.println("❌ User not found or fetch failed");
                return;
            }

            DocumentSnapshot snapshot = task.getResult();
            Long currentCoins = snapshot.getLong("coins"); // Firestore stores numbers as Long
            if (currentCoins == null) currentCoins = 0L;

            long newCoins = currentCoins + bonusCoins;

            Map<String, Object> updates = new HashMap<>();
            updates.put("coins", newCoins);

            db.collection("users").document(userId).update(updates)
                    .addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            System.out.println("✅ Coins updated to: " + newCoins);
                        } else {
                            System.err.println("❌ Failed to update coins: " + updateTask.getException());
                        }
                    });
        });
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
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null && currentUser.isEmailVerified()) {

                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String token = tokenTask.getResult();
                                            FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(currentUser.getUid())
                                                    .update("fcmToken", token)
                                                    .addOnSuccessListener(aVoid ->
                                                            Log.d("FCM", "Token saved after login"))
                                                    .addOnFailureListener(e ->
                                                            Log.e("FCM", "Failed to save token after login", e));
                                        } else {
                                            Log.w("FCM", "Fetching FCM registration token failed",
                                                    tokenTask.getException());
                                        }
                                    });

                            /*
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(currentUser.getUid())
                                    .get()
                                    .addOnSuccessListener(document -> {
                                        if (document.exists()) {
                                            User user = document.toObject(User.class);
                                            if (user != null) {
                                                SessionManager.getInstance().setUserData(user);
                                                Log.d("Session", "✅ User data stored in SessionManager: " + user.getId());
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Log.e("Session", "❌ Failed to load user data", e));
*/
                            userLiveData.postValue(currentUser);
                            this.getCurrentUser();
                        } else {
                            errorLiveData.postValue("Please verify your email before login.");
                            auth.signOut();
                        }
                    } else {
                        errorLiveData.postValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unknown error"
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
                            SessionManager.getInstance().setUserData(user);
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

    public MutableLiveData<User> getUserByEmail(String email) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        User user = querySnapshot.getDocuments()
                                .get(0)
                                .toObject(User.class);
                        liveData.setValue(user);
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));

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

            Long coinsVal = snapshot.getLong("coins");
            long coins = coinsVal != null ? coinsVal : 0L;

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
        DocumentReference bossRef = db.collection("boss").document("levelBoss");

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

            Long powerPointsVal = snapshot.getLong("powerPoints");
            long powerPoints = powerPointsVal != null ? powerPointsVal : 0L;
            Long successfulAttackChanceVal = snapshot.getLong("successfulAttackChance");
            long successfulAttackChance = successfulAttackChanceVal != null ? successfulAttackChanceVal : 0L;
            Long attackCountVal = snapshot.getLong("attackCount");
            long attackCount = attackCountVal != null ? attackCountVal : 0L;
            String name = activatedEquipment.getName();
            Number effect = (Number) eq.get("effectAmount");
            double effectAmount = effect != null ? effect.doubleValue() : 0;
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
                case "Sword":
                    powerPoints = (long)(powerPoints + powerPoints*(effectAmount/100.0));
                    transaction.update(userRef, "powerPoints", powerPoints);
                    break;
                case "Bow and Arrow":
                    transaction.update(bossRef, "bonus", 5);
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

    public void earnXP(int xP, OnCompleteListener<Object> listener){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            User user = transaction.get(userRef).toObject(User.class);
            if (!snapshot.exists()) {
                Log.e("activateEquipment", "User document does not exist!");
                throw new FirebaseFirestoreException(
                        "User not found",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            long experience = user.getExperience();
            long levelThreshold = user.getLevelThreshold();
            long level = user.getLevel();
            Title title = user.getTitle();
            Title currentTitle = user.getTitle();
            Title[] titles = Title.values();
            int nextIndex = currentTitle.ordinal() + 1;
            long powerPoints = user.getPowerPoints();

            transaction.update(userRef, "experience", experience + xP);
            if(experience + xP >= levelThreshold){
                transaction.update(userRef, "level", level+1);
                transaction.update(userRef, "levelThreshold", (long)Math.round(levelThreshold*5/2.0));
                if(title != Title.CHALLENGER) {
                    Title nextTitle = titles[nextIndex];
                    transaction.update(userRef, "title", nextTitle.name());
                }
                if(level == 0){
                    transaction.update(userRef, "powerPoints", 40);
                }else{
                    transaction.update(userRef, "powerPoints", powerPoints+(long)Math.round(40*Math.pow(7/4.0, level)));
                }
            }
            return null;
        }).addOnCompleteListener(listener);
    }

    public void upgradeEquipment(int equipmentIndex, OnCompleteListener<Object> listener){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(user.getUid());
        DocumentReference bossRef = db.collection("boss").document("levelBoss");

        db.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef);
            DocumentSnapshot bossSnapshot = transaction.get(bossRef);
            if (!userSnapshot.exists()) {
                Log.e("upgradeEquipment", "User document does not exist!");
                throw new FirebaseFirestoreException(
                        "User not found",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            List<Map<String, Object>> equipmentList =
                    (List<Map<String, Object>>) userSnapshot.get("equipment");

            if (equipmentList == null || equipmentList.isEmpty() || equipmentIndex >= equipmentList.size()) {
                Log.e("upgradeEquipment", "Invalid equipment index!");
                throw new FirebaseFirestoreException(
                        "Invalid equipment index",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }
            Map<String, Object> eq = equipmentList.get(equipmentIndex);
            Number effect = (Number) eq.get("effectAmount");
            double effectAmount = effect != null ? effect.doubleValue() : 0;
            String name = (String) eq.get("name");
            Long powerPointsVal = userSnapshot.getLong("powerPoints");
            long powerPoints = powerPointsVal != null ? powerPointsVal : 0L;
            double oldPowerPoints = powerPoints * (100/(100.0 + effectAmount));
            boolean isActivated = Boolean.TRUE.equals(eq.get("isActivated"));
            Long coinsVal = userSnapshot.getLong("coins");
            long coins = coinsVal != null ? coinsVal : 0L;
            Long bossLevelVal = bossSnapshot.getLong("level");
            long bossLevel = bossLevelVal != null ? bossLevelVal : 0L;
            long lastBossReward = (long) Math.ceil(Math.pow(6.0 / 5.0, bossLevel - 2) * 200);
            long price = (long) (lastBossReward*0.6);
            if (coins < lastBossReward) {
                throw new FirebaseFirestoreException(
                        "Not enough coins",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            double newEffect = effectAmount + 1;
            newEffect = Math.round(newEffect * 100.0) / 100.0;
            eq.put("effectAmount", newEffect);
            transaction.update(userRef, "equipment", equipmentList);
            transaction.update(userRef, "coins", coins - price);
            if(isActivated){
                if(name.equals("Sword")){
                    powerPoints = Math.round(oldPowerPoints + oldPowerPoints * (newEffect/100.0));
                    transaction.update(userRef, "powerPoints", powerPoints);

                }else{
                    transaction.update(bossRef, "bonus", newEffect);
                }
            }

            return  null;
        }).addOnCompleteListener(listener);
    }

    public void claimPrize(OnCompleteListener<Equipment> listener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            listener.onComplete(Tasks.forException(new Exception("No authenticated user")));
            return;
        }

        DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());
        DocumentReference bossRef = db.collection("boss").document("levelBoss");

        Tasks.whenAllSuccess(userRef.get(), bossRef.get())
                .addOnSuccessListener(results -> {
                    DocumentSnapshot userSnap = (DocumentSnapshot) results.get(0);
                    DocumentSnapshot bossSnap = (DocumentSnapshot) results.get(1);

                    User user = userSnap.toObject(User.class);
                    Boss boss = bossSnap.toObject(Boss.class);

                    if (user == null || boss == null) {
                        listener.onComplete(Tasks.forException(new Exception("User or Boss not found")));
                        return;
                    }

                    List<Equipment> equipment = user.getEquipment();
                    Equipment rewardEquipment;

                    long lastBossReward = (long) Math.ceil(
                            Math.pow(6.0 / 5.0, /*boss.getCoinReward()*/100 - 2) * 200);

                    Random random = new Random();
                    int number = random.nextInt(100) + 1;
                    if (number <= 80) {
                        int equipmentNumber = random.nextInt(100) + 1;
                        if (equipmentNumber <= 80) {
                            int clothesNumber = random.nextInt(120) + 1;
                            if (clothesNumber <= 100) {
                                rewardEquipment = new Equipment(
                                        EquipmentType.CLOTHING, "Gloves",
                                        (int) Math.ceil(lastBossReward * 0.6),
                                        "Gives power point increase by 10% for two boss fights.",
                                        10, 2, R.drawable.gloves, false, false);
                            } else if (clothesNumber <= 100) {
                                rewardEquipment = new Equipment(
                                        EquipmentType.CLOTHING, "Shield",
                                        (int) Math.ceil(lastBossReward * 0.6),
                                        "Increases odds of landing a successful attack by 10%.",
                                        10, 2, R.drawable.shield, false, false);
                            } else {
                                rewardEquipment = new Equipment(
                                        EquipmentType.CLOTHING, "Boots",
                                        (int) Math.ceil(lastBossReward * 0.8),
                                        "Increases odds of getting an additional attack by 40%.",
                                        40, 2, R.drawable.boots, false, false);
                            }
                        } else {
                            int weaponsNumber = random.nextInt(100) + 1;
                            if (weaponsNumber <= 50) {
                                rewardEquipment = new Equipment(
                                        EquipmentType.WEAPON, "Sword", 0,
                                        "Permanently increases power by 5%",
                                        5, -1, R.drawable.sword, false, true);
                            } else {
                                rewardEquipment = new Equipment(
                                        EquipmentType.WEAPON, "Bow and Arrow", 0,
                                        "Permanently increases boss coin reward bonus by 5%",
                                        5, -1, R.drawable.bow_and_arrow, false, true);
                            }
                        }
                    } else {
                        listener.onComplete(Tasks.forResult(null));
                        return;
                    }

                    for (Equipment eq : equipment) {
                        if (eq.getName().equals(rewardEquipment.getName())) {
                            if (eq.getType() == EquipmentType.CLOTHING) {
                                rewardEquipment.setEffectAmount(rewardEquipment.getEffectAmount() + 10);
                                rewardEquipment.setUsageCount(2);
                                rewardEquipment.setActivated(eq.isActivated());
                            } else {
                                double newEffect = rewardEquipment.getEffectAmount() + 0.02;
                                newEffect = Math.round(newEffect * 100.0) / 100.0;
                                rewardEquipment.setEffectAmount(newEffect);
                                rewardEquipment.setActivated(eq.isActivated());
                            }
                        }
                    }

                    addEquipment(rewardEquipment);
                    updateBossAndUser(task -> {
                        if (task.isSuccessful()) {
                        } else {
                            Exception e = task.getException();
                            Log.e("UpdateBossAndUser", "Transaction failed", e);
                        }
                    });

                    listener.onComplete(Tasks.forResult(rewardEquipment));
                })
                .addOnFailureListener(e ->
                        listener.onComplete(Tasks.forException(e))
                );
    }

    private void updateBossAndUser(OnCompleteListener<Object> listener){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            listener.onComplete(Tasks.forException(new Exception("No authenticated user")));
            return;
        }
        DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());
        DocumentReference bossRef = db.collection("boss").document("levelBoss");

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            DocumentSnapshot bossSnapshot = transaction.get(bossRef);
            Long coinsVal =  snapshot.getLong("coins");
            long coins = coinsVal != null ? coinsVal : 0L;
            Long rewardVal =  bossSnapshot.getLong("coinReward");
            long reward = rewardVal != null ? rewardVal : 0L;
            Double bonusVal = bossSnapshot.getDouble("bonus");
            double bonus = bonusVal != null ? bonusVal : 0.0;
            Long levelVal =  bossSnapshot.getLong("level");
            long level = levelVal != null ? levelVal : 0L;
            Long xpThresholdVal =  bossSnapshot.getLong("xpthreshold");
            long xpThreshold = xpThresholdVal != null ? xpThresholdVal : 0L;
            transaction.update(userRef, "coins", coins + reward + (long)(reward*(bonus/100.0)));
            transaction.update(bossRef, "level", level+1);
            transaction.update(bossRef, "xpthreshold", (long)(xpThreshold*5/2.0));
            transaction.update(bossRef, "coinReward", (long)(reward*6/5.0));
            return null;
        });
    }


    private void addEquipment(Equipment rewardEquipment){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(user.getUid());
        DocumentReference bossRef = db.collection("boss").document("levelBoss");

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(userRef);
                    DocumentSnapshot bossSnapshot = transaction.get(bossRef);
                    List<Map<String, Object>> equipmentList =
                            (List<Map<String, Object>>) snapshot.get("equipment");

                    if (equipmentList == null) return null;
                    boolean found = false;
                    Long powerPointsVal = snapshot.getLong("powerPoints");
                    long powerPoints = powerPointsVal != null ? powerPointsVal : 0L;
                    double oldPowerPoints = powerPoints * (100/(100.0 + rewardEquipment.getEffectAmount()-10));
                    Long successVal = snapshot.getLong("successfulAttackChance");
                    long successfulAttackChance = successVal != null ? successVal : 0L;
                    double oldSuccessfulAttackChance = successfulAttackChance* (100/(100.0 + rewardEquipment.getEffectAmount()-10));
                    Long attackCountVal = snapshot.getLong("attackCount");
                    long attackCount = attackCountVal != null ? attackCountVal : 0L;

                    for (int i = 0; i < equipmentList.size(); i++) {
                        Map<String, Object> eq = equipmentList.get(i);
                        String name = (String) eq.get("name");

                        if (name.equals(rewardEquipment.getName())) {
                            equipmentList.set(i, createEquipmentMap(rewardEquipment));
                            found = true;
                            if(rewardEquipment.isActivated()) {
                                if (name.equals("Gloves")) {
                                    powerPoints =  Math.round(oldPowerPoints + oldPowerPoints * (rewardEquipment.getEffectAmount() / 100.0));
                                    transaction.update(userRef, "powerPoints", powerPoints);
                                }else if(name.equals("Shield")){
                                    successfulAttackChance = Math.round(oldSuccessfulAttackChance + oldSuccessfulAttackChance * (rewardEquipment.getEffectAmount() / 100.0));
                                    transaction.update(userRef, "successfulAttackChance", successfulAttackChance);
                                }else if(name.equals("Boots")){
                                    Random random = new Random();
                                    int number = random.nextInt(100) + 1;
                                    if(number <= rewardEquipment.getEffectAmount()) {
                                        transaction.update(userRef, "attackCount", attackCount + 1);
                                    }
                                }else if(name.equals("Sword")){
                                    powerPoints =  Math.round(oldPowerPoints + oldPowerPoints * (rewardEquipment.getEffectAmount() / 100.0));
                                    transaction.update(userRef, "powerPoints", powerPoints);
                                }else if(name.equals("Bow and Arrow")){
                                    transaction.update(bossRef, "bonus", rewardEquipment.getEffectAmount());
                                }
                            }
                            break;
                        }
                    }

                    if(!found){
                        equipmentList.add(createEquipmentMap(rewardEquipment));
                    }
                    transaction.update(userRef, "equipment", equipmentList);
                    return null;
                }).addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Equipment upgraded successfully"))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Failed to upgrade equipment", e));


    }

    private Map<String, Object> createEquipmentMap(Equipment eq) {
        Map<String, Object> map = new HashMap<>();
        map.put("description", eq.getDescription());
        map.put("effectAmount", eq.getEffectAmount());
        map.put("image", eq.getImage());
        map.put("isActivated", eq.isActivated());
        map.put("isEffectPermanent", eq.isEffectPermanent());
        map.put("name", eq.getName());
        map.put("price", eq.getPrice());
        map.put("type", eq.getType().name());
        map.put("usageCount", eq.getUsageCount());
        return map;
    }

    public void decreaseUsageCountAndRemove(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(userRef);
                    List<Map<String, Object>> equipmentList =
                            (List<Map<String, Object>>) snapshot.get("equipment");

                    if (equipmentList == null) return null;

                    Iterator<Map<String, Object>> iterator = equipmentList.iterator();
                    while (iterator.hasNext()) {
                        Map<String, Object> eq = iterator.next();

                        String name = (String) eq.get("name");
                        EquipmentType type = EquipmentType.valueOf((String) eq.get("type"));
                        boolean isActivated = Boolean.TRUE.equals(eq.get("isActivated"));
                        boolean isPermanent = Boolean.TRUE.equals(eq.get("isEffectPermanent"));

                        Number usageNum = (Number) eq.get("usageCount");
                        long usageCount = usageNum != null ? usageNum.longValue() : 0L;

                        Number effectNum = (Number) eq.get("effectAmount");
                        double effectAmount = effectNum != null ? effectNum.doubleValue() : 0.0;

                        Long powerPointsVal = snapshot.getLong("powerPoints");
                        long powerPoints = powerPointsVal != null ? powerPointsVal : 0L;

                        Long successVal = snapshot.getLong("successfulAttackChance");
                        long successfulAttackChance = successVal != null ? successVal : 0L;

                        if(type == EquipmentType.POTION){
                            if (isActivated && !isPermanent) {
                                transaction.update(userRef, "powerPoints", (long)(powerPoints * (100/(100.0 + effectAmount))));
                                iterator.remove();
                            }else if(isActivated && isPermanent){
                                iterator.remove();
                            }
                        }else if(name.equals("Gloves")){
                            if(isActivated){
                                if(usageCount - 1 == 0){
                                    transaction.update(userRef, "powerPoints", (long)(powerPoints * (100/(100.0 + effectAmount))));
                                    iterator.remove();
                                }else{
                                    eq.put("usageCount", usageCount-1);
                                }
                            }
                        }else if(name.equals("Shield")){
                            if(isActivated){
                                if(usageCount - 1 == 0){
                                    transaction.update(userRef, "successfulAttackChance", (long)(successfulAttackChance * (100/(100.0 + effectAmount))));
                                    iterator.remove();
                                }else{
                                    eq.put("usageCount", usageCount-1);
                                }
                            }
                        }else if(name.equals("Boots")){
                            if(isActivated){
                                if(usageCount - 1 == 0){
                                    transaction.update(userRef, "attackCount", 5);
                                    iterator.remove();
                                }else{
                                    eq.put("usageCount", usageCount-1);
                                }
                            }
                        }
                    }

                    transaction.update(userRef, "equipment", equipmentList);
                    return null;
                }).addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Equipment upgraded successfully"))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Failed to upgrade equipment", e));


    }
    public void searchUsersByUsername(String query, UserSearchCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) users.add(u);
                    }
                    callback.onResult(users);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void sendFriendRequest(User sender, User recipient, OnCompleteListener<Object> listener) {

        String recipientEmail = recipient.getEmail();

        db.collection("users")
                .whereEqualTo("email", recipientEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {

                        DocumentReference userRef =
                                querySnapshot.getDocuments().get(0).getReference();

                        db.runTransaction(transaction -> {
                            DocumentSnapshot snapshot = transaction.get(userRef);
                            if (!snapshot.exists()) {
                                throw new FirebaseFirestoreException(
                                        "Recipient not found",
                                        FirebaseFirestoreException.Code.ABORTED
                                );
                            }

                            List<User> friendRequestsList =
                                    (List<User>) snapshot.get("friendRequests");

                            if (friendRequestsList == null) {
                                friendRequestsList = new ArrayList<>();
                            }

                            friendRequestsList.add(sender);

                            transaction.update(userRef, "friendRequests", friendRequestsList);
                            return null;
                        }).addOnCompleteListener(listener);

                    } else {
                        if (listener != null) {
                            listener.onComplete(
                                    Tasks.forException(
                                            new Exception("Recipient with email "
                                                    + recipientEmail + " not found")));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onComplete(Tasks.forException(e));
                    }
                });
    }


    public void acceptFriendRequest(User sender, OnCompleteListener<Object> listener){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);

            List<User> friendsList = (List<User>) snapshot.get("friends");

            if (friendsList == null) {
                friendsList = new ArrayList<>();
            }
            friendsList.add(sender);

            transaction.update(userRef, "friends", friendsList);

            List<Map<String,Object>> requests =
                    (List<Map<String,Object>>) snapshot.get("friendRequests");
            if (requests == null) {
                requests = new ArrayList<>();
            }
            Iterator<Map<String,Object>> iterator = requests.iterator();
            while (iterator.hasNext()) {
                Map<String,Object> req = iterator.next();
                String email = (String) req.get("email");
                if (sender.getEmail().equals(email)) {
                    iterator.remove();
                    break;
                }
            }

            transaction.update(userRef, "friendRequests", requests);

            return null;
        }).addOnCompleteListener(listener);
    }

    public void declineFriendRequest(User sender, OnCompleteListener<Object> listener){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);

            List<Map<String, Object>> requests =
                    (List<Map<String, Object>>) snapshot.get("friendRequests");
            if (requests == null) {
                requests = new ArrayList<>();
            }
            Iterator<Map<String, Object>> iterator = requests.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> req = iterator.next();
                Map<String, Object> senderMap = (Map<String, Object>) req.get("sender");
                if (senderMap != null &&
                        sender.getEmail().equals(senderMap.get("email"))) {
                    iterator.remove();
                    break;
                }
            }

            transaction.update(userRef, "friendRequests", requests);

            return null;
        }).addOnCompleteListener(listener);
    }

    public void addFriend(User sender, User recipient, OnCompleteListener<Object> listener){
        String senderEmail = sender.getEmail();
        db.collection("users")
                .whereEqualTo("email", senderEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {

                        DocumentReference userRef =
                                querySnapshot.getDocuments().get(0).getReference();

                        db.runTransaction(transaction -> {
                            DocumentSnapshot snapshot = transaction.get(userRef);
                            if (!snapshot.exists()) {
                                throw new FirebaseFirestoreException(
                                        "Sender not found",
                                        FirebaseFirestoreException.Code.ABORTED
                                );
                            }

                            List<User> friendList =
                                    (List<User>) snapshot.get("friends");

                            if (friendList == null) {
                                friendList = new ArrayList<>();
                            }

                            friendList.add(recipient);

                            transaction.update(userRef, "friends", friendList);
                            return null;
                        }).addOnCompleteListener(listener);

                    } else {
                        if (listener != null) {
                            listener.onComplete(
                                    Tasks.forException(
                                            new Exception("Recipient with email "
                                                    + senderEmail + " not found")));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onComplete(Tasks.forException(e));
                    }
                });
    }

    public void inviteFriend(int index, User friend, @NonNull OnCompleteListener<Object> listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());
        if (currentUser == null) {
            listener.onComplete(Tasks.forException(new Exception("Not logged in")));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot ->{
                    User user = documentSnapshot.toObject(User.class);
                    String inviteId = UUID.randomUUID().toString();
                    Map<String, Object> invite = new HashMap<>();
                    invite.put("allianceName", user.getAlliance());
                    invite.put("senderEmail", user.getEmail());
                    invite.put("senderName", user.getUsername());
                    invite.put("receiverEmail", friend.getEmail());
                    invite.put("receiverName", friend.getUsername());
                    invite.put("status", "pending");

                    db.collection("invites").document(inviteId)
                            .set(invite)
                            .addOnSuccessListener(aVoid -> {
                                sendPushNotification(inviteId, friend.getEmail(), friend.getUsername(), user.getUsername(), user.getAlliance(), user.getEmail());
                                listener.onComplete(Tasks.forResult(null));
                            })
                            .addOnFailureListener(e -> listener.onComplete(Tasks.forException(e)));
                }).addOnFailureListener(e -> listener.onComplete(Tasks.forException(e)));
    }

    public void sendPushNotification(String invitedId, String receiverEmail,
                                      String receiverName,
                                      String senderName,
                                      String allianceName, String senderEmail) {

        OkHttpClient client = new OkHttpClient();

        String json = "{"
                + "\"inviteId\":\"" + invitedId + "\","
                + "\"receiverEmail\":\"" + receiverEmail + "\","
                + "\"receiverName\":\"" + receiverName + "\","
                + "\"senderName\":\"" + senderName + "\","
                + "\"allianceName\":\"" + allianceName + "\","
                + "\"senderEmail\":\"" + senderEmail + "\""
                + "}";

        RequestBody body = RequestBody.create(
                json,
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://subdepressed-unmagnanimously-tori.ngrok-free.dev/sendInvite")
                .post(body)
                .build();

        Log.d("Invite", "Sending POST to server");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Invite", "Failed to call API", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("Invite", "Notification sent successfully");
                } else {
                    Log.e("Invite", "Server error: " + response.code());
                }
            }
        });
    }

    public void sendAcceptedNotification(Context context,
                                          String senderEmail,
                                          String allianceName,
                                          String accepterName) {
        OkHttpClient client = new OkHttpClient();

        String json = "{"
                + "\"receiverEmail\":\"" + senderEmail + "\","
                + "\"accepterName\":\"" + accepterName + "\","
                + "\"allianceName\":\"" + allianceName + "\""
                + "}";


        RequestBody body = RequestBody.create(
                json.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://subdepressed-unmagnanimously-tori.ngrok-free.dev/sendAccept")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e("InviteAction", "Failed to notify sender", e);
            }
            @Override public void onResponse(Call call, Response response) {
                Log.d("InviteAction", "Sender notified: " + response.code());
            }
        });
    }

    public void sendAllianceMessageNotification(
            String allianceName,
            String senderEmail,
            String senderName,
            String messageText) {

        OkHttpClient client = new OkHttpClient();

        String json = "{"
                + "\"allianceName\":\"" + allianceName + "\","
                + "\"senderEmail\":\"" + senderEmail + "\","
                + "\"senderName\":\"" + senderName + "\","
                + "\"messageText\":\"" + messageText + "\""
                + "}";

        RequestBody body = RequestBody.create(
                json,
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://subdepressed-unmagnanimously-tori.ngrok-free.dev/sendAllianceMessage")
                .post(body)
                .build();

        Log.d("AllianceMsg", "Sending alliance message notification…");

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e("AllianceMsg", "Failed to call API", e);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("AllianceMsg", "Notification sent successfully");
                } else {
                    Log.e("AllianceMsg", "Server error: " + response.code());
                }
            }
        });
    }


    public void acceptInvite(String email, String allianceName){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String userId = userDoc.getId();

                        db.collection("users")
                                .document(userId)
                                .update("alliance", allianceName)
                                .addOnSuccessListener(aVoid ->
                                        Log.d("AllianceRepo", "Alliance updated for " + email))
                                .addOnFailureListener(e ->
                                        Log.e("AllianceRepo", "Failed to update alliance", e));
                    } else {
                        Log.w("AllianceRepo", "No user found with email: " + email);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("AllianceRepo", "Error fetching user", e));
    }


    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
    public interface UserSearchCallback {
        void onResult(List<User> users);
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

