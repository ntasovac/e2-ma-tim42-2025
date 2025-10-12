package com.example.taskgame.view.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.CategoryRepository;
import com.example.taskgame.domain.models.Category;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository repo = new CategoryRepository();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    @Nullable private ListenerRegistration reg;

    public LiveData<List<Category>> getCategories() { return categories; }

    /** Start Firestore realtime stream */
    public void startObserving() {
        if (reg != null) return;
        reg = repo.observeCategories(new CategoryRepository.StreamCallback() {
            @Override public void onChanged(List<Category> list) { categories.postValue(list); }
            @Override public void onFailure(Exception e) { /* TODO: surface/log if you want */ }
        });
    }

    /** Create new category in Firestore (repo enforces unique color) */
    public void addCategory(String name, int color, Result cb) {
        long id = System.currentTimeMillis(); // your model uses long id
        Category c = new Category(id, name, color);
        repo.addCategory(c, new CategoryRepository.CreateCallback() {
            @Override public void onSuccess() { if (cb != null) cb.ok(); }
            @Override public void onFailure(Exception e) { if (cb != null) cb.error(e); }
        });
    }

    /** Update existing category (name/color); repo checks unique color */
    public void updateCategory(long id, String name, int color, Result cb) {
        Category updated = new Category(id, name, color);
        repo.updateCategory(updated, new CategoryRepository.UpdateCallback() {
            @Override public void onSuccess() { if (cb != null) cb.ok(); }
            @Override public void onFailure(Exception e) { if (cb != null) cb.error(e); }
        });
    }

    public interface Result { void ok(); void error(Exception e); }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (reg != null) { reg.remove(); reg = null; }
    }
}
