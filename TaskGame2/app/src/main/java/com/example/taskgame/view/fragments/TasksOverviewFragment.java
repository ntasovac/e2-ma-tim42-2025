package com.example.taskgame.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.taskgame.R;
import com.example.taskgame.databinding.FragmentTasksOverviewBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class TasksOverviewFragment extends Fragment {

    private FragmentTasksOverviewBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksOverviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull @Override public Fragment createFragment(int position) {
                return position == 0 ? new TasksCalendarFragment() : new TasksListFragment();
            }
            @Override public int getItemCount() { return 2; }
        });

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, pos) -> tab.setText(pos == 0 ? "Kalendar" : "Lista")).attach();

        binding.fabCreate.setOnClickListener(v1 ->
                Navigation.findNavController(requireActivity(), R.id.fragment_nav_content_main)
                        .navigate(R.id.fragmentTask) // your TaskFragment destination id
        );
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
