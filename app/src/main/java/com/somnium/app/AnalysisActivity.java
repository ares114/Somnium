package com.somnium.app;

import android.os.Bundle;

public class AnalysisActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        setupBottomNavigation();
    }
}