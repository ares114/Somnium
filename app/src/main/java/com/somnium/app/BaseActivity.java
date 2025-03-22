package com.somnium.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    protected BottomNavigationView bottomNav;
    private View homeContent, addContent, analysisContent, profileContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_nav);
        
        // Find all content containers
        homeContent = findViewById(R.id.home_content);
        addContent = findViewById(R.id.add_content);
        analysisContent = findViewById(R.id.analysis_content);
        profileContent = findViewById(R.id.profile_content);

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Log.d(TAG, "Navigation item selected: " + itemId);

                if (!(this instanceof HomepageActivity) && itemId == R.id.nav_home) {
                    navigateToActivity(HomepageActivity.class);
                    return true;
                } else if (!(this instanceof AddDreamActivity) && itemId == R.id.nav_add) {
                    navigateToActivity(AddDreamActivity.class);
                    return true;
                } else if (!(this instanceof AnalysisActivity) && itemId == R.id.nav_analysis) {
                    navigateToActivity(AnalysisActivity.class);
                    return true;
                } else if (!(this instanceof ProfileActivity) && itemId == R.id.nav_profile) {
                    navigateToActivity(ProfileActivity.class);
                    return true;
                }

                // Hide all content first
                if (homeContent != null) homeContent.setVisibility(View.GONE);
                if (addContent != null) addContent.setVisibility(View.GONE);
                if (analysisContent != null) analysisContent.setVisibility(View.GONE);
                if (profileContent != null) profileContent.setVisibility(View.GONE);

                // Show selected content
                if (itemId == R.id.nav_home) {
                    if (homeContent != null) homeContent.setVisibility(View.VISIBLE);
                    return true;
                } else if (itemId == R.id.nav_add) {
                    if (addContent != null) addContent.setVisibility(View.VISIBLE);
                    return true;
                } else if (itemId == R.id.nav_analysis) {
                    if (analysisContent != null) analysisContent.setVisibility(View.VISIBLE);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    if (profileContent != null) profileContent.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            });

            // Set initial selected item
            int selectedItemId;
            if (this instanceof HomepageActivity) {
                selectedItemId = R.id.nav_home;
                if (homeContent != null) homeContent.setVisibility(View.VISIBLE);
            } else if (this instanceof AddDreamActivity) {
                selectedItemId = R.id.nav_add;
                if (addContent != null) addContent.setVisibility(View.VISIBLE);
            } else if (this instanceof AnalysisActivity) {
                selectedItemId = R.id.nav_analysis;
                if (analysisContent != null) analysisContent.setVisibility(View.VISIBLE);
            } else if (this instanceof ProfileActivity) {
                selectedItemId = R.id.nav_profile;
                if (profileContent != null) profileContent.setVisibility(View.VISIBLE);
            } else {
                selectedItemId = R.id.nav_home;
                if (homeContent != null) homeContent.setVisibility(View.VISIBLE);
            }
            bottomNav.setSelectedItemId(selectedItemId);
        }
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Log.d(TAG, "Navigating to: " + targetActivity.getSimpleName());
        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        if (!(this instanceof HomepageActivity)) {
            finish();
        }
        overridePendingTransition(0, 0);
    }
} 