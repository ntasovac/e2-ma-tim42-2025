package com.example.taskgame.view.fragments;

import android.content.Intent;
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
import com.example.taskgame.data.repositories.BossRepository;
import com.example.taskgame.databinding.FragmentEquipmentActivationBinding;
import com.example.taskgame.databinding.FragmentProfileBinding;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.view.activities.BossFightActivity;
import com.example.taskgame.view.adapters.OwnedEquipmentListAdapter;
import com.example.taskgame.view.viewmodels.EquipmentActivationViewModel;
import com.example.taskgame.view.viewmodels.ProfileViewModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;


public class EquipmentActivationFragment extends Fragment {

    private EquipmentActivationViewModel viewModel;
    private FragmentEquipmentActivationBinding binding;
    public static ArrayList<Equipment> equipment = new ArrayList<>();
    private OwnedEquipmentListAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEquipmentActivationBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(EquipmentActivationViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
        binding.fightButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BossFightActivity.class);
            startActivity(intent);
        });*/
        binding.fightButton.setOnClickListener(v -> {
            String userId = SessionManager.getInstance().getUserId();

            viewModel.hasActiveBoss(userId, task -> {
                if (task.isSuccessful()) {
                    boolean hasBoss = task.getResult();

                    if (hasBoss) {
                        // ✅ Active boss found → start BossFightActivity
                        Intent intent = new Intent(requireContext(), BossFightActivity.class);
                        startActivity(intent);
                    } else {
                        // ⚠️ No boss found → show message
                        Toast.makeText(requireContext(),
                                "⚠️ You don’t have an active boss yet!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // ❌ Firestore error
                    Exception e = task.getException();
                    Toast.makeText(requireContext(),
                            "❌ Failed to check active boss: " +
                                    (e != null ? e.getMessage() : "unknown error"),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });


        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                equipment = new ArrayList<>(user.getEquipment());
                for (Equipment eq : equipment) {
                    Log.d("EquipmentLog", "Name: " + eq.getName() + ", Activated: " + eq.isActivated());
                }
                adapter = new OwnedEquipmentListAdapter(OwnedEquipmentListAdapter.Mode.USER,equipment, false, (index, item) -> {
                    Log.d("eq", String.valueOf(item.isActivated()));
                    viewModel.activateEquipment(getContext(), index, item, task -> {
                        if (task.isSuccessful()) {
                            adapter.updateActivated(index);
                            Toast.makeText(getContext(), "Activation successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }, null);
                RecyclerView recyclerView = binding.ownedEquipmentList;
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }
        });
    }
}