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
