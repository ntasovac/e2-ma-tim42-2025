package com.example.taskgame.view.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.taskgame.R;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.databinding.ActivityHomeBinding;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.domain.models.User;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_home_base), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navView = binding.navView;

        Toolbar toolbar = binding.activityHomeBase.toolbar;
        setSupportActionBar(toolbar);

        TextView tvUserInfo = toolbar.findViewById(R.id.tvUserInfo);

        UserRepository userRepo = new UserRepository();
        String loggedUserId = "1759088611"; // 👈 Replace with real logged-in user ID

        if(SessionManager.getInstance().getUserId() != null){
            loggedUserId = SessionManager.getInstance().getUserId();
        }
        userRepo.getUserById(loggedUserId, new UserRepository.GetUserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    tvUserInfo.setText(user.getUsername() + " • " + user.getId());
                    SessionManager.getInstance().setUserData(user);
                    tvUserInfo.setOnClickListener(v -> {
                        SessionManager session = SessionManager.getInstance();

                        String message = "XP: " + session.getUserXP() +
                                "\nPP: " + session.getUserPP() +
                                "\nLevel: " + session.getUserLevel() +
                                "\nCoins: " + session.getCoins();

                        new AlertDialog.Builder(HomeActivity.this)
                                .setTitle("User Info")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                tvUserInfo.setText("Error loading user");
            }
        });


        // IMPORTANT: use the new NavHostFragment ID from activity_home_content.xml
        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);

        // Declare top-level destinations (hamburger instead of back arrow)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.fragmentHome,
                R.id.fragmentBossFight,
                R.id.fragmentCategory
        ).setOpenableLayout(drawer).build();

        // Wire toolbar + drawer + menu to Navigation
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}

/*
package com.example.taskgame.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.taskgame.R;
import com.example.taskgame.databinding.ActivityHomeBinding;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.view.viewmodels.HomeViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_home_base), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navView = binding.navView;
        Toolbar toolbar = binding.activityHomeBase.toolbar;
        setSupportActionBar(toolbar);

        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.fragmentHome,
                R.id.fragmentCategory,
                R.id.fragmentProfile,
                R.id.fragmentEquipmentShop,
                R.id.fragmentLevels,
                R.id.fragmentFriends
        ).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        if (getIntent() != null) {
            String target = getIntent().getStringExtra("openFragment");
            if ("HomeFragment".equals(target)) {
                navController.navigate(R.id.fragmentHome);
            }
        }

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(this, "You have been logged out", Toast.LENGTH_SHORT).show();
                return true;
            }else if(id == R.id.fragmentEquipmentShop) {
                int bossLevel = viewModel.getBossLevel();
                if (bossLevel == 1) {
                    Toast.makeText(this, "Defeat level 1 boss to unlock", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putInt("bossLevel", bossLevel);
                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.fragmentHome, false)
                            .setLaunchSingleTop(true)
                            .build();

                    navController.navigate(R.id.fragmentEquipmentShop, bundle, navOptions);
                    return true;
                }
            } else {
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}

 */