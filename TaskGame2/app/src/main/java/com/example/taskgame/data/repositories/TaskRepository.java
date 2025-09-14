package com.example.taskgame.data.repositories;

import androidx.annotation.NonNull;

import com.example.taskgame.domain.models.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Store tasks either globally ("tasks") or per-user ("users/{uid}/tasks")
    private CollectionReference col(String userId) {
        if (userId == null || userId.isEmpty()) {
            return db.collection("tasks");
        }
        return db.collection("users").document(userId).collection("tasks");
    }

    /* ---------- Create ---------- */
    public void create(String userId, Task task, final CreateCallback cb) {
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(col(userId).document().getId());
        }
        col(userId).document(task.getId())
                .set(task)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess(task.getId());
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Update (merge whole object) ---------- */
    public void update(String userId, Task task, final VoidCallback cb) {
        if (task.getId() == null || task.getId().isEmpty()) {
            cb.onFailure(new IllegalArgumentException("Task id missing"));
            return;
        }
        col(userId).document(task.getId())
                .set(task) // replace; use .set(task, SetOptions.merge()) if you prefer partials
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Delete ---------- */
    public void delete(String userId, String taskId, final VoidCallback cb) {
        col(userId).document(taskId)
                .delete()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess();
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Stream all (ordered) ---------- */
    public ListenerRegistration observeAll(String userId, final StreamCallback cb) {
        Query q = col(userId).orderBy("createdAtUtc", Query.Direction.DESCENDING);
        return q.addSnapshotListener((snap, e) -> {
            if (e != null) { cb.onFailure(e); return; }
            if (snap == null) { cb.onChanged(new ArrayList<>()); return; }

            List<Task> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Task t = d.toObject(Task.class);
                if (t.getId() == null || t.getId().isEmpty()) t.setId(d.getId());
                list.add(t);
            }
            cb.onChanged(list);
        });
    }

    /* ---------- Optional: get single once ---------- */
    public void getById(String userId, String taskId, final GetOneCallback cb) {
        col(userId).document(taskId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { cb.onFailure(task.getException()); return; }
            DocumentSnapshot d = task.getResult();
            if (d != null && d.exists()) {
                Task t = d.toObject(Task.class);
                if (t != null && (t.getId() == null || t.getId().isEmpty())) t.setId(d.getId());
                cb.onSuccess(t);
            } else {
                cb.onSuccess(null);
            }
        });
    }

    /* ---------- Callbacks ---------- */
    public interface CreateCallback { void onSuccess(String id); void onFailure(Exception e); }
    public interface VoidCallback { void onSuccess(); void onFailure(Exception e); }
    public interface GetOneCallback { void onSuccess(Task task); void onFailure(Exception e); }
    public interface StreamCallback { void onChanged(List<Task> list); void onFailure(Exception e); }
}
