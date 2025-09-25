package com.example.taskgame.data.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AllianceRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final UserRepository userRepository;

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

                    return null;
                })
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> allianceData = new HashMap<>();
                    allianceData.put("name", name);
                    allianceData.put("members", new ArrayList<>());
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

}

