package com.somnium.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import com.somnium.app.ai.DreamAnalyzer;
import com.somnium.app.ai.DreamAnalysis;

public class HomepageActivity extends BaseActivity {
    private EditText dreamInput;
    private TextView analysisResult;
    private Button analyzeButton;
    private ProgressBar progressBar;
    private DreamAnalyzer dreamAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Initialize views
        dreamInput = findViewById(R.id.dream_input);
        analysisResult = findViewById(R.id.analysis_result);
        analyzeButton = findViewById(R.id.analyze_button);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize dream analyzer
        dreamAnalyzer = new DreamAnalyzer(this);

        // Set up click listener
        analyzeButton.setOnClickListener(v -> analyzeDream());

        // Set up navigation
        setupBottomNavigation();
    }

    private void analyzeDream() {
        String dreamText = dreamInput.getText().toString().trim();
        if (dreamText.isEmpty()) {
            Toast.makeText(this, "Please enter your dream first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        analyzeButton.setEnabled(false);
        analysisResult.setText("Analyzing your dream...");

        // Analyze the dream
        dreamAnalyzer.analyzeDream(dreamText, new DreamAnalyzer.AnalysisCallback() {
            @Override
            public void onSuccess(DreamAnalysis analysis) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    analyzeButton.setEnabled(true);
                    analysisResult.setText(analysis.getFullAnalysis());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    analyzeButton.setEnabled(true);
                    Toast.makeText(HomepageActivity.this, error, Toast.LENGTH_LONG).show();
                    analysisResult.setText("Failed to analyze dream. Please try again.");
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}