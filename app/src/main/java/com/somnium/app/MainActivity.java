package com.somnium.app;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.somnium.app.fragments.HomeFragment;
import com.somnium.app.fragments.JournalFragment;
import com.somnium.app.fragments.ProfileFragment;
import com.somnium.app.fragments.StatisticsFragment;

public class MainActivity extends BaseActivity implements NavigationBarView.OnItemSelectedListener {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);
        
        // Set default fragment to Home
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }
    
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_journal) {
            fragment = new JournalFragment();
        } else if (itemId == R.id.nav_stats) {
            fragment = new StatisticsFragment();
        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
        }
        
        return loadFragment(fragment);
    }
}