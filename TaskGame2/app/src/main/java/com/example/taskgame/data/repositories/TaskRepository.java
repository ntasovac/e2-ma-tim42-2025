package com.example.taskgame.data.repositories;

import android.util.Log;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /* ---------- Calculate success ratio ---------- */
    public void calculateSuccessRatio(String userId, int level, final RatioCallback cb) {
        col(userId)
                .whereEqualTo("level", level)
                .whereIn("status", List.of("DONE", "CANCELLED"))
                .get()
                .addOnCompleteListener(t -> {
                    if (!t.isSuccessful()) {
                        cb.onFailure(t.getException());
                        return;
                    }

                    int doneCount = 1;
                    int cancelledCount = 0;

                    for (QueryDocumentSnapshot d : t.getResult()) {
                        String status = d.getString("status");
                        if ("DONE".equals(status)) {
                            doneCount++;
                        } else if ("CANCELLED".equals(status)) {
                            cancelledCount++;
                        }
                    }

                    int total = doneCount + cancelledCount;
                    double ratio = (total > 0) ? (double) doneCount / total : 0.0;

                    cb.onSuccess(ratio);
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

    public void getDoneTasks(String userId, GetTasksCallback cb) {
        long id = Long.parseLong(userId);

        db.collection("tasks")
                .whereEqualTo("status", "DONE")
                .whereEqualTo("userId", id)
                .get()
                .addOnCompleteListener(t -> {
                    if (!t.isSuccessful()) {
                        cb.onFailure(t.getException());
                        return;
                    }

                    int count = 0;
                    for (QueryDocumentSnapshot d : t.getResult()) {
                        count++;
                    }
                    Log.d("TaskRepository", "Uradjenih zadataka je "+ count);
                    cb.onSuccess(count);
                });
    }
    public void getActiveTasks(String userId, GetTasksCallback cb) {
        long id = Long.parseLong(userId);
        db.collection("tasks")
                .whereEqualTo("status", "ACTIVE")
                .whereEqualTo("userId", id)
                .get()
                .addOnCompleteListener(t -> {
                    if (!t.isSuccessful()) {
                        cb.onFailure(t.getException());
                        return;
                    }

                    int count = 0;
                    for (QueryDocumentSnapshot d : t.getResult()) {
                        count++;
                        }
                    cb.onSuccess(count);
                });
    }
    public void getCancelledTasks(String userId, GetTasksCallback cb) {
        long id = Long.parseLong(userId);
        db.collection("tasks")
                .whereEqualTo("status", "CANCELLED")
                .whereEqualTo("userId", id)
                .get()
                .addOnCompleteListener(t -> {
                    if (!t.isSuccessful()) {
                        cb.onFailure(t.getException());
                        return;
                    }

                    int count = 0;
                    for (QueryDocumentSnapshot d : t.getResult()) {
                        count++;
                    }
                    cb.onSuccess(count);
                });
    }

    public void getTaskStreak(String userId, GetTasksCallback cb) {
        long id = Long.parseLong(userId);
        db.collection("tasks")
                .whereEqualTo("userId", id)
                .orderBy("startDateUtc", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(t -> {
                    int maxCount = 0;
                    int count = 0;
                    for (QueryDocumentSnapshot d : t.getResult()) {
                        String status = d.getString("status");
                        if ("DONE".equals(status)) {
                            count++;
                            maxCount = Math.max(maxCount, count);
                        } else if ("CANCELLED".equals(status)) {
                            count = 0;
                        }
                    }
                    cb.onSuccess(maxCount);
                });
    }
    public void getDoneTasksAndCategories(String userId, GetCategoriesCallback cb) {
        long id = Long.parseLong(userId);
        db.collection("tasks")
                .whereEqualTo("userId", id)
                .whereEqualTo("status", "DONE")
                .get()
                .addOnCompleteListener(t -> {
                    if (!t.isSuccessful()) {
                        cb.onFailure(t.getException());
                        return;
                    }
                    Map<String, Integer> categoryCount = new HashMap<>();

                    for (QueryDocumentSnapshot d : t.getResult()) {
                        String category = d.getString("categoryName");

                        if (category == null || category.isEmpty()) continue;

                        if (categoryCount.containsKey(category)) {
                            int current = categoryCount.get(category);
                            categoryCount.put(category, current + 1);
                        } else {
                            categoryCount.put(category, 1);
                        }
                    }

                    cb.onSuccess(categoryCount);
                });
    }
    public void getDoneTasksWeek(String userId, GetDoneTasksWeekCallback cb) {
        long id = Long.parseLong(userId);
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000;

        db.collection("tasks")
                .whereEqualTo("userId", id)
                .whereEqualTo("status", "DONE")
                .whereGreaterThanOrEqualTo("startDateUtc", sevenDaysAgo)
                .whereLessThanOrEqualTo("startDateUtc", now)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<Integer, Integer> xpPerDay = new HashMap<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Long startDateUtc = doc.getLong("startDateUtc");
                            Long totalXp = doc.getLong("totalXp");
                            if (startDateUtc == null || totalXp == null) continue;

                            int dayIndex = (int) ((startDateUtc - sevenDaysAgo) / (24L * 60 * 60 * 1000));
                            xpPerDay.put(dayIndex, xpPerDay.getOrDefault(dayIndex, 0) + totalXp.intValue());
                        }

                        cb.onSuccess(xpPerDay);
                    } else {
                        cb.onFailure(task.getException() != null
                                ? task.getException()
                                : new Exception("Unknown error retrieving tasks"));
                    }
                });
    }

    /* ---------- Callbacks ---------- */
    public interface CreateCallback { void onSuccess(String id); void onFailure(Exception e); }
    public interface VoidCallback { void onSuccess(); void onFailure(Exception e); }
    public interface GetOneCallback { void onSuccess(Task task); void onFailure(Exception e); }
    public interface StreamCallback { void onChanged(List<Task> list); void onFailure(Exception e); }

    public interface RatioCallback {
        void onSuccess(double ratio);
        void onFailure(Exception e);
    }
    public interface GetTasksCallback {
        void onSuccess(int count);
        void onFailure(Exception e);
    }
    public interface GetCategoriesCallback {
        void onSuccess(Map<String, Integer> categories);
        void onFailure(Exception e);
    }
    public interface GetDoneTasksWeekCallback {
        void onSuccess(Map<Integer, Integer> xpPerDay);
        void onFailure(Exception e);
    }


}
