package com.somnium.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.somnium.app.R;
import com.somnium.app.models.Dream;
import com.somnium.app.viewmodels.DreamViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private TextView tvTotalDreams;
    private TextView tvAnalyzedDreams;
    private TextView tvCommonThemes;
    private TextView tvLongestStreak;
    private DreamViewModel viewModel;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tvTotalDreams = view.findViewById(R.id.textViewTotalDreams);
        tvAnalyzedDreams = view.findViewById(R.id.textViewAnalyzedDreams);
        tvCommonThemes = view.findViewById(R.id.textViewCommonThemes);
        tvLongestStreak = view.findViewById(R.id.textViewLongestStreak);
        
        viewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadUserStatistics(user.getUid());
        }
    }
    
    private void loadUserStatistics(String userId) {
        viewModel.loadUserDreams(userId);
        
        viewModel.getDreamsList().observe(getViewLifecycleOwner(), dreams -> {
            if (dreams != null) {
                updateStatistics(dreams);
            }
        });
    }
    
    private void updateStatistics(List<Dream> dreams) {
        int totalDreams = dreams.size();
        tvTotalDreams.setText(String.valueOf(totalDreams));
        
        int analyzedDreams = 0;
        for (Dream dream : dreams) {
            if (dream.hasAnalysis()) {
                analyzedDreams++;
            }
        }
        tvAnalyzedDreams.setText(String.valueOf(analyzedDreams));
        
        Map<String, Integer> themes = analyzeCommonThemes(dreams);
        StringBuilder themesText = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Integer> entry : themes.entrySet()) {
            if (count++ < 3) {
                themesText.append(entry.getKey()).append(" (").append(entry.getValue()).append(")\n");
            } else {
                break;
            }
        }
        tvCommonThemes.setText(themesText.toString().trim());
        
        tvLongestStreak.setText(calculateLongestStreak(dreams) + " days");
    }
    
    private Map<String, Integer> analyzeCommonThemes(List<Dream> dreams) {
        Map<String, Integer> themes = new HashMap<>();
        String[] commonThemes = {"water", "flying", "falling", "chase", "family", "work", "school"};
        
        for (Dream dream : dreams) {
            String content = dream.getContent().toLowerCase();
            for (String theme : commonThemes) {
                if (content.contains(theme)) {
                    themes.put(theme, themes.getOrDefault(theme, 0) + 1);
                }
            }
        }
        
        return themes;
    }
    
    private int calculateLongestStreak(List<Dream> dreams) {
        return Math.min(dreams.size(), 7);
    }
} 