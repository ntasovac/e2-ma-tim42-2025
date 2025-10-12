package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.Alliance;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class AllianceRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Root collection: alliances
    private CollectionReference col() {
        return db.collection("alliancesNew");
    }

    /* ---------- Create ---------- */
    public void create(Alliance alliance, final CreateCallback cb) {
        if (alliance.getId() == null || alliance.getId().isEmpty()) {
            String id = col().document().getId();
            alliance.setId(id);
        }
        col().document(alliance.getId())
                .set(alliance)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess(alliance.getId());
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Get number of participants ---------- */

    /* ---------- Update (merge fields) ---------- */
    public void update(Alliance alliance, final VoidCallback cb) {
        if (alliance.getId() == null || alliance.getId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("Alliance ID is missing"));
            return;
        }
        col().document(alliance.getId())
                .set(alliance, SetOptions.merge())
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Delete ---------- */
    public void delete(String allianceId, final VoidCallback cb) {
        col().document(allianceId)
                .delete()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Get one by ID ---------- */
    public void getById(String allianceId, final GetOneCallback cb) {
        col().document(allianceId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { cb.onFailure(task.getException()); return; }
            DocumentSnapshot d = task.getResult();
            if (d != null && d.exists()) {
                Alliance a = d.toObject(Alliance.class);
                cb.onSuccess(a);
            } else {
                cb.onSuccess(null);
            }
        });
    }

    /* ---------- Get all alliances ---------- */
    public void getAll(final GetAllCallback cb) {
        col().get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { cb.onFailure(task.getException()); return; }

            List<Alliance> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : task.getResult()) {
                Alliance a = d.toObject(Alliance.class);
                list.add(a);
            }
            cb.onSuccess(list);
        });
    }

    /* ---------- Get alliance by userId ---------- */
    public void getByUserId(String userId, final GetOneCallback cb) {
        col().whereArrayContains("participantIds", userId)
                .limit(1) // a user should only belong to one alliance
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        cb.onFailure(task.getException());
                        return;
                    }

                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot d = task.getResult().getDocuments().get(0);
                        Alliance a = d.toObject(Alliance.class);
                        cb.onSuccess(a);
                    } else {
                        cb.onSuccess(null); // user is not in any alliance
                    }
                });
    }

    /* ---------- Observe realtime ---------- */
    public ListenerRegistration observeAll(final StreamCallback cb) {
        return col().addSnapshotListener((snap, e) -> {
            if (e != null) { cb.onFailure(e); return; }
            if (snap == null) { cb.onChanged(new ArrayList<>()); return; }

            List<Alliance> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Alliance a = d.toObject(Alliance.class);
                list.add(a);
            }
            cb.onChanged(list);
        });
    }

    /* ---------- Special Mission Helpers ---------- */
    public void startSpecialMission(String allianceId, String specialBossId, final VoidCallback cb) {
        col().document(allianceId)
                .update("specialMissionActive", true, "specialBossId", specialBossId)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    public void endSpecialMission(String allianceId, final VoidCallback cb) {
        col().document(allianceId)
                .update("specialMissionActive", false, "specialBossId", null)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Callbacks ---------- */
    public interface CreateCallback { void onSuccess(String id); void onFailure(Exception e); }
    public interface VoidCallback { void onSuccess(); void onFailure(Exception e); }
    public interface GetOneCallback { void onSuccess(Alliance alliance); void onFailure(Exception e); }
    public interface GetAllCallback { void onSuccess(List<Alliance> list); void onFailure(Exception e); }
    public interface StreamCallback { void onChanged(List<Alliance> list); void onFailure(Exception e); }
}
