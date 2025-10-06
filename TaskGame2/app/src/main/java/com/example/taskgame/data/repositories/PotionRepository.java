package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.Potion;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class PotionRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference col(String userId) {
        return db.collection("users").document(userId).collection("potions");
    }

    /* ---------- Create ---------- */
    public void addPotion(String userId, Potion potion, final VoidCallback cb) {
        if (userId == null || userId.isEmpty()) {
            cb.onFailure(new IllegalArgumentException("UserId missing"));
            return;
        }
        col(userId).document(potion.getId())
                .set(potion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(task.getException());
                });
    }

    /* ---------- Get All ---------- */
    public void getAllPotions(String userId, final GetAllCallback cb) {
        col(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                cb.onFailure(task.getException());
                return;
            }
            List<Potion> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : task.getResult()) {
                Potion p = d.toObject(Potion.class);
                list.add(p);
            }
            cb.onSuccess(list);
        });
    }

    /* ---------- Get One ---------- */
    public void getPotion(String userId, String potionId, final GetOneCallback cb) {
        col(userId).document(potionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        cb.onFailure(task.getException());
                        return;
                    }
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        cb.onSuccess(doc.toObject(Potion.class));
                    } else {
                        cb.onSuccess(null);
                    }
                });
    }

    /* ---------- Update ---------- */
    public void updatePotion(String userId, Potion potion, final VoidCallback cb) {
        col(userId).document(potion.getId())
                .set(potion, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(task.getException());
                });
    }

    /* ---------- Delete ---------- */
    public void deletePotion(String userId, String potionId, final VoidCallback cb) {
        col(userId).document(potionId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(task.getException());
                });
    }

    /* ---------- Callbacks ---------- */
    public interface VoidCallback { void onSuccess(); void onFailure(Exception e); }
    public interface GetOneCallback { void onSuccess(Potion potion); void onFailure(Exception e); }
    public interface GetAllCallback { void onSuccess(List<Potion> list); void onFailure(Exception e); }
}
