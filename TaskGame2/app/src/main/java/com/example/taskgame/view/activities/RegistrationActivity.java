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

        gojo.setTag(1);
        eren.setTag(2);
        mikasa.setTag(3);
        kaneki.setTag(4);
        hinata.setTag(5);

        final int[] selectedAvatar = {1};

        View.OnClickListener avatarClickListener = v -> {

            eren.setSelected(false);
            gojo.setSelected(false);
            mikasa.setSelected(false);
            kaneki.setSelected(false);
            hinata.setSelected(false);

            v.setSelected(true);

            int selectedValue = (int) v.getTag();
            selectedAvatar[0] = selectedValue;

            viewModel.setSelectedAvatar(selectedValue);
        };

        gojo.setOnClickListener(avatarClickListener);
        eren.setOnClickListener(avatarClickListener);
        mikasa.setOnClickListener(avatarClickListener);
        kaneki.setOnClickListener(avatarClickListener);
        hinata.setOnClickListener(avatarClickListener);

        viewModel.getRegistrationSuccess().observe(this, success -> {
            if(success != null && success) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
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

        // 🔹 Log In button (direct user selection: 1, 2, or 3)
        binding.loginButton.setOnClickListener(v -> {
            String input = binding.userNumberInput.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(this, "Enter a user number (1, 2 or 3)", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int userNumber = Integer.parseInt(input);

                if (userNumber < 1 || userNumber > 3) {
                    Toast.makeText(this, "Only users 1, 2, or 3 are allowed", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Now really log in via ViewModel
                viewModel.login(userNumber);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                Toast.makeText(RegistrationActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}