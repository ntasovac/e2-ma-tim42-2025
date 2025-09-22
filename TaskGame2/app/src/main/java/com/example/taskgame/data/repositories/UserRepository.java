package com.example.taskgame.data.repositories;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.taskgame.R;
import com.example.taskgame.domain.enums.EquipmentType;
import com.example.taskgame.domain.enums.Title;
import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.Equipment;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UserRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final BossRepository bossRepository;
    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        if (auth.getCurrentUser() != null) {
            userLiveData.postValue(auth.getCurrentUser());
        }
        bossRepository = new BossRepository();
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
                            Math.pow(6.0 / 5.0, boss.getCoinReward() - 2) * 200);

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
                    double oldPowerPoints = powerPoints * (100/(100.0 + rewardEquipment.getEffectAmount()-10);
                    Long successVal = snapshot.getLong("successfulAttackChance");
                    long successfulAttackChance = successVal != null ? successVal : 0L;
                    double oldSuccessfulAttackChance = successfulAttackChance* (100/(100.0 + rewardEquipment.getEffectAmount()-10);
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
