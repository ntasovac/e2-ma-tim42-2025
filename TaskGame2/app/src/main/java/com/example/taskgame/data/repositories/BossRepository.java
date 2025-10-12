package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.Boss;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .set(boss, SetOptions.merge()) // 🔹 merge instead of overwrite
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Reset availableAttacks to 5 and set status ACTIVE ---------- */
    public void resetAllPendingBossesForNextFight(String userId, final VoidCallback cb) {
        col(userId)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        cb.onFailure(task.getException() != null ? task.getException() :
                                new IllegalStateException("Failed to fetch pending bosses"));
                        return;
                    }

                    if (task.getResult().isEmpty()) {
                        cb.onFailure(new IllegalStateException("No pending bosses found"));
                        return;
                    }

                    // update all pending bosses
                    for (DocumentSnapshot d : task.getResult().getDocuments()) {
                        String bossId = d.getId();
                        col(userId).document(bossId)
                                .update("status", "ACTIVE", "availableAttacks", 5)
                                .addOnFailureListener(cb::onFailure);
                    }

                    cb.onSuccess();
                });
    }


    /* ---------- Set status to PENDING ---------- */
    public void setBossPending(String userId, int bossIndex, final VoidCallback cb) {
        String id = "boss_" + bossIndex;
        col(userId).document(id)
                .update("status", "PENDING")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cb.onSuccess();
                    } else {
                        cb.onFailure(task.getException());
                    }
                });
    }

    public void setAllActiveBossesToPending(String userId, final VoidCallback cb) {
        col(userId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        cb.onFailure(task.getException());
                        return;
                    }

                    if (task.getResult() == null || task.getResult().isEmpty()) {
                        cb.onSuccess(); // nothing to update
                        return;
                    }

                    // Track how many updates succeed/fail
                    final int total = task.getResult().size();
                    final int[] completed = {0};
                    final boolean[] failed = {false};

                    for (DocumentSnapshot d : task.getResult().getDocuments()) {
                        d.getReference().update("status", "PENDING")
                                .addOnCompleteListener(updateTask -> {
                                    completed[0]++;
                                    if (!updateTask.isSuccessful()) {
                                        failed[0] = true;
                                    }
                                    if (completed[0] == total) {
                                        if (failed[0]) cb.onFailure(new Exception("One or more updates failed"));
                                        else cb.onSuccess();
                                    }
                                });
                    }
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

    /* ---------- Get the first active boss (lowest level) ---------- */
    public void getFirstActiveBoss(String userId, final GetOneCallback cb) {
        col(userId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        cb.onFailure(task.getException());
                        return;
                    }

                    Boss lowest = null;
                    if (task.getResult() != null) {
                        for (DocumentSnapshot d : task.getResult()) {
                            Boss b = d.toObject(Boss.class);
                            if (b != null && (lowest == null || b.getLevel() < lowest.getLevel())) {
                                lowest = b;
                            }
                        }
                    }
                    cb.onSuccess(lowest);
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
