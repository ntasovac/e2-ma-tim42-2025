package com.example.taskgame.data.repositories;

import android.util.Log;

import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.SessionManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BossRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference col() {
        return db.collection("bosses");
    }

    /* ---------- Create ---------- */
    public void create(Boss boss, final CreateCallback cb) {
        if (boss.getUserId() == null) {
            cb.onFailure(new IllegalArgumentException("UserId is missing"));
            return;
        }

        String id = "boss_" + boss.getUserId() + "_" + boss.getBossIndex();
        boss.setId(id);

        col().document(id)
                .set(boss)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        cb.onSuccess(id);
                        Log.d("BossRepository", "✅ Created boss " + id + " for user " + boss.getUserId());
                    } else {
                        cb.onFailure(t.getException());
                        Log.e("BossRepository", "❌ Failed to create boss", t.getException());
                    }
                });
    }

    /* ---------- Update ---------- */
    public void update(Boss boss, final VoidCallback cb) {
        if (boss.getUserId() == null) {
            cb.onFailure(new IllegalArgumentException("UserId is missing"));
            return;
        }

        String id = boss.getId() != null ? boss.getId() :
                "boss_" + boss.getUserId() + "_" + boss.getBossIndex();

        col().document(id)
                .set(boss, SetOptions.merge())
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Delete ---------- */
    public void delete(String bossId, final VoidCallback cb) {
        col().document(bossId)
                .delete()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Get all getFirstActiveBoss for one user ---------- */
    public void getAllByUserId(String userId, final GetAllCallback cb) {
        col().whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        cb.onFailure(task.getException());
                        return;
                    }

                    List<Boss> bosses = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Boss b = doc.toObject(Boss.class);
                        bosses.add(b);
                    }
                    cb.onSuccess(bosses);
                });
    }

    /* ---------- Get one boss by ID ---------- */
    public void getById(String bossId, final GetOneCallback cb) {
        col().document(bossId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        cb.onFailure(task.getException());
                        return;
                    }

                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        Boss boss = doc.toObject(Boss.class);
                        cb.onSuccess(boss);
                    } else {
                        cb.onSuccess(null);
                    }
                });
    }

    /* ---------- Get first active boss for a user ---------- */
    public void getFirstActiveBoss(String userId, final GetOneCallback cb) {
        Log.d("BossRepo", "🔍 getFirstActiveBoss() called for userId: " + userId);

        Long luserId = Long.parseLong(userId);
        col()
                .whereEqualTo("userId", luserId)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("level", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("BossRepo", "❌ Firestore query failed", task.getException());
                        cb.onFailure(task.getException());
                        return;
                    }

                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        Boss boss = doc.toObject(Boss.class);
                        Log.d("BossRepo", "🏆 Found active boss: " +
                                (boss != null ? boss.getName() + " (Level " + boss.getLevel() + ")" : "null"));
                        cb.onSuccess(boss);
                    } else {
                        Log.w("BossRepo", "⚠️ No active bosses found for userId: " + userId);
                        cb.onSuccess(null);
                    }
                });
    }
    public void getFirstActiveOrPendingBoss(String userId, final GetOneCallback cb) {
        Log.d("BossRepo", "🔍 getFirstActiveBoss() called for userId: " + userId);

        Long luserId = Long.parseLong(userId);
        col()
                .whereEqualTo("userId", luserId)
                .whereIn("status", Arrays.asList("ACTIVE", "PENDING"))
                .orderBy("level", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("BossRepo", "❌ Firestore query failed", task.getException());
                        cb.onFailure(task.getException());
                        return;
                    }

                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        Boss boss = doc.toObject(Boss.class);
                        Log.d("BossRepo", "🏆 Found active boss: " +
                                (boss != null ? boss.getName() + " (Level " + boss.getLevel() + ")" : "null"));
                        cb.onSuccess(boss);
                    } else {
                        Log.w("BossRepo", "⚠️ No active bosses found for userId: " + userId);
                        cb.onSuccess(null);
                    }
                });
    }

    /* ---------- Reset all pending bosses for next fight ---------- */
    public void resetAllPendingBossesForNextFight(String userId, final VoidCallback cb) {
        col().whereEqualTo("userId", userId)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        cb.onFailure(task.getException());
                        return;
                    }

                    if (task.getResult().isEmpty()) {
                        cb.onFailure(new IllegalStateException("No pending bosses found"));
                        return;
                    }

                    for (DocumentSnapshot doc : task.getResult()) {
                        doc.getReference().update("status", "ACTIVE", "availableAttacks", 5)
                                .addOnFailureListener(cb::onFailure);
                    }
                    cb.onSuccess();
                });
    }

    /* ---------- Observe all bosses of a user in real-time ---------- */
    public ListenerRegistration observeByUserId(String userId, final StreamCallback cb) {
        return col()
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        cb.onFailure(e);
                        return;
                    }

                    List<Boss> list = new ArrayList<>();
                    if (snap != null) {
                        for (QueryDocumentSnapshot doc : snap) {
                            Boss b = doc.toObject(Boss.class);
                            list.add(b);
                        }
                    }
                    cb.onChanged(list);
                });
    }

    /* ---------- Set boss to PENDING using bossId ---------- */
    public void setBossPending(String bossId, final VoidCallback cb) {
        col().document(bossId)
                .update("status", "PENDING")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cb.onSuccess();
                        Log.d("BossRepository", "✅ Boss " + bossId + " set to PENDING");
                    } else {
                        cb.onFailure(task.getException());
                        Log.e("BossRepository", "❌ Failed to set boss pending", task.getException());
                    }
                });
    }



    /* ---------- Interfaces ---------- */
    public interface CreateCallback { void onSuccess(String id); void onFailure(Exception e); }
    public interface VoidCallback { void onSuccess(); void onFailure(Exception e); }
    public interface GetOneCallback { void onSuccess(Boss boss); void onFailure(Exception e); }
    public interface GetAllCallback { void onSuccess(List<Boss> list); void onFailure(Exception e); }
    public interface StreamCallback { void onChanged(List<Boss> list); void onFailure(Exception e); }
}
