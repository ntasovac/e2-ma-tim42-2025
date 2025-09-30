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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.view.viewmodels.BossFightViewModel;

public class BossFightFragment extends Fragment {

    private BossFightViewModel viewModel;
    private ProgressBar hpBar;
    private TextView tvBossName, tvStatus, tvHp, tvAttacks;
    private Button btnAttack;

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
        tvHp = view.findViewById(R.id.tvHp);
        tvAttacks = view.findViewById(R.id.tvAttacks);
        hpBar = view.findViewById(R.id.hpBar);
        btnAttack = view.findViewById(R.id.btnAttack);

        viewModel = new ViewModelProvider(this).get(BossFightViewModel.class);

        String userId = SessionManager.getInstance().getUserId();
        int level = SessionManager.getInstance().getUserLevel();

        viewModel.loadBoss(userId, level);

        viewModel.getBoss().observe(getViewLifecycleOwner(), boss -> {
            if (boss == null) return;

            tvBossName.setText(boss.getName());
            tvStatus.setText("Status: " + boss.getStatus());
            tvHp.setText("HP: " + (int)boss.getHp());
            tvAttacks.setText("Attacks left: " + boss.getAvailableAttacks());

            hpBar.setMax((int)boss.getHp()); // možeš dodati i maxHP ako želiš
            hpBar.setProgress((int)boss.getHp());

            btnAttack.setEnabled(boss.canAttack());
        });

        btnAttack.setOnClickListener(v -> {
            int userPP = SessionManager.getInstance().getUserPP();
            boolean hit = viewModel.attackBoss(userPP);

            if (hit) {
                Toast.makeText(requireContext(), "🎯 Hit!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "💨 Miss!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
