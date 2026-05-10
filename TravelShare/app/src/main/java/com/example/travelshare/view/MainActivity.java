package com.example.travelshare.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private boolean isUserLogged = false;

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
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.feedFragment, R.id.searchFragment, R.id.groupsFragment, R.id.publishFragment)
                .build();

        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(bottomNav, navController);

        bottomNav.setOnItemSelectedListener(item -> {
            navController.popBackStack(item.getItemId(), false);
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        bottomNav.setOnItemReselectedListener(item -> {
            navController.popBackStack(item.getItemId(), false);
        });

        authViewModel.getUser().observe(this, firebaseUser -> {
            isUserLogged = (firebaseUser != null);
            if (firebaseUser != null) {
                userViewModel.loadUser(firebaseUser.getUid());
            }

            invalidateOptionsMenu();
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem loginItem = menu.findItem(R.id.header_login);
        MenuItem profileItem = menu.findItem(R.id.header_profile);

        if (isUserLogged) {
            loginItem.setVisible(false);
            profileItem.setVisible(true);

            View actionView = profileItem.getActionView();
            ImageView profileImage = actionView.findViewById(R.id.toolbar_profile_image);

            userViewModel.getSelectedUser().observe(this, user -> {
                if (user != null && profileImage != null) {
                    Glide.with(this)
                            .load(user.getProfilePictureUrl())
                            .placeholder(R.drawable.default_user)
                            .error(R.drawable.default_user)
                            .circleCrop()
                            .into(profileImage);
                }
            });
            actionView.setOnClickListener(v -> onOptionsItemSelected(profileItem));

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
            int startDestination = navController.getGraph().getStartDestinationId();

            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(startDestination, false)
                    .build();

            navController.navigate(R.id.profileFragment, null, navOptions);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}