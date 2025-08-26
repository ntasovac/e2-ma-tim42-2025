package com.example.taskgame.view.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskgame.R;
import com.example.taskgame.databinding.FragmentProfileBinding;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.viewmodels.ProfileViewModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                bindUser(user);
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    String data = user.getUsername() + ";" + user.getEmail();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(
                            data,
                            BarcodeFormat.QR_CODE,
                            200,
                            200
                    );
                    binding.profileQrCode.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.changePasswordButton.setOnClickListener(v -> {
            viewModel.setCurrentPassword(binding.currentPasswordInput.getText().toString());
            viewModel.setNewPassword(binding.changePasswordInput.getText().toString());
            viewModel.setConfirmPassword(binding.confirmNewPasswordInput.getText().toString());

            viewModel.changePassword();
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void bindUser(User user) {
        binding.profileUsername.setText(user.getUsername());
        binding.profileAvatar.setImageResource(getAvatarDrawable(user.getAvatar()));
        binding.profileLevel.setText(getString(R.string.level, user.getLevel()));
        binding.profileTitle.setText(getString(R.string.title, user.getTitle().name()));
        binding.profilePowerPoints.setText(getString(R.string.power_points, user.getPowerPoints()));
        binding.profileExperience.setText(getString(R.string.experience_points, user.getExperience()));
        binding.profileCoins.setText(getString(R.string.coins, user.getCoins()));
    }

    public static int getAvatarDrawable(int avatarNumber) {
        switch (avatarNumber) {
            case 1: return R.drawable.gojo;
            case 2: return R.drawable.eren;
            case 3: return R.drawable.mikasa;
            case 4: return R.drawable.kaneki;
            case 5: return R.drawable.hinata;
            default: return R.drawable.gojo;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
