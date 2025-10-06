package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.SpecialEquipment;
import com.example.taskgame.domain.models.UserEquipment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class UserEquipmentRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Subcollection: users/{userId}/equipment
    private CollectionReference col(String userId) {
        return db.collection("users").document(userId).collection("equipment");
    }

    /* ---------- Add Equipment to User ---------- */
    public void add(String userId, UserEquipment userEq, final VoidCallback cb) {
        if (userEq.getEquipmentId() == null || userEq.getEquipmentId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("Equipment id missing"));
            return;
        }

        col(userId).document(userEq.getEquipmentId())
                .set(userEq)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    public void setActive(String userId, String equipmentId, boolean active) {
        col(userId).document(equipmentId)
                .update("active", active)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("✅ Equipment " + equipmentId + " active=" + active);
                    } else {
                        System.err.println("❌ Failed to update equipment: " + task.getException());
                    }
                });
    }

    public void toggleActive(String userId, String equipmentId, ToggleCallback cb) {
        col(userId).document(equipmentId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                if (cb != null) cb.onFailure(task.getException());
                return;
            }

            Boolean currentActive = task.getResult().getBoolean("active");
            boolean newActive = (currentActive == null) ? true : !currentActive;

            col(userId).document(equipmentId)
                    .update("active", newActive)
                    .addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            System.out.println("✅ Equipment " + equipmentId + " toggled to active=" + newActive);
                            if (cb != null) cb.onSuccess(newActive);
                        } else {
                            if (cb != null) cb.onFailure(updateTask.getException());
                        }
                    });
        });
    }

    // Callback interface
    public interface ToggleCallback {
        void onSuccess(boolean newStatus);
        void onFailure(Exception e);
    }


    /* ---------- Remove Equipment from User ---------- */
    public void remove(String userId, String equipmentId, final VoidCallback cb) {
        col(userId).document(equipmentId)
                .delete()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Get All Equipment for User ---------- */
    public void getAll(String userId, final GetAllCallback cb) {
        col(userId).get().addOnCompleteListener(t -> {
            if (!t.isSuccessful()) {
                cb.onFailure(t.getException());
                return;
            }
            List<UserEquipment> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : t.getResult()) {
                UserEquipment ue = d.toObject(UserEquipment.class);
                list.add(ue);
            }
            cb.onSuccess(list);
        });
    }

    public void getActive(String userId, final GetAllCallback cb) {
        col(userId).whereEqualTo("active", true).get().addOnCompleteListener(t -> {
            if (!t.isSuccessful()) { cb.onFailure(t.getException()); return; }

            List<UserEquipment> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : t.getResult()) {
                UserEquipment ue = d.toObject(UserEquipment.class);
                list.add(ue);
            }
            cb.onSuccess(list);
        });
    }

    public void calculateActiveEquipmentBonuses(final Runnable onComplete) {
        String userId = SessionManager.getInstance().getUserId();
        UserEquipmentRepository userRepo = new UserEquipmentRepository();
        SpecialEquipmentRepository eqRepo = new SpecialEquipmentRepository();

        SessionManager.getInstance().setUserEquipmentPP(0);
        SessionManager.getInstance().setBonusCoinPercent(0.0);

        userRepo.getAll(userId, new UserEquipmentRepository.GetAllCallback() {
            @Override
            public void onSuccess(List<UserEquipment> userEquipments) {
                if (userEquipments.isEmpty()) {
                    if (onComplete != null) onComplete.run();
                    return;
                }

                final int[] remaining = { userEquipments.size() };

                for (UserEquipment ue : userEquipments) {
                    if (ue.isActive()) {
                        eqRepo.getById(ue.getEquipmentId(), new SpecialEquipmentRepository.GetOneCallback() {
                            @Override
                            public void onSuccess(SpecialEquipment eq) {
                                if (eq != null) {
                                    SessionManager session = SessionManager.getInstance();
                                    session.setUserEquipmentPP(session.getUserEquipmentPP() + eq.getBonusPP());
                                    session.setBonusCoinPercent(session.getBonusCoinPercent() + eq.getBonusCoinPercent());
                                }
                                if (--remaining[0] == 0 && onComplete != null) onComplete.run();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (--remaining[0] == 0 && onComplete != null) onComplete.run();
                            }
                        });
                    } else {
                        if (--remaining[0] == 0 && onComplete != null) onComplete.run();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (onComplete != null) onComplete.run();
            }
        });
    }



    /* ---------- Callbacks ---------- */
    public interface VoidCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    public interface GetAllCallback {
        void onSuccess(List<UserEquipment> list);
        void onFailure(Exception e);
    }

}
