package com.somnium.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private EditText fullnameInput;
    private EditText usernameInput;
    private Button saveButton;
    private ImageButton backButton;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        
        // Load current user data
        loadUserData();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.edit_profile_image);
        fullnameInput = findViewById(R.id.fullname_input);
        usernameInput = findViewById(R.id.username_input);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);
    }

    private void loadUserData() {
        mDatabase.child("users").child(currentUser.getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullname = dataSnapshot.child("fullname").getValue(String.class);
                        String username = dataSnapshot.child("username").getValue(String.class);
                        
                        if (fullname != null) {
                            fullnameInput.setText(fullname);
                        }
                        
                        if (username != null) {
                            usernameInput.setText(username);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(EditProfileActivity.this, 
                        "Failed to load profile data", 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveChanges());
        backButton.setOnClickListener(v -> finish());
    }

    private void saveChanges() {
        String newFullname = fullnameInput.getText().toString().trim();
        String newUsername = usernameInput.getText().toString().trim();
        
        if (newFullname.isEmpty()) {
            fullnameInput.setError("Full name cannot be empty");
            return;
        }
        
        if (newUsername.isEmpty()) {
            usernameInput.setError("Username cannot be empty");
            return;
        }

        // Update user data
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullname", newFullname);
        updates.put("username", newUsername);

        mDatabase.child("users").child(currentUser.getUid())
            .updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(EditProfileActivity.this, 
                    "Profile updated successfully", 
                    Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(EditProfileActivity.this,
                    "Failed to update profile: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show()
            );
    }
} 