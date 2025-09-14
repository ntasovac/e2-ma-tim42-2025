package com.example.taskgame.view.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.TaskRepository;
import com.example.taskgame.domain.models.Task;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends ViewModel {

    private final TaskRepository repo = new TaskRepository();

    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    private @Nullable ListenerRegistration reg;
    private @Nullable String userId; // optional: set if you store per-user

    /* ---------- Expose state ---------- */
    public LiveData<List<Task>> getTasks() { return tasks; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    /* ---------- Scope to a user (optional) ---------- */
    public void setUserId(@Nullable String uid) {
        if ((userId == null && uid == null) || (userId != null && userId.equals(uid))) return;
        userId = uid;
        restartStream();
    }

    /* ---------- Start / Restart live stream ---------- */
    public void startObserving() {
        if (reg != null) return;
        loading.postValue(true);
        reg = repo.observeAll(userId, new TaskRepository.StreamCallback() {
            @Override public void onChanged(List<Task> list) {
                loading.postValue(false);
                tasks.postValue(list);
            }
            @Override public void onFailure(Exception e) {
                loading.postValue(false);
                error.postValue(e != null ? e.getMessage() : "Unknown error");
            }
        });
    }

    public void stopObserving() {
        if (reg != null) { reg.remove(); reg = null; }
    }

    private void restartStream() {
        stopObserving();
        startObserving();
    }

    /* ---------- Create ---------- */
    public void createTask(Task t, final Result cb) {
        // compute total XP if caller forgot
        t.setTotalXp(t.getDifficultyXp() + t.getImportanceXp());
        t.setStatus("ACTIVE");
        if (t.getCreatedAtUtc() == 0L) t.setCreatedAtUtc(System.currentTimeMillis());

        repo.create(userId, t, new TaskRepository.CreateCallback() {
            @Override public void onSuccess(String id) { if (cb != null) cb.ok(id); }
            @Override public void onFailure(Exception e) { if (cb != null) cb.error(e); }
        });
    }

    /* ---------- Update ---------- */
    public void updateTask(Task t, final VoidResult cb) {
        repo.update(userId, t, new TaskRepository.VoidCallback() {
            @Override public void onSuccess() { if (cb != null) cb.ok(); }
            @Override public void onFailure(Exception e) { if (cb != null) cb.error(e); }
        });
    }

    /* ---------- Delete ---------- */
    public void deleteTask(String taskId, final VoidResult cb) {
        repo.delete(userId, taskId, new TaskRepository.VoidCallback() {
            @Override public void onSuccess() { if (cb != null) cb.ok(); }
            @Override public void onFailure(Exception e) { if (cb != null) cb.error(e); }
        });
    }

    /* ---------- Optional: get one once ---------- */
    public void fetchTask(String taskId, final OneResult cb) {
        repo.getById(userId, taskId, new TaskRepository.GetOneCallback() {
            @Override public void onSuccess(Task task) { if (cb != null) cb.ok(task); }
            @Override public void onFailure(Exception e) { if (cb != null) cb.error(e); }
        });
    }

    /* ---------- Results ---------- */
    public interface Result { void ok(String id); void error(Exception e); }
    public interface VoidResult { void ok(); void error(Exception e); }
    public interface OneResult { void ok(Task task); void error(Exception e); }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopObserving();
    }
}
