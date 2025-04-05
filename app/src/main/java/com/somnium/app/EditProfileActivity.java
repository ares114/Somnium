package com.somnium.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputEditText etFullName, etUsername;
    private Button btnSaveChanges;
    private CircleImageView ivProfilePicture;
    private TextView tvChangePhoto;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    
    private Uri imageUri = null;
    private String currentPhotoUrl = null;
    private boolean cloudinaryInitialized = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloudinary
        initCloudinary();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // Initialize views
        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        tvChangePhoto = findViewById(R.id.tv_change_photo);
        
        // Set up progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);
        
        // Set up profile picture click listener
        ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
        
        tvChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
        
        // Load current user data
        loadUserData();
        
        // Set button click listener
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
    }
    
    private void initCloudinary() {
        try {
            if (!cloudinaryInitialized) {
                Map<String, String> config = new HashMap<>();
                config.put("cloud_name", "dzinqbeef");
                config.put("api_key", "345231684349233");
                config.put("api_secret", "j3fNLLdZc9fsYbJzvMkpbI0wIU4");
                MediaManager.init(this, config);
                cloudinaryInitialized = true;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to initialize image upload service", Toast.LENGTH_LONG).show();
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            
            // Display the selected image
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfilePicture);
        }
    }
    
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // If displayName contains username in format "Name|username"
                if (displayName.contains("|")) {
                    String[] parts = displayName.split("\\|");
                    etFullName.setText(parts[0]);
                    etUsername.setText(parts[1]);
                } else {
                    etFullName.setText(displayName);
                    etUsername.setText(displayName.toLowerCase().replace(" ", ""));
                }
            }
            
            // Load profile picture if exists
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                currentPhotoUrl = photoUrl.toString();
                Glide.with(this)
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfilePicture);
            }
        }
    }
    
    private void saveChanges() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Remove spaces and special characters from username
        username = username.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        
        if (username.isEmpty()) {
            Toast.makeText(this, "Username contains invalid characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Store as "FullName|username" in displayName
        final String combinedName = fullName + "|" + username;
        
        // Show progress
        progressDialog.show();
        
        // If new image is selected, upload it first
        if (imageUri != null) {
            uploadImageToCloudinary(combinedName);
        } else {
            updateUserProfile(combinedName, currentPhotoUrl);
        }
    }
    
    private void uploadImageToCloudinary(final String combinedName) {
        String requestId = MediaManager.get().upload(imageUri)
            .option("public_id", "profile_" + UUID.randomUUID().toString())
            .option("folder", "somnium_profile_pictures")
            .callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    // Upload started
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    // Upload progress
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    String secureUrl = (String) resultData.get("secure_url");
                    updateUserProfile(combinedName, secureUrl);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, 
                            "Failed to upload image: " + error.getDescription(), 
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    // Upload rescheduled
                }
            })
            .dispatch();
    }
    
    private void updateUserProfile(String displayName, String photoUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName);
            
            if (photoUrl != null) {
                profileUpdatesBuilder.setPhotoUri(Uri.parse(photoUrl));
            }
            
            UserProfileChangeRequest profileUpdates = profileUpdatesBuilder.build();
            
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 