package com.example.taskgame.data.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import com.example.taskgame.domain.models.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore repository for categories.
 * Collection: "categories"
 * Fields (example): { id: String, name: String, color: int }
 */
public class CategoryRepository {

    private final FirebaseFirestore db;
    private final CollectionReference categoriesCol;
    private final CollectionReference tasksCol; // optional, used when propagating color changes

    public CategoryRepository() {
        db = FirebaseFirestore.getInstance();
        categoriesCol = db.collection("categories");
        tasksCol = db.collection("tasks");
    }

    /* ----------------------------- CREATE ----------------------------- */

    /** Adds a category if no other category uses the same color. */
    public void addCategory(final Category category, final CreateCallback callback) {
        // enforce unique color
        categoriesCol.whereEqualTo("color", category.getColor())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure(task.getException());
                        return;
                    }
                    if (!task.getResult().isEmpty()) {
                        callback.onFailure(new IllegalStateException("Color already taken"));
                        return;
                    }
                    // use category.id as document id (String)
                    categoriesCol.document(String.valueOf(category.getId()))
                            .set(category)
                            .addOnCompleteListener(voidTask -> {
                                if (voidTask.isSuccessful()) callback.onSuccess();
                                else callback.onFailure(voidTask.getException());
                            });
                });
    }

    /* ----------------------------- READ ------------------------------ */

    /** One-shot load of all categories. */
    public void getAllCategories(final LoadCallback callback) {
        categoriesCol.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Category> list = task.getResult().toObjects(Category.class);
                callback.onLoaded(list);
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    /**
     * Realtime stream of categories.
     * Remember to keep a reference to ListenerRegistration and call remove() when done.
     */
    public ListenerRegistration observeCategories(final StreamCallback callback) {
        return categoriesCol.addSnapshotListener((snap, e) -> {
            if (e != null) {
                callback.onFailure(e);
                return;
            }
            if (snap != null) {
                List<Category> list = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    Category c = d.toObject(Category.class);
                    if (c != null) list.add(c);
                }
                callback.onChanged(list);
            }
        });
    }

    /* ----------------------------- UPDATE ---------------------------- */

    /** Update name/color with uniqueness check for color. */
    public void updateCategory(final Category updated, final UpdateCallback callback) {
        categoriesCol.whereEqualTo("color", updated.getColor())
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure(task.getException());
                        return;
                    }
                    // color can be the same if it's this document
                    boolean colorTakenByOther = false;
                    for (DocumentSnapshot d : task.getResult()) {
                        if (!d.getId().equals(String.valueOf(updated.getId()))) {
                            colorTakenByOther = true;
                            break;
                        }
                    }
                    if (colorTakenByOther) {
                        callback.onFailure(new IllegalStateException("Color already taken"));
                        return;
                    }
                    categoriesCol.document(String.valueOf(updated.getId()))
                            .set(updated, SetOptions.merge())
                            .addOnCompleteListener(vt -> {
                                if (vt.isSuccessful()) callback.onSuccess();
                                else callback.onFailure(vt.getException());
                            });
                });
    }

    /**
     * Change color and propagate to all tasks with this categoryId.
     * Assumes tasks documents have fields: categoryId (String) and categoryColor (int) used by calendar.
     */
    public void updateCategoryColorAndPropagate(
            final String categoryId,
            final int newColor,
            final UpdateCallback callback
    ) {
        // Ensure color is unique
        categoriesCol.whereEqualTo("color", newColor).limit(1).get()
                .addOnCompleteListener(t -> {
                    if (!t.isSuccessful()) {
                        callback.onFailure(t.getException());
                        return;
                    }
                    if (!t.getResult().isEmpty()) {
                        // If the found doc IS this category, it's fine; otherwise taken
                        DocumentSnapshot d = t.getResult().getDocuments().get(0);
                        if (!d.getId().equals(categoryId)) {
                            callback.onFailure(new IllegalStateException("Color already taken"));
                            return;
                        }
                    }

                    // 1) Update category
                    categoriesCol.document(categoryId)
                            .update("color", newColor)
                            .addOnCompleteListener(catUpdate -> {
                                if (!catUpdate.isSuccessful()) {
                                    callback.onFailure(catUpdate.getException());
                                    return;
                                }
                                // 2) Update all tasks that reference this category
                                tasksCol.whereEqualTo("categoryId", categoryId).get()
                                        .addOnCompleteListener(taskQuery -> {
                                            if (!taskQuery.isSuccessful()) {
                                                callback.onFailure(taskQuery.getException());
                                                return;
                                            }
                                            WriteBatch batch = db.batch();
                                            for (DocumentSnapshot ds : taskQuery.getResult().getDocuments()) {
                                                batch.update(ds.getReference(), "categoryColor", newColor);
                                            }
                                            batch.commit().addOnCompleteListener(commitTask -> {
                                                if (commitTask.isSuccessful()) callback.onSuccess();
                                                else callback.onFailure(commitTask.getException());
                                            });
                                        });
                            });
                });
    }

    /* ----------------------------- DELETE ---------------------------- */

    public void deleteCategory(final String categoryId, final DeleteCallback callback) {
        categoriesCol.document(categoryId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onFailure(task.getException());
                });
    }

    /* ----------------------------- CALLBACKS ------------------------- */

    public interface CreateCallback { void onSuccess(); void onFailure(Exception e); }
    public interface UpdateCallback { void onSuccess(); void onFailure(Exception e); }
    public interface DeleteCallback { void onSuccess(); void onFailure(Exception e); }
    public interface LoadCallback   { void onLoaded(List<Category> categories); void onFailure(Exception e); }
    public interface StreamCallback { void onChanged(List<Category> categories); void onFailure(Exception e); }
}
