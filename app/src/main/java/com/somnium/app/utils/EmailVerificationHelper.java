package com.somnium.app.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Random;

public class EmailVerificationHelper {
    private static final int CODE_LENGTH = 6;
    private final DatabaseReference dbRef;

    public EmailVerificationHelper() {
        dbRef = FirebaseDatabase.getInstance().getReference("verification_codes");
    }

    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    public void saveVerificationCode(String email, String code) {
        String sanitizedEmail = email.replace(".", "_").replace("@", "_");
        dbRef.child(sanitizedEmail).setValue(code);
    }

    public void verifyCode(String email, String code, VerificationCallback callback) {
        String sanitizedEmail = email.replace(".", "_").replace("@", "_");
        dbRef.child(sanitizedEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String savedCode = task.getResult().getValue(String.class);
                if (savedCode != null && savedCode.equals(code)) {
                    // Code matches, remove it from database and notify success
                    dbRef.child(sanitizedEmail).removeValue();
                    callback.onVerificationSuccess();
                } else {
                    callback.onVerificationFailed("Invalid verification code");
                }
            } else {
                callback.onVerificationFailed("Failed to verify code");
            }
        });
    }

    public interface VerificationCallback {
        void onVerificationSuccess();
        void onVerificationFailed(String error);
    }
} 