package com.example.taskgame.view.fragments;

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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskgame.databinding.FragmentCategoryBinding;
import com.example.taskgame.domain.models.Category;
import com.example.taskgame.view.adapters.CategoryListAdapter;
import com.example.taskgame.view.viewmodels.CategoryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryViewModel vm;
    private CategoryListAdapter adapter;

    // Shared color palette (names + ARGB ints)
    private static final String[] COLOR_NAMES = {"Red", "Blue", "Green", "Yellow", "Purple", "Teal"};
    private static final int[] COLORS = {
            0xFFE57373, // red
            0xFF64B5F6, // blue
            0xFF81C784, // green
            0xFFFFD54F, // yellow
            0xFFBA68C8, // purple
            0xFF4DB6AC  // teal
    };

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

        adapter = new CategoryListAdapter(this::showEditDialog);
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(adapter);

        vm.getCategories().observe(getViewLifecycleOwner(), adapter::submitList);
        vm.startObserving(); // Firestore realtime

        binding.fabAddCategory.setOnClickListener(v -> showAddDialog());
    }

    /* ----------------------- ADD ----------------------- */
    private void showAddDialog() {
        if (!canShowDialog()) return;

        final int[] selectedIndex = {0};

        LinearLayout container = makeDialogContainer();
        EditText etName = makeNameField("Category name");
        container.addView(etName);

        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Add category")
                .setView(container)
                .setSingleChoiceItems(COLOR_NAMES, 0, (d, which) -> selectedIndex[0] = which)
                .setPositiveButton("Create", (d, w) -> {
                    String name = safeText(etName);
                    if (name.isEmpty()) {
                        toast("Name is required");
                        return;
                    }
                    int color = COLORS[selectedIndex[0]];

                    vm.addCategory(name, color, new CategoryViewModel.Result() {
                        @Override public void ok() { toast("Created"); }
                        @Override public void error(Exception e) {
                            toast(messageOrDefault(e, "Create failed"));
                        }
                    });

                    // If you’re still on the in-memory VM version:
                    // boolean ok = vm.addCategory(name, color);
                    // if (!ok) toast("That color is already taken");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ----------------------- EDIT ----------------------- */
    private void showEditDialog(Category cat) {
        if (!canShowDialog()) return;

        int preselect = indexOfColor(cat.getColor());
        final int[] selectedIndex = {preselect < 0 ? 0 : preselect};

        LinearLayout container = makeDialogContainer();
        EditText etName = makeNameField("Category name");
        etName.setText(cat.getName());
        container.addView(etName);

        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Edit category")
                .setView(container)
                .setSingleChoiceItems(COLOR_NAMES, selectedIndex[0], (d, which) -> selectedIndex[0] = which)
                .setPositiveButton("Save", (d, w) -> {
                    String name = safeText(etName);
                    if (name.isEmpty()) {
                        toast("Name is required");
                        return;
                    }
                    int newColor = COLORS[selectedIndex[0]];

                    vm.updateCategory(cat.getId(), name, newColor, new CategoryViewModel.Result() {
                        @Override public void ok() { toast("Saved"); }
                        @Override public void error(Exception e) {
                            toast(messageOrDefault(e, "Save failed"));
                        }
                    });

                    // In-memory fallback:
                    // boolean ok = vm.updateCategory(cat.getId(), name, newColor);
                    // if (!ok) toast("That color is already taken");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* -------------------- Helpers ---------------------- */
    private boolean canShowDialog() {
        return isAdded()
                && getActivity() != null
                && getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
    }

    private int indexOfColor(int color) {
        for (int i = 0; i < COLORS.length; i++) if (COLORS[i] == color) return i;
        return -1;
    }

    private LinearLayout makeDialogContainer() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);
        return container;
    }

    private EditText makeNameField(String hint) {
        EditText et = new EditText(requireContext());
        et.setHint(hint);
        return et;
    }

    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String messageOrDefault(Exception e, String fallback) {
        return (e != null && e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : fallback;
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
