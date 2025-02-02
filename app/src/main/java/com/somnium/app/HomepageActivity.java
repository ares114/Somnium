package com.somnium.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;


public class HomepageActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private EditText dreamInput;
    private TextView analysisResult;
    private Button analyzeButton;
    private final String API_KEY = "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"; // Replace with your OpenRouter API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        dreamInput = findViewById(R.id.dream_input);
        analysisResult = findViewById(R.id.analysis_result);
        analyzeButton = findViewById(R.id.analyze_button);
        bottomNav = findViewById(R.id.bottom_nav);

        analyzeButton.setOnClickListener(v -> analyzeDream());


        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Already on home
                    return true;
                } else if (itemId == R.id.nav_add) {
                    startActivity(new Intent(HomepageActivity.this, AddDreamActivity.class));
                    return true;
                } else if (itemId == R.id.nav_analysis) {
                    startActivity(new Intent(HomepageActivity.this, AnalysisActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(HomepageActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void analyzeDream() {
        String dreamText = dreamInput.getText().toString().trim();
        if (dreamText.isEmpty()) {
            Toast.makeText(this, "Please enter your dream first", Toast.LENGTH_SHORT).show();
            return;
        }

        analysisResult.setText("Analyzing your dream...");
        analyzeButton.setEnabled(false);

        ApiRequest.Message message = new ApiRequest.Message(
                "user",
                "Analyze this dream in detail and provide interpretation: " + dreamText
        );
        List<ApiRequest.Message> messages = new ArrayList<>();
        messages.add(message);

        ApiRequest request = new ApiRequest();
        request.setMessages(messages);

        ApiService service = ApiClient.getClient(API_KEY).create(ApiService.class);
        service.getCompletion(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                analyzeButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String result = response.body().getChoices().get(0).getMessage().getContent();
                        runOnUiThread(() -> analysisResult.setText(result));
                    } catch (Exception e) {
                        showError("Failed to parse response");
                    }
                } else {
                    showError("API Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                analyzeButton.setEnabled(true);
                showError("Network Error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            analysisResult.setText(message);
            Toast.makeText(HomepageActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }
}