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

// PREVIOUS CODE
/*
public AllianceRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository();
    }

    public void createAlliance(String name, @NonNull OnCompleteListener<String> listener) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            listener.onComplete(Tasks.forException(new Exception("User not logged in")));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());

        db.runTransaction(transaction -> {

                    DocumentSnapshot snapshot = transaction.get(userRef);
                    if (!snapshot.exists()) {
                        throw new FirebaseFirestoreException(
                                "User document does not exist",
                                FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    transaction.update(userRef, "alliance", name);
                    transaction.update(userRef, "allianceOwner", true);

                    return null;
                })
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> allianceData = new HashMap<>();
                    allianceData.put("name", name);
                    allianceData.put("members", new ArrayList<>());
                    allianceData.put("isMissionActive", false);
                    userRepository.getCurrentUser().observeForever(user -> {
                        allianceData.put("owner", user);
                        db.collection("alliances")
                                .add(allianceData)
                                .addOnSuccessListener(docRef ->
                                        listener.onComplete(Tasks.forResult(name))
                                )
                                .addOnFailureListener(e ->
                                        listener.onComplete(Tasks.forException(e))
                                );
                    });
                })
                .addOnFailureListener(e ->
                        listener.onComplete(Tasks.forException(e))
                );
    }

    public MutableLiveData<Alliance> getAllianceByName(String name) {
        MutableLiveData<Alliance> liveData = new MutableLiveData<>();

        db.collection("alliances")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Alliance alliance = querySnapshot.getDocuments()
                                .get(0)
                                .toObject(Alliance.class);
                        liveData.setValue(alliance);
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));

        return liveData;
    }
    public void acceptInvite(String inviteId, String receiverEmail, String allianceName){
        db.collection("invites")
                .document(inviteId)
                .update("status", "accepted");
        userRepository.acceptInvite(receiverEmail, allianceName);
        userRepository.getUserByEmail(receiverEmail).observeForever(user -> {
            addMember(user, allianceName);
        });
    }
    public void declineInvite(String inviteId, String receiverEmail){
        db.collection("invites")
                .document(inviteId)
                .update("status", "declined");
    }
    public void addMember(User user, String allianceName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("alliances")
                .whereEqualTo("name", allianceName)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot allianceDoc = querySnapshot.getDocuments().get(0);
                        String allianceId = allianceDoc.getId();

                        db.collection("alliances")
                                .document(allianceId)
                                .update("members", FieldValue.arrayUnion(user))
                                .addOnSuccessListener(aVoid ->
                                        Log.d("AllianceRepo", "User added to members of " + allianceName))
                                .addOnFailureListener(e ->
                                        Log.e("AllianceRepo", "Failed to add member", e));
                    } else {
                        Log.w("AllianceRepo", "Alliance not found: " + allianceName);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("AllianceRepo", "Error fetching alliance", e));
    }

    public void leaveAlliance(String allianceName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w("AllianceRepo", "No logged in user.");
            return;
        }
        String userEmail = currentUser.getEmail();
        if (userEmail == null) {
            Log.w("AllianceRepo", "Current user has no email.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("alliances")
                .whereEqualTo("name", allianceName)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.w("AllianceRepo", "Alliance not found: " + allianceName);
                        return;
                    }

                    DocumentSnapshot allianceDoc = querySnapshot.getDocuments().get(0);
                    String allianceId = allianceDoc.getId();

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> members =
                            (List<Map<String, Object>>) allianceDoc.get("members");

                    if (members != null) {
                        List<Map<String, Object>> updatedMembers = new ArrayList<>();
                        for (Map<String, Object> m : members) {
                            Object emailObj = m.get("email");
                            if (emailObj == null || !userEmail.equals(emailObj.toString())) {
                                updatedMembers.add(m);
                            }
                        }

                        db.collection("alliances")
                                .document(allianceId)
                                .update("members", updatedMembers)
                                .addOnSuccessListener(aVoid ->
                                        Log.d("AllianceRepo", "Removed " + userEmail + " from members"))
                                .addOnFailureListener(e ->
                                        Log.e("AllianceRepo", "Failed to update members", e));
                    }

                    db.collection("users")
                            .document(currentUser.getUid())
                            .update("alliance", "")
                            .addOnSuccessListener(aVoid ->
                                    Log.d("AllianceRepo", "User alliance cleared"))
                            .addOnFailureListener(e ->
                                    Log.e("AllianceRepo", "Failed to clear alliance in user doc", e));
                })
                .addOnFailureListener(e ->
                        Log.e("AllianceRepo", "Error fetching alliance", e));
    }

    private void removeUserAlliance(String allianceName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 1️⃣  Pronađi sve korisnike čiji je alliance == allianceName
        db.collection("users")
                .whereEqualTo("alliance", allianceName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d("AllianceRepo", "No users found with alliance: " + allianceName);
                    } else {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String userId = doc.getId();

                            // 2️⃣  Svakom korisniku postavi alliance na ""
                            db.collection("users")
                                    .document(userId)
                                    .update("alliance", "")
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("AllianceRepo", "Alliance cleared for user: " + userId))
                                    .addOnFailureListener(e ->
                                            Log.e("AllianceRepo", "Failed to clear alliance for " + userId, e));
                        }
                    }

                    // 3️⃣  Trenutnom korisniku postavi allianceOwner na false
                    if (currentUser != null) {
                        db.collection("users")
                                .document(currentUser.getUid())
                                .update("allianceOwner", false)
                                .addOnSuccessListener(aVoid ->
                                        Log.d("AllianceRepo", "Current user's allianceOwner set to false"))
                                .addOnFailureListener(e ->
                                        Log.e("AllianceRepo", "Failed to update allianceOwner for current user", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("AllianceRepo", "Error fetching users with alliance " + allianceName, e));
    }

    public void deleteAlliance(String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("alliances")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.w("AllianceRepo", "Alliance not found: " + name);
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String docId = doc.getId();

                    db.collection("alliances")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AllianceRepo", "Alliance deleted: " + name);
                                removeUserAlliance(name);
                            })
                            .addOnFailureListener(e ->
                                    Log.e("AllianceRepo", "Failed to delete alliance: " + name, e)
                            );
                })
                .addOnFailureListener(e ->
                        Log.e("AllianceRepo", "Error fetching alliance: " + name, e)
                );
    }
 */