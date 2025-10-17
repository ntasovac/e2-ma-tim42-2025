package com.example.taskgame.view.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskgame.R;
import com.example.taskgame.databinding.FragmentLevelsBinding;
import com.example.taskgame.domain.enums.Title;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.viewmodels.LevelsViewModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class LevelsFragment extends Fragment {

    private FragmentLevelsBinding binding;
    private LevelsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLevelsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(LevelsViewModel.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            binding.Title.setText(user.getTitle() != null ? user.getTitle().toString() : "N/A");
            binding.Level.setText(getString(R.string.level, user.getLevel()));
            binding.PowerPoints.setText(getString(R.string.power_points, user.getPowerPoints()));
            binding.Experience.setText(getString(R.string.experience_points, user.getExperience()));

            double thresholdLevel = SessionManager.getInstance().getTotalXPforLEVEL();
            int neededXp = (int) (thresholdLevel - user.getExperience());
            Log.d("XPSystem", "🎯 Threshold XP for level "
                    + SessionManager.getInstance().getUserLevel()
                    + " = " + thresholdLevel);
            binding.LevelThreshold.setText(getString(R.string.neededXp, neededXp));
            binding.progressLevel.setProgress(viewModel.getProgressPercent());
        });
        binding.gainXpButton.setOnClickListener(v ->{
            viewModel.earnXP();
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}