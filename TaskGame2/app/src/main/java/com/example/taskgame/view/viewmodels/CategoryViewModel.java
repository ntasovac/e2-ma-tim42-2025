package com.example.taskgame.view.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.CategoryRepository;
import com.example.taskgame.domain.models.Category;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository repo = new CategoryRepository(); // or DI
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    @Nullable private ListenerRegistration reg;

    public CategoryViewModel() {
        // 🔹 Test data
        categories.setValue(Arrays.asList(
                new Category(1, "Health", 0xFFE57373),    // red
                new Category(2, "School", 0xFF64B5F6),    // blue
                new Category(3, "Sports", 0xFF81C784)     // green
        ));
    }

    public LiveData<List<Category>> getCategories() { return categories; }

    public boolean addCategory(String name, int color) {
        List<Category> current = categories.getValue();
        if (current == null) current = new ArrayList<>();
        for (Category c : current) if (c.getColor() == color) return false; // unique color
        long id = System.currentTimeMillis();
        List<Category> updated = new ArrayList<>(current);
        updated.add(new Category(id, name, color));
        categories.setValue(updated);
        return true;
    }

    public boolean updateCategory(long id, String newName, int newColor) {
        List<Category> list = categories.getValue();
        if (list == null) return false;

        // unique color among other categories
        for (Category c : list) {
            if (c.getId() != id && c.getColor() == newColor) return false;
        }

        List<Category> updated = new ArrayList<>(list.size());
        for (Category c : list) {
            if (c.getId() == id) {
                updated.add(new Category(id, newName, newColor));
            } else {
                updated.add(c);
            }
        }
        categories.setValue(updated);
        return true;
    }


    public void startObserving() {
        if (reg != null) return;
        reg = repo.observeCategories(new CategoryRepository.StreamCallback() {
            @Override public void onChanged(List<Category> list) { categories.postValue(list); }
            @Override public void onFailure(Exception e) { /* log / surface error if needed */ }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (reg != null) { reg.remove(); reg = null; }
    }
}
