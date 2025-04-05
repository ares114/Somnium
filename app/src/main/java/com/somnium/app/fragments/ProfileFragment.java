package com.somnium.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.somnium.app.EditProfileActivity;
import com.somnium.app.LoginActivity;
import com.somnium.app.R;
import com.somnium.app.viewmodels.DreamViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int EDIT_PROFILE_REQUEST = 1001;
    
    private TextView tvName, tvUsername, tvEmail, tvDreamsRecorded, tvAccountCreated;
    private Button btnEditProfile, btnLogout;
    private CircleImageView ivProfilePicture;
    private FirebaseAuth mAuth;
    private DreamViewModel dreamViewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize ViewModel
        dreamViewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);

        // Initialize views
        tvName = view.findViewById(R.id.tv_profile_name);
        tvUsername = view.findViewById(R.id.tv_profile_username);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvDreamsRecorded = view.findViewById(R.id.tv_dreams_recorded);
        tvAccountCreated = view.findViewById(R.id.tv_account_created);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);

        // Set up logout button
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // Set up edit profile button
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        // Set up dream count observer
        setupDreamCountObserver();
        
        // Load user data
        loadUserData();
    }
    
    private void setupDreamCountObserver() {
        dreamViewModel.getDreamsList().observe(getViewLifecycleOwner(), dreams -> {
            if (dreams != null) {
                tvDreamsRecorded.setText(String.valueOf(dreams.size()));
            } else {
                tvDreamsRecorded.setText("0");
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST) {
            // Refresh profile data when returning from edit screen
            loadUserData();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadUserData();
        loadDreams();
    }
    
    private void loadDreams() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Load user's dreams to update the count
            dreamViewModel.loadUserDreams(currentUser.getUid());
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Display user info
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // If displayName contains username in format "Name|username"
                if (displayName.contains("|")) {
                    String[] parts = displayName.split("\\|");
                    tvName.setText(parts[0]);
                    tvUsername.setText("@" + parts[1]);
                } else {
                    tvName.setText(displayName);
                    tvUsername.setText("@" + displayName.toLowerCase().replace(" ", ""));
                }
            } else {
                tvName.setText(R.string.app_name);
                tvUsername.setText("@user");
            }

            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                tvEmail.setText(email);
            }

            // Dreams count is now handled by the observer

            // Show account creation time
            if (currentUser.getMetadata() != null && currentUser.getMetadata().getCreationTimestamp() > 0) {
                long creationTimestamp = currentUser.getMetadata().getCreationTimestamp();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String formattedDate = sdf.format(new Date(creationTimestamp));
                tvAccountCreated.setText(formattedDate);
            } else {
                tvAccountCreated.setText("N/A");
            }

            // Load profile picture
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfilePicture);
            }
        }
    }
} 