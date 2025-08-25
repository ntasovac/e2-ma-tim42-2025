package com.example.taskgame.view.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.taskgame.R;

import com.example.taskgame.databinding.ActivityRegistrationBinding;
import com.example.taskgame.view.viewmodels.RegistrationViewModel;

public class RegistrationActivity extends AppCompatActivity {

    private ActivityRegistrationBinding binding;
    private RegistrationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

        ImageView gojo = findViewById(R.id.gojo);
        ImageView eren = findViewById(R.id.eren);
        ImageView mikasa = findViewById(R.id.mikasa);
        ImageView kaneki = findViewById(R.id.kaneki);
        ImageView hinata = findViewById(R.id.hinata);
        final int[] selectedAvatar = {R.drawable.gojo};

        View.OnClickListener avatarClickListener = v -> {

            eren.setSelected(false);
            gojo.setSelected(false);
            mikasa.setSelected(false);
            kaneki.setSelected(false);
            hinata.setSelected(false);

            v.setSelected(true);

            int selectedResId;
            if (v.getId() == R.id.gojo) {
                selectedResId = R.drawable.gojo;
            } else if(v.getId()== R.id.eren){
                selectedResId = R.drawable.eren;
            } else if (v.getId() == R.id.mikasa) {
                selectedResId = R.drawable.mikasa;
            } else if (v.getId() == R.id.kaneki) {
                selectedResId = R.drawable.kaneki;
            } else if (v.getId() == R.id.hinata) {
                selectedResId = R.drawable.hinata;
            } else {
                selectedResId = R.drawable.gojo;
            }

            viewModel.setSelectedAvatar(selectedResId);
        };

        gojo.setOnClickListener(avatarClickListener);
        eren.setOnClickListener(avatarClickListener);
        mikasa.setOnClickListener(avatarClickListener);
        kaneki.setOnClickListener(avatarClickListener);
        hinata.setOnClickListener(avatarClickListener);

        viewModel.getRegistrationSuccess().observe(this, success -> {
            if(success != null && success) {
                Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.registerButton.setOnClickListener(v -> {
            viewModel.setUsername(binding.usernameInput.getText().toString().trim());
            viewModel.setEmail(binding.emailInput.getText().toString().trim());
            viewModel.setPassword(binding.passwordInput.getText().toString());
            viewModel.setConfirmPassword(binding.confirmPasswordInput.getText().toString());

            viewModel.register();
        });

        viewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                Toast.makeText(RegistrationActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}