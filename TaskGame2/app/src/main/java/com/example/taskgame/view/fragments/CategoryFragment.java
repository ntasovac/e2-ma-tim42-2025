package com.example.taskgame.view.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskgame.databinding.FragmentCategoryBinding;
import com.example.taskgame.view.adapters.CategoryListAdapter;
import com.example.taskgame.view.viewmodels.CategoryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryViewModel vm;
    private CategoryListAdapter adapter;

    public static CategoryFragment newInstance() { return new CategoryFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(CategoryViewModel.class);

        adapter = new CategoryListAdapter(category -> {
            // TODO: open edit screen, or filter tasks by category
        });

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(adapter);

        vm.getCategories().observe(getViewLifecycleOwner(), adapter::submitList);

        // TEMP for testing: seed some data on first open (optional)
        // vm.seedTestData();

        // Show "Add" popup
        binding.fabAddCategory.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        // --- preset color list (name + ARGB) ---
        final String[] colorNames = {"Red", "Blue", "Green", "Yellow", "Purple", "Teal"};
        final int[] colors = {
                0xFFE57373, // red
                0xFF64B5F6, // blue
                0xFF81C784, // green
                0xFFFFD54F, // yellow
                0xFFBA68C8, // purple
                0xFF4DB6AC  // teal
        };

        // Simple vertical container with an EditText
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        final EditText etName = new EditText(requireContext());
        etName.setHint("Category name");
        container.addView(etName);

        final int[] selectedIndex = {0}; // default selected color

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add category")
                .setView(container)
                .setSingleChoiceItems(colorNames, 0, (d, which) -> selectedIndex[0] = which)
                .setPositiveButton("Create", (d, w) -> {
                    String name = etName.getText() == null ? "" : etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int color = colors[selectedIndex[0]];

                    // ViewModel should return false if that color is already taken
                    boolean ok = vm.addCategory(name, color);
                    if (!ok) {
                        Toast.makeText(requireContext(), "That color is already taken", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
