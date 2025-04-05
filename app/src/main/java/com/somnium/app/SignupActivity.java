package com.somnium.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends BaseActivity {

    private TextInputEditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etFullName = findViewById(R.id.et_fullname);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);

        // Set click listeners
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        final String fullName = etFullName.getText().toString().trim();
        final String username = etUsername.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate form
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.fields_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.passwords_not_match, Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        btnSignup.setEnabled(false);

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        btnSignup.setEnabled(true);
                        
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Set display name (using fullName instead of username)
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();
                                
                                user.updateProfile(profileUpdates);
                                
                                // Send verification email
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SignupActivity.this, 
                                                            R.string.email_verification_sent, 
                                                            Toast.LENGTH_LONG).show();
                                                    
                                                    // Sign out and redirect to verification page
                                                    mAuth.signOut();
                                                    startActivity(new Intent(SignupActivity.this, 
                                                            VerificationActivity.class));
                                                    finish();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(SignupActivity.this, R.string.signup_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
} 