package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.Badge;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class BadgeRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Collection path: users/{userId}/badges
    private CollectionReference col(String userId) {
        return db.collection("users").document(userId).collection("badges");
    }

    /* ---------- Create ---------- */
    public void create(Badge badge, final CreateCallback cb) {
        if (badge.getUserId() == null || badge.getUserId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("User ID is missing"));
            return;
        }

        if (badge.getId() == null || badge.getId().isEmpty()) {
            badge.setId(col(badge.getUserId()).document().getId());
        }

        col(badge.getUserId()).document(badge.getId())
                .set(badge)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess(badge.getId());
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Increment badge count or create if missing ---------- */
    public void incrementBadge(String userId, String badgeName, final VoidCallback cb) {
        col(userId)
                .whereEqualTo("name", badgeName)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        cb.onFailure(task.getException());
                        return;
                    }

                    if (task.getResult().isEmpty()) {
                        // Create new badge if not found
                        Badge newBadge = new Badge(userId, badgeName);
                        create(newBadge, new CreateCallback() {
                            @Override public void onSuccess(String id) { cb.onSuccess(); }
                            @Override public void onFailure(Exception e) { cb.onFailure(e); }
                        });
                    } else {
                        // Increment existing one
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        Badge existing = doc.toObject(Badge.class);
                        if (existing != null) {
                            int newCount = existing.getCount() + 1;
                            col(userId).document(doc.getId())
                                    .update("count", newCount)
                                    .addOnSuccessListener(aVoid -> cb.onSuccess())
                                    .addOnFailureListener(cb::onFailure);
                        }
                    }
                });
    }

    /* ---------- Get all badges for user ---------- */
    public void getAllByUser(String userId, final GetAllCallback cb) {
        col(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                cb.onFailure(task.getException());
                return;
            }

            List<Badge> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : task.getResult()) {
                Badge b = d.toObject(Badge.class);
                list.add(b);
            }
            cb.onSuccess(list);
        });
    }

    /* ---------- Callbacks ---------- */
    public interface CreateCallback { void onSuccess(String id); void onFailure(Exception e); }
    public interface VoidCallback { void onSuccess(); void onFailure(Exception e); }
    public interface GetAllCallback { void onSuccess(List<Badge> list); void onFailure(Exception e); }
}
