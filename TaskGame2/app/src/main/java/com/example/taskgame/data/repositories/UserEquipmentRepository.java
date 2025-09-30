package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.UserEquipment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
