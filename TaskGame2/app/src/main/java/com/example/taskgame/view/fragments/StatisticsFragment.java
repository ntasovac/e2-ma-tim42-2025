package com.example.taskgame.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taskgame.R;
import com.example.taskgame.databinding.FragmentStatisticsBinding;
import com.example.taskgame.view.viewmodels.StatisticsViewModel;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private StatisticsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        viewModel.getStreakCount().observe(getViewLifecycleOwner(), streak ->
                binding.tvStreak.setText(getString(R.string.streak, streak))
        );

        viewModel.updateStreak();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}