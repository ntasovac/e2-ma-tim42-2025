package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.SpecialMission;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class SpecialMissionRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Root collection: alliances/{allianceId}/specialMissions
    private CollectionReference col(String allianceId) {
        return db.collection("alliancesNew").document(allianceId).collection("specialMissions");
    }

    /* ---------- Create ---------- */
    public void create(String allianceId, SpecialMission mission, final CreateCallback cb) {
        if (mission.getId() == null || mission.getId().isEmpty()) {
            String id = col(allianceId).document().getId();
            mission.setId(id);
        }
        col(allianceId).document(mission.getId())
                .set(mission)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess(mission.getId());
                    else cb.onFailure(t.getException());
                });
    }

    public void getByAllianceId(String allianceId, final GetOneCallback cb) {
        db.collection("alliancesNew").document(allianceId).collection("specialMissions")
                .whereEqualTo("allianceId", allianceId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        cb.onFailure(task.getException());
                        return;
                    }

                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot d = task.getResult().getDocuments().get(0);
                        SpecialMission mission = d.toObject(SpecialMission.class);
                        cb.onSuccess(mission);
                    } else {
                        cb.onSuccess(null); // no mission found
                    }
                });
    }

    /* ---------- Update existing Special Mission ---------- */
    public void update(SpecialMission mission, final VoidCallback cb) {
        if (mission.getAllianceId() == null || mission.getAllianceId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("Alliance ID is missing"));
            return;
        }

        col(mission.getAllianceId())
                .document(mission.getAllianceId()) // or mission.getId() if you store multiple missions
                .set(mission, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("✅ Special mission updated for alliance: " + mission.getAllianceId());
                        cb.onSuccess();
                    } else {
                        System.err.println("❌ Failed to update special mission: " + task.getException());
                        cb.onFailure(task.getException());
                    }
                });
    }




    /* ---------- Update (merge) ---------- */
    public void update(String allianceId, SpecialMission mission, final VoidCallback cb) {
        if (mission.getId() == null || mission.getId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("SpecialMission ID is missing"));
            return;
        }
        col(allianceId).document(mission.getId())
                .set(mission, SetOptions.merge())
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Delete ---------- */
    public void delete(String allianceId, String missionId, final VoidCallback cb) {
        col(allianceId).document(missionId)
                .delete()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Get one ---------- */
    public void getById(String allianceId, String missionId, final GetOneCallback cb) {
        col(allianceId).document(missionId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { cb.onFailure(task.getException()); return; }
            DocumentSnapshot d = task.getResult();
            if (d != null && d.exists()) {
                SpecialMission m = d.toObject(SpecialMission.class);
                cb.onSuccess(m);
            } else {
                cb.onSuccess(null);
            }
        });
    }

    /* ---------- Get all missions for an alliance ---------- */
    public void getAll(String allianceId, final GetAllCallback cb) {
        col(allianceId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { cb.onFailure(task.getException()); return; }

            List<SpecialMission> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : task.getResult()) {
                SpecialMission m = d.toObject(SpecialMission.class);
                list.add(m);
            }
            cb.onSuccess(list);
        });
    }

    /* ---------- Get active mission for alliance ---------- */
    public void getActiveMission(String allianceId, final GetOneCallback cb) {
        col(allianceId).whereEqualTo("status", "ACTIVE").limit(1).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) { cb.onFailure(task.getException()); return; }
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot d = task.getResult().getDocuments().get(0);
                        SpecialMission m = d.toObject(SpecialMission.class);
                        cb.onSuccess(m);
                    } else {
                        cb.onSuccess(null);
                    }
                });
    }

    /* ---------- Observe realtime updates ---------- */
    public ListenerRegistration observeAll(String allianceId, final StreamCallback cb) {
        return col(allianceId).addSnapshotListener((snap, e) -> {
            if (e != null) { cb.onFailure(e); return; }
            if (snap == null) { cb.onChanged(new ArrayList<>()); return; }

            List<SpecialMission> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                SpecialMission m = d.toObject(SpecialMission.class);
                list.add(m);
            }
            cb.onChanged(list);
        });
    }

    /* ---------- Callbacks ---------- */
    public interface CreateCallback { void onSuccess(String id); void onFailure(Exception e); }
    public interface VoidCallback { void onSuccess(); void onFailure(Exception e); }
    public interface GetOneCallback { void onSuccess(SpecialMission mission); void onFailure(Exception e); }
    public interface GetAllCallback { void onSuccess(List<SpecialMission> list); void onFailure(Exception e); }
    public interface StreamCallback { void onChanged(List<SpecialMission> list); void onFailure(Exception e); }
}
