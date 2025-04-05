package com.somnium.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.somnium.app.R;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private static final long SPLASH_DISPLAY_TIME = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_splash_screen);
            Log.d(TAG, "Splash screen layout set");
            
            // Find the logo ImageView
            ImageView logo = findViewById(R.id.logo);
            if (logo == null) {
                Log.e(TAG, "Logo ImageView not found in layout");
                proceedToLogin();
                return;
            }
            
            Log.d(TAG, "Logo found, setting up animation");

            // Create a fade-in animation
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(1000);
            fadeIn.setFillAfter(true);

            // Set animation listener
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Not needed
                    Log.d(TAG, "Animation started");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // After the animation ends, wait a bit more then go to Login
                    Log.d(TAG, "Animation ended, scheduling login transition");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        proceedToLogin();
                    }, SPLASH_DISPLAY_TIME - 1000); // Subtract animation time
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Not needed
                }
            });

            // Start the animation
            logo.startAnimation(fadeIn);
            Log.d(TAG, "Animation started on logo");
            
        } catch (Exception e) {
            Log.e(TAG, "Error in splash screen: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show();
            proceedToLogin();
        }
    }
    
    private void proceedToLogin() {
        try {
            Log.d(TAG, "Proceeding to login screen");
            // Navigate to the login activity after the splash screen
            Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            
            // Close this activity
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to login: " + e.getMessage(), e);
            Toast.makeText(this, "Error starting app", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
} 