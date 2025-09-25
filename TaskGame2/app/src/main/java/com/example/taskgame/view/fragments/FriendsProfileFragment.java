package com.example.taskgame.view.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.taskgame.R;
import com.example.taskgame.databinding.FragmentFriendsProfileBinding;
import com.example.taskgame.databinding.FragmentProfileBinding;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.adapters.OwnedEquipmentListAdapter;
import com.example.taskgame.view.viewmodels.FriendsProfileViewModel;
import com.example.taskgame.view.viewmodels.FriendsViewModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;


public class FriendsProfileFragment extends Fragment {

    private FriendsProfileViewModel viewModel;
    private FragmentFriendsProfileBinding binding;
    public static ArrayList<Equipment> equipment = new ArrayList<>();
    private OwnedEquipmentListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFriendsProfileBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(FriendsProfileViewModel.class);

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
                    binding.otherProfileQrCode.setImageBitmap(bitmap);
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

        String friendEmail = getArguments() != null
                ? getArguments().getString("friendEmail")
                : null;

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                bindUser(user);

                equipment = new ArrayList<>(user.getEquipment());
                ArrayList<Equipment> activatedEquipment = new ArrayList<>();
                for (Equipment eq : equipment) {
                    if(eq.isActivated()){
                        activatedEquipment.add(eq);
                    }
                }


                adapter = new OwnedEquipmentListAdapter(OwnedEquipmentListAdapter.Mode.FRIEND, activatedEquipment, true, (index, item) ->{}, (index, item) ->{});
                RecyclerView recyclerView = binding.otherProfileEquipmentList;
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void bindUser(User user) {
        binding.otherProfileUsername.setText(user.getUsername());
        binding.otherProfileAvatar.setImageResource(getAvatarDrawable(user.getAvatar()));
        binding.otherProfileLevel.setText(getString(R.string.level, user.getLevel()));
        binding.otherProfileTitle.setText(getString(R.string.title, user.getTitle().name()));
        binding.otherProfileExperience.setText(getString(R.string.experience_points, user.getExperience()));
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