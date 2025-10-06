package com.example.taskgame.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskgame.R;
import com.example.taskgame.data.repositories.SpecialEquipmentRepository;
import com.example.taskgame.data.repositories.UserEquipmentRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Boss;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.SpecialEquipment;
import com.example.taskgame.domain.models.UserEquipment;
import com.example.taskgame.view.viewmodels.AllianceViewModel;
import com.example.taskgame.view.viewmodels.BossFightViewModel;

import java.util.ArrayList;
import java.util.List;

public class BossFightFragment extends Fragment {

    private BossFightViewModel viewModel;

    private AllianceViewModel allianceViewModel;
    private ProgressBar hpBar;
    private TextView tvBossName, tvStatus, tvHp, tvAttacks, tvBasePP, tvEquipmentPP, tvTotalPP;
    private Button btnAttack, btnViewRewards;

    private Button btnUseEquipment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_boss_fight, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvBossName = view.findViewById(R.id.tvBossName);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvBasePP = view.findViewById(R.id.tvBasePP);
        tvEquipmentPP = view.findViewById(R.id.tvEquipmentPP);
        tvTotalPP = view.findViewById(R.id.tvTotalPP);
        tvHp = view.findViewById(R.id.tvHp);
        tvAttacks = view.findViewById(R.id.tvAttacks);
        hpBar = view.findViewById(R.id.hpBar);
        btnAttack = view.findViewById(R.id.btnAttack);
        btnViewRewards = view.findViewById(R.id.btnViewRewards);


        viewModel = new ViewModelProvider(this).get(BossFightViewModel.class);

        String userId = SessionManager.getInstance().getUserId();
        int level = SessionManager.getInstance().getUserLevel();

        viewModel.loadBoss(userId, level);

        viewModel.getBoss().observe(getViewLifecycleOwner(), boss -> {
            if (boss == null) return;

            tvBossName.setText(boss.getName());
            tvStatus.setText("Status: " + boss.getStatus());
            //tvHp.setText("HP: " + (int)boss.getHp());
            tvHp.setText("HP: " + (int) boss.getHp() + " / " + (int) boss.getTotalHp());
            tvAttacks.setText("Attacks left: " + boss.getAvailableAttacks());

            hpBar.setMax((int)boss.getTotalHp()); // možeš dodati i maxHP ako želiš
            hpBar.setProgress((int)boss.getHp());

            btnAttack.setEnabled(boss.canAttack());

            // 🔹 Get PP values from Session
            int basePP = SessionManager.getInstance().getUserPP();
            int equipmentPP = SessionManager.getInstance().getUserEquipmentPP();
            int totalPP = basePP + equipmentPP;

            tvBasePP.setText("Base PP: " + basePP);
            tvEquipmentPP.setText("Equipment PP: " + equipmentPP);
            tvTotalPP.setText("Total PP: " + totalPP);
        });

        btnViewRewards.setOnClickListener(v -> {
            Boss boss = viewModel.getBoss().getValue();
            if (boss == null) {
                Toast.makeText(requireContext(), "No boss loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            // Possible equipment list
            String[] possibleEquipment = {
                    "Golden Crown (Clothing, +10PP, +10% coins)",
                    "Shadow Cloak (Clothing, +15PP, +5% coins)",
                    "Sword of Flames (Weapon, +20PP, +15% coins)",
                    "War Hammer (Weapon, +25PP, +20% coins)"
            };

            String message = "💰 Coins: " + boss.getCoins() +
                    "\n\n🎁 Possible equipment rewards:\n" +
                    String.join("\n", possibleEquipment);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Boss Rewards")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });


        btnAttack.setOnClickListener(v -> {
            int totalUserPP = SessionManager.getInstance().getUserPP() + SessionManager.getInstance().getUserEquipmentPP();
            boolean hit = viewModel.attackBoss(totalUserPP);

            if (hit) {
                Toast.makeText(requireContext(), "🎯 Hit!", Toast.LENGTH_SHORT).show();
                specialMissionRegularHit();

                if (viewModel.isFightOver()) {
                    Boss boss = viewModel.getBoss().getValue();
                    int rewardCoins = 0;
                    if (viewModel.getBoss().getValue() != null && viewModel.getBoss().getValue().isDefeated()) {
                        Toast.makeText(requireContext(), "🏆 Boss defeated!", Toast.LENGTH_LONG).show();
                        rewardCoins = boss.calculateFullReward();
                        // later: call boss reward function
                    } else {
                        Toast.makeText(requireContext(), "⚔️ Fight ended. Boss survived!", Toast.LENGTH_LONG).show();
                        viewModel.setBossPending();
                        if (boss.getHp() <= boss.getTotalHp() / 2.0) {
                            rewardCoins = boss.calculateHalfReward();
                        } else {
                            rewardCoins = boss.calculateNoReward();
                        }
                    }
                    btnAttack.setEnabled(false); // disable further attacks


                    if(!boss.isRewardGiven()){
                        this.coinsAfterFight(rewardCoins);
                    }





                    if (boss.rollForEquipmentDrop() && !boss.isRewardGiven()) {
                        String eqId = boss.getRandomEquipmentId();
                        System.out.println("🎁 Player won equipment: " + eqId);
                        Toast.makeText(requireContext(), "🎁 Player won equipment: " + eqId, Toast.LENGTH_LONG).show();

                        // Add it to user
                        UserEquipmentRepository userEqRepo = new UserEquipmentRepository();
                        userEqRepo.add(
                                SessionManager.getInstance().getUserId(),
                                new UserEquipment(SessionManager.getInstance().getUserId(), eqId, false), // default inactive
                                new UserEquipmentRepository.VoidCallback() {
                                    @Override public void onSuccess() {
                                        System.out.println("✅ Equipment saved to user: " + eqId);
                                    }
                                    @Override public void onFailure(Exception e) {
                                        System.err.println("❌ Failed to save equipment: " + e.getMessage());
                                    }
                                }
                        );
                    }

                }
            } else {
                Toast.makeText(requireContext(), "💨 Miss!", Toast.LENGTH_SHORT).show();
            }
        });


        btnUseEquipment = view.findViewById(R.id.btnUseEquipment);
        btnUseEquipment.setOnClickListener(v -> {
            UserEquipmentRepository userRepo = new UserEquipmentRepository();
            SpecialEquipmentRepository eqRepo = new SpecialEquipmentRepository();

            userRepo.getAll(userId, new UserEquipmentRepository.GetAllCallback() {
                @Override
                public void onSuccess(List<UserEquipment> list) {
                    List<SpecialEquipment> equippedList = new ArrayList<>();

                    for (UserEquipment ue : list) {
                        eqRepo.getById(ue.getEquipmentId(), new SpecialEquipmentRepository.GetOneCallback() {
                            @Override
                            public void onSuccess(SpecialEquipment eq) {
                                if (eq != null) {
                                    equippedList.add(eq);
                                    // Once all are fetched, you can show dialog
                                    if (equippedList.size() == list.size()) {
                                        showEquipmentDialog(equippedList);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Failed to fetch equipment", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to load equipment", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void specialMissionRegularHit(){
        // Step 1️⃣  Load the alliance for the current user first (this populates LiveData)
        allianceViewModel = new ViewModelProvider(this).get(AllianceViewModel.class);
        allianceViewModel.loadAllianceByUser(SessionManager.getInstance().getUserId());

// Step 2️⃣  Observe allianceLiveData and react once loaded
        allianceViewModel.getAlliance().observe(getViewLifecycleOwner(), alliance -> {
            if (alliance != null) {
                System.out.println("✅ Alliance loaded for user: " + alliance.getId());

                // Step 3️⃣  Once alliance is available, you can safely call special mission actions
                allianceViewModel.applySpecialMissionAction(
                        SessionManager.getInstance().getUserId(),
                        "regularHit",   // or "task", "storePurchase", etc.
                        null            // optional difficulty if needed
                );
            }
        });
    }



    private void coinsAfterFight(int rewardCoins){
        double bonusMultiplier = 1.0 + SessionManager.getInstance().getBonusCoinPercent();
        int finalCoins = (int) Math.round(rewardCoins * bonusMultiplier);


        // 🔹 Call repository function
        UserRepository userRepo = new UserRepository();
        userRepo.giveBossRewards(SessionManager.getInstance().getUserId(), finalCoins);

        Toast.makeText(requireContext(),
                "💰 You earned " + finalCoins + " coins!",
                Toast.LENGTH_LONG).show();
    }
    private void showEquipmentDialog(List<SpecialEquipment> eqList) {
        String[] names = new String[eqList.size()];
        for (int i = 0; i < eqList.size(); i++) {
            names[i] = eqList.get(i).getName() + " (" + eqList.get(i).getDescription() + ")";
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Choose equipment to toggle")
                .setItems(names, (dialog, which) -> {
                    SpecialEquipment chosen = eqList.get(which);
                    String chosenId = chosen.getId();

                    UserEquipmentRepository userEqRepo = new UserEquipmentRepository();
                    userEqRepo.toggleActive(
                            SessionManager.getInstance().getUserId(),
                            chosenId,
                            new UserEquipmentRepository.ToggleCallback() {
                                @Override
                                public void onSuccess(boolean newStatus) {
                                    Toast.makeText(getContext(),
                                            chosen.getName() + " set to " + (newStatus ? "Active" : "Inactive"),
                                            Toast.LENGTH_SHORT).show();

                                    // 🔹 Recalculate bonuses after status change
                                    //userEqRepo.calculateActiveEquipmentBonuses();

                                    userEqRepo.calculateActiveEquipmentBonuses(() -> {
                                        int basePP = SessionManager.getInstance().getUserPP();
                                        int equipmentPP = SessionManager.getInstance().getUserEquipmentPP();
                                        int totalPP = basePP + equipmentPP;

                                        tvBasePP.setText("Base PP: " + basePP);
                                        tvEquipmentPP.setText("Equipment PP: " + equipmentPP);
                                        tvTotalPP.setText("Total PP: " + totalPP);
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(), "Failed to toggle equipment", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                })
                .show();
    }
}
