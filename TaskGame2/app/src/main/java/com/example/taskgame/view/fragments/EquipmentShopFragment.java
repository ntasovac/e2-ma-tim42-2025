package com.example.taskgame.view.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskgame.R;
import com.example.taskgame.databinding.FragmentEquipmentShopBinding;
import com.example.taskgame.domain.enums.EquipmentType;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.view.adapters.EquipmentListAdapter;
import com.example.taskgame.view.viewmodels.AllianceViewModel;
import com.example.taskgame.view.viewmodels.EquipmentShopViewModel;


import java.util.ArrayList;


public class EquipmentShopFragment extends Fragment {

    public static ArrayList<Equipment> equipment = new ArrayList<>();
    private EquipmentShopViewModel equipmentShopViewModel;
    private FragmentEquipmentShopBinding binding;
    private EquipmentListFragment listFragment;
    private AllianceViewModel allianceViewModel;

    public static EquipmentShopFragment newInstance() {
        return new EquipmentShopFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        equipmentShopViewModel = new ViewModelProvider(this).get(EquipmentShopViewModel.class);
        allianceViewModel = new ViewModelProvider(this).get(AllianceViewModel.class);
        binding = FragmentEquipmentShopBinding.inflate(inflater, container, false);
        prepareEquipmentList(equipment);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listFragment = EquipmentListFragment.newInstance(equipment);
        listFragment.setOnBuyClickListener(item -> {
            Log.d("EquipmentShopFragment", "Buy clicked in shop for: " + item.getName());
            equipmentShopViewModel.buyEquipment(item, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Purchase successful!", Toast.LENGTH_SHORT).show();
                    allianceViewModel.applySpecialMissionAction(
                            SessionManager.getInstance().getUserId(),
                            "storePurchase",   // or "task", "storePurchase", etc.
                            null            // optional difficulty if needed
                    );
                } else {
                    Toast.makeText(getContext(), "Not enough coins!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        FragmentTransition.to(listFragment, requireActivity(), false, R.id.scroll_equipment_list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void prepareEquipmentList(ArrayList<Equipment> equipment) {
        int level = getArguments().getInt("bossLevel");
        int lastBossReward = (int) Math.ceil(Math.pow(6.0 / 5.0, level - 2) * 200);

        equipment.add(new Equipment(EquipmentType.POTION, "One-time potion I", (int) Math.ceil(lastBossReward * 0.5), "Gives a one time power point increase by 20%.", 20, 1, R.drawable.blue_potion, false, false));
        equipment.add(new Equipment(EquipmentType.POTION, "One-time potion II", (int) Math.ceil(lastBossReward * 0.7), "Gives a one time power point increase by 40%.", 40, 1, R.drawable.green_potion, false, false));
        equipment.add(new Equipment(EquipmentType.POTION, "Permanent potion I", (int) Math.ceil(lastBossReward * 2), "Gives a permanent power point increase by 5%.", 50, 1, R.drawable.yellow_potion, false, true));
        equipment.add(new Equipment(EquipmentType.POTION, "Permanent potion II", (int) Math.ceil(lastBossReward * 10), "Gives a permanent power point increase by 10%.", 10, 1, R.drawable.red_potion, false, true));
        equipment.add(new Equipment(EquipmentType.CLOTHING, "Gloves", (int) Math.ceil(lastBossReward * 0.6), "Gives power point increase by 10% for two boss fights.", 10, 2, R.drawable.gloves, false, false));
        equipment.add(new Equipment(EquipmentType.CLOTHING, "Shield", (int) Math.ceil(lastBossReward * 0.6), "Increases odds of landing a successful attack by 10%.", 10, 2, R.drawable.shield, false, false));
        equipment.add(new Equipment(EquipmentType.CLOTHING, "Boots", (int) Math.ceil(lastBossReward * 0.8), "Increases odds of getting an additional attack by 40%.", 40, 2, R.drawable.boots, false, false));
    }
}
