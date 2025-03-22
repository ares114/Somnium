package com.somnium.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.somnium.app.utils.EmailVerificationHelper;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;

public class EmailVerificationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText verificationCodeInput;
    private Button verifyButton;
    private TextView resendCodeText;
    private EmailVerificationHelper verificationHelper;
    private String verificationCode;
    private static final int EMAIL_TIMEOUT_SECONDS = 30;
    private String email;
    private String password;
    private String fullname;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        // Get registration details
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        fullname = getIntent().getStringExtra("fullname");
        username = getIntent().getStringExtra("username");

        if (email == null || password == null || fullname == null || username == null) {
            Toast.makeText(this, "Registration details missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        verificationHelper = new EmailVerificationHelper();

        // Initialize views
        verificationCodeInput = findViewById(R.id.verification_code_input);
        verifyButton = findViewById(R.id.verify_button);
        resendCodeText = findViewById(R.id.resend_code);

        // Send verification code when activity starts
        sendVerificationCode();

        verifyButton.setOnClickListener(v -> {
            String code = verificationCodeInput.getText().toString().trim();
            if (code.isEmpty()) {
                verificationCodeInput.setError("Please enter verification code");
                return;
            }
            verifyCode(code);
        });

        resendCodeText.setOnClickListener(v -> {
            resendCodeText.setEnabled(false);
            sendVerificationCode();
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                runOnUiThread(() -> resendCodeText.setEnabled(true)), 60000);
        });
    }

    private void sendVerificationCode() {
        verificationCode = verificationHelper.generateVerificationCode();
        verificationHelper.saveVerificationCode(email, verificationCode);

        Thread emailThread = new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", "587");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("somnium.verif@gmail.com", "zxac kqrx zsaf zrf");
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("somnium.verif@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Somnium Email Verification");
                message.setText("Your verification code is: " + verificationCode + "\n\nThis code will expire in 10 minutes.");

                Transport transport = session.getTransport("smtp");
                transport.connect("smtp.gmail.com", "somnium.verif@gmail.com", "hjrq chor yemu zgyd");
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();

                runOnUiThread(() ->
                    Toast.makeText(EmailVerificationActivity.this,
                        "Verification code sent", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(EmailVerificationActivity.this,
                        "Failed to send code: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });

        emailThread.start();
    }

    private void verifyCode(String code) {
        if (!code.equals(verificationCode)) {
            Toast.makeText(EmailVerificationActivity.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Firebase account
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user != null) {
                    // Create user data
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("fullname", fullname);
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("totalDreams", 0L);
                    userData.put("currentStreak", 0L);

                    // Get database reference and enable persistence
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.setPersistenceEnabled(true);
                    DatabaseReference userRef = database.getReference()
                        .child("users")
                        .child(user.getUid());
                    userRef.keepSynced(true);

                    // Save user data
                    userRef.setValue(userData)
                        .addOnSuccessListener(aVoid -> {
                            // Verify data was saved by reading it back
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // Data saved successfully, proceed to login
                                        mAuth.signOut();
                                        Intent intent = new Intent(EmailVerificationActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(EmailVerificationActivity.this,
                                            "Failed to verify data save",
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(EmailVerificationActivity.this,
                                        "Error verifying data: " + error.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EmailVerificationActivity.this,
                                "Failed to save user data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(EmailVerificationActivity.this,
                    "Account creation failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }
}
