package com.example.taskgame.view.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.SpecialMission;
import com.example.taskgame.view.viewmodels.AllianceViewModel;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AllianceFragment extends Fragment {

    private AllianceViewModel viewModel;
    private TextView tvAllianceName, tvLeader, tvMembers, tvSpecialMissionStatus;
    private Button btnStartSpecialMission;

    private String allianceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AllianceViewModel.class);
        viewModel.loadAllianceByUser(SessionManager.getInstance().getUserId());

        tvAllianceName = view.findViewById(R.id.tvAllianceName);
        tvLeader = view.findViewById(R.id.tvLeader);
        tvMembers = view.findViewById(R.id.tvMembers);
        tvSpecialMissionStatus = view.findViewById(R.id.tvSpecialMissionStatus);
        btnStartSpecialMission = view.findViewById(R.id.btnStartSpecialMission);


        // Observe alliance data
        viewModel.getAlliance().observe(getViewLifecycleOwner(), alliance -> {
            if (alliance == null) return;
            allianceId = alliance.getId();
            tvAllianceName.setText("Alliance ID: " + alliance.getId());
            tvLeader.setText("Leader: " + alliance.getLeaderId());
            tvMembers.setText("Members: " + alliance.getParticipantIds().size());

            if (alliance.isSpecialMissionActive()) {
                tvSpecialMissionStatus.setText("Special Mission: Active");
                btnStartSpecialMission.setVisibility(View.GONE);
                viewModel.loadSpecialMissionByAlliance(allianceId);
                viewModel.getSpecialMission().observe(getViewLifecycleOwner(), mission -> {
                    if (mission != null) {
                        updateSpecialMissionUI(mission);
                    } else{
                        System.out.println("⚠️ No special mission found for alliance frag: " + allianceId);
                    }
                });
            } else {
                tvSpecialMissionStatus.setText("Special Mission: Not Started");
                btnStartSpecialMission.setVisibility(View.VISIBLE);
            }
        });



        // Start mission button
        btnStartSpecialMission.setOnClickListener(v -> {
            viewModel.startSpecialMission("specialBoss_1"); // example boss
            Toast.makeText(requireContext(), "🚀 Special mission started!", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                viewModel.loadSpecialMissionByAlliance(allianceId);
            }, 1000);
        });
    }

    private void updateSpecialMissionUI(SpecialMission mission) {
        // Get UI references
        LinearLayout missionDetailsContainer = getView().findViewById(R.id.missionDetailsContainer);
        Button btnStartSpecialMission = getView().findViewById(R.id.btnStartSpecialMission);
        TextView tvBossHp = getView().findViewById(R.id.tvBossHp);
        ProgressBar pbBossHP = getView().findViewById(R.id.pbBossHP);
        TextView tvTotalDamage = getView().findViewById(R.id.tvTotalDamage);
        TextView tvYourDamage = getView().findViewById(R.id.tvYourDamage);
        LinearLayout memberDamageContainer = getView().findViewById(R.id.memberDamageContainer);

        if (mission == null) {
            // Hide everything if no mission
            missionDetailsContainer.setVisibility(View.GONE);
            btnStartSpecialMission.setVisibility(View.VISIBLE);
            return;
        }

        // Mission active — show details and hide start button
        missionDetailsContainer.setVisibility(View.VISIBLE);
        btnStartSpecialMission.setVisibility(View.GONE);

        int totalHp = (int) mission.getTotalHp();
        int remainingHp = (int) Math.max(mission.getCurrentHp(), 0);

        // Update boss HP progress
        pbBossHP.setMax(totalHp);
        pbBossHP.setProgress(remainingHp);
        tvBossHp.setText("HP: " + remainingHp + " / " + totalHp);

        // Calculate total and user-specific damage
        int totalDamage = mission.getTotalDamageDealt();
        tvTotalDamage.setText("Total Damage: " + totalDamage);

        String userId = SessionManager.getInstance().getUserId();
        int userDamage = mission.getUserDamage().getOrDefault(userId, 0);
        tvYourDamage.setText("Your Damage: " + userDamage);

        // Populate member contributions dynamically
        memberDamageContainer.removeAllViews();
        for (Map.Entry<String, Integer> entry : mission.getUserDamage().entrySet()) {
            String uid = entry.getKey();
            int damage = entry.getValue();

            TextView tv = new TextView(getContext());
            tv.setText(uid.equals(userId)
                    ? "⭐ You: " + damage + " dmg"
                    : uid + ": " + damage + " dmg");
            tv.setTextSize(14);
            memberDamageContainer.addView(tv);
        }
    }




}
