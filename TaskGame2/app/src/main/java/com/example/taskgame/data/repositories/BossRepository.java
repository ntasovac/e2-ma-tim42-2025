package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.Boss;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class BossRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Kolekcija: users/{uid}/bosses
    private CollectionReference col(String userId) {
        return db.collection("users").document(userId).collection("bosses");
    }

    /* ---------- Create ---------- */
    public void create(Boss boss, final CreateCallback cb) {
        if (boss.getUserId() == null || boss.getUserId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("UserId is missing"));
            return;
        }
        String id = "boss_" + boss.getBossIndex();
        col(boss.getUserId()).document(id)
                .set(boss)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess(id);
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Update ---------- */
    public void update(Boss boss, final VoidCallback cb) {
        if (boss.getUserId() == null || boss.getUserId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("UserId is missing"));
            return;
        }
        String id = "boss_" + boss.getBossIndex();
        col(boss.getUserId()).document(id)
                .set(boss)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Delete ---------- */
    public void delete(String userId, int bossIndex, final VoidCallback cb) {
        String id = "boss_" + bossIndex;
        col(userId).document(id)
                .delete()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Get one ---------- */
    public void getByIndex(String userId, int bossIndex, final GetOneCallback cb) {
        String id = "boss_" + bossIndex;
        col(userId).document(id).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { cb.onFailure(task.getException()); return; }
            DocumentSnapshot d = task.getResult();
            if (d != null && d.exists()) {
                Boss b = d.toObject(Boss.class);
                cb.onSuccess(b);
            } else {
                cb.onSuccess(null);
            }
        });
    }

    /* ---------- Get all bosses ---------- */
    public void getAll(String userId, final GetAllCallback cb) {
        col(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { cb.onFailure(task.getException()); return; }

            List<Boss> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : task.getResult()) {
                Boss b = d.toObject(Boss.class);
                list.add(b);
            }
            cb.onSuccess(list);
        });
    }

    /* ---------- Optional: Observe realtime ---------- */
    public ListenerRegistration observeAll(String userId, final StreamCallback cb) {
        return col(userId).addSnapshotListener((snap, e) -> {
            if (e != null) { cb.onFailure(e); return; }
            if (snap == null) { cb.onChanged(new ArrayList<>()); return; }

            List<Boss> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Boss b = d.toObject(Boss.class);
                list.add(b);
            }
            cb.onChanged(list);
        });
    }

    /* ---------- Callbacks ---------- */
    public interface CreateCallback { void onSuccess(String id); void onFailure(Exception e); }
    public interface VoidCallback { void onSuccess(); void onFailure(Exception e); }
    public interface GetOneCallback { void onSuccess(Boss boss); void onFailure(Exception e); }
    public interface GetAllCallback { void onSuccess(List<Boss> list); void onFailure(Exception e); }
    public interface StreamCallback { void onChanged(List<Boss> list); void onFailure(Exception e); }
}
