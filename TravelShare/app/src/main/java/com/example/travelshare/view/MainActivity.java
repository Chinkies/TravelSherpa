package com.example.travelshare.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.model.User;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private boolean isUserLogged = false;
    private User currentUserProfile = null;

    // Launcher pour la demande de permission de notifications (Android 13+)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Les notifications sont désactivées. Vous ne recevrez pas d'alertes en temps réel.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.headBar);
        setSupportActionBar(toolbar);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_bar);

        // On retire notificationsFragment des destinations de premier niveau pour avoir la flèche de retour
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.feedFragment, R.id.searchFragment, R.id.groupsFragment, 
                R.id.publishFragment, R.id.preferenceFragment)
                .build();

        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        authViewModel.getUser().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                userViewModel.loadCurrentUser(firebaseUser.getUid());
            } else {
                isUserLogged = false;
                currentUserProfile = null;
                userViewModel.clearUserData();
                invalidateOptionsMenu();
            }
        });

        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                isUserLogged = true;
                currentUserProfile = user;
                invalidateOptionsMenu();
            }
        });

        // Détection d'un compte Auth sans profil Firestore (cas de clean DB)
        userViewModel.getErrorMessage().observe(this, error -> {
            if ("Utilisateur introuvable".equals(error)) {
                authViewModel.logout();
                userViewModel.clearError();
                Toast.makeText(this, "Profil introuvable, veuillez vous reconnecter", Toast.LENGTH_LONG).show();
            }
        });

        askNotificationPermission();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem loginItem = menu.findItem(R.id.header_login);
        MenuItem profileItem = menu.findItem(R.id.header_profile);

        if (isUserLogged && currentUserProfile != null) {
            loginItem.setVisible(false);
            profileItem.setVisible(true);

            View actionView = profileItem.getActionView();
            if (actionView != null) {
                ImageView profileImage = actionView.findViewById(R.id.toolbar_profile_image);
                if (profileImage != null) {
                    Glide.with(this)
                            .load(currentUserProfile.getProfilePictureUrl())
                            .placeholder(R.drawable.default_user)
                            .error(R.drawable.default_user)
                            .circleCrop()
                            .into(profileImage);
                }
                actionView.setOnClickListener(v -> onOptionsItemSelected(profileItem));
            }
        } else {
            loginItem.setVisible(true);
            profileItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.header_login) {
            navController.navigate(R.id.authFragment);
            return true;
        } else if (itemId == R.id.header_profile) {
            navController.navigate(R.id.profileFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
