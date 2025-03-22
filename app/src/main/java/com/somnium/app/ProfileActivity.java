package com.somnium.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.annotation.NonNull;

public class ProfileActivity extends BaseActivity {
    private CircleImageView profileImage;
    private TextView profileName;
    private TextView profileUsername;
    private TextView profileEmail;
    private TextView dreamsCount;
    private TextView streakCount;
    private Button editProfileButton;
    private Button logoutButton;
    
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            goToLogin();
            return;
        }

        userRef = database.getReference().child("users").child(currentUser.getUid());
        userRef.keepSynced(true);

        initializeViews();
        setupBottomNavigation();
        setupClickListeners();
        attachUserListener();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileUsername = findViewById(R.id.profile_username);
        profileEmail = findViewById(R.id.profile_email);
        dreamsCount = findViewById(R.id.dreams_count);
        streakCount = findViewById(R.id.streak_count);
        editProfileButton = findViewById(R.id.edit_profile_button);
        logoutButton = findViewById(R.id.logout_button);

        if (currentUser != null) {
            profileEmail.setText(currentUser.getEmail());
        }
    }

    private void attachUserListener() {
        if (userListener != null) {
            userRef.removeEventListener(userListener);
        }

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    System.out.println("No data exists for user");
                    setDefaultValues();
                    return;
                }

                try {
                    String fullname = dataSnapshot.child("fullname").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    Long dreams = dataSnapshot.child("totalDreams").getValue(Long.class);
                    Long streak = dataSnapshot.child("currentStreak").getValue(Long.class);

                    runOnUiThread(() -> {
                        profileName.setText(fullname != null ? fullname : "No name set");
                        profileUsername.setText(username != null ? "@" + username : "@user");
                        dreamsCount.setText(String.valueOf(dreams != null ? dreams : 0));
                        streakCount.setText(String.valueOf(streak != null ? streak : 0));
                    });

                } catch (Exception e) {
                    System.out.println("Error loading data: " + e.getMessage());
                    setDefaultValues();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Database error: " + error.getMessage());
                setDefaultValues();
            }
        };

        userRef.addValueEventListener(userListener);
    }

    private void setDefaultValues() {
        runOnUiThread(() -> {
            profileName.setText("No name set");
            profileUsername.setText("@user");
            dreamsCount.setText("0");
            streakCount.setText("0");
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            goToLogin();
        });
    }
}