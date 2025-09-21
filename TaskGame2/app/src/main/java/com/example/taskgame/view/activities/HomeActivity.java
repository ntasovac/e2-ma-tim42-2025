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
                R.id.fragmentLevels
        ).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

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
                    navController.navigate(R.id.fragmentEquipmentShop, bundle);
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
