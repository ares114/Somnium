package com.somnium.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {

    private EditText fullnameInput, usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button signupButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        fullnameInput = findViewById(R.id.fullname_input);
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        signupButton = findViewById(R.id.signup_button);
        TextView signinRedirectText = findViewById(R.id.signin_redirect);

        mAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(v -> {
            String fullname = fullnameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            if (fullname.isEmpty()) {
                fullnameInput.setError("Full name is required");
                return;
            }

            if (username.isEmpty()) {
                usernameInput.setError("Username is required");
                return;
            }

            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegistrationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(fullname, username, email, password);
        });

        signinRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser(String fullname, String username, String email, String password) {
        // Pass all user data to verification
        Intent intent = new Intent(RegistrationActivity.this, EmailVerificationActivity.class);
        intent.putExtra("fullname", fullname);
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
        finish();
    }
}