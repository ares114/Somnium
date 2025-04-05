package com.somnium.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class VerificationActivity extends BaseActivity {

    private Button btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        btnBackToLogin = findViewById(R.id.btn_back_to_login);
        
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to login screen
                Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
} 