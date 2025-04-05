package com.somnium.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.somnium.app.R;
import com.somnium.app.adapters.HomeAnalyzedDreamAdapter;
import com.somnium.app.adapters.HomeRecentDreamAdapter;
import com.somnium.app.models.Dream;
import com.somnium.app.viewmodels.DreamViewModel;

public class HomeFragment extends Fragment {

    private TextView textViewGreeting;
    private TextView textViewDate;
    private RecyclerView recyclerViewRecentDreams;
    private RecyclerView recyclerViewAnalyzedDreams;
    private LinearLayout layoutNoAnalyzedDreams;
    private Button buttonRecordDream;
    private DreamViewModel viewModel;
    private HomeRecentDreamAdapter recentDreamsAdapter;
    private HomeAnalyzedDreamAdapter analyzedDreamsAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        textViewGreeting = view.findViewById(R.id.textViewGreeting);
        textViewDate = view.findViewById(R.id.textViewDate);
        recyclerViewRecentDreams = view.findViewById(R.id.recyclerViewRecentDreams);
        recyclerViewAnalyzedDreams = view.findViewById(R.id.recyclerViewAnalyzedDreams);
        layoutNoAnalyzedDreams = view.findViewById(R.id.layoutNoAnalyzedDreams);
        buttonRecordDream = view.findViewById(R.id.buttonRecordDream);
        
        // Set dynamic greeting and date
        updateGreetingAndDate();
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        
        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Setup Record Dream button
        buttonRecordDream.setOnClickListener(v -> navigateToAddDream());
        
        // Load user dreams if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadUserDreams(currentUser.getUid());
        }
    }
    
    private void updateGreetingAndDate() {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        
        // Determine greeting based on time of day
        String greeting;
        if (hourOfDay >= 5 && hourOfDay < 12) {
            greeting = "Good morning";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            greeting = "Good afternoon";
        } else if (hourOfDay >= 17 && hourOfDay < 21) {
            greeting = "Good evening";
        } else {
            greeting = "Good night";
        }
        
        // Set the greeting text
        textViewGreeting.setText(greeting + ", Dreamer");
        
        // Format and set the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());
        textViewDate.setText(formattedDate);
    }
    
    private void setupRecyclerViews() {
        // Recent Dreams RecyclerView
        recyclerViewRecentDreams.setLayoutManager(new LinearLayoutManager(requireContext()));
        recentDreamsAdapter = new HomeRecentDreamAdapter(
            // Empty initial list
            java.util.Collections.emptyList(),
            // OnClick listener
            dream -> {
                navigateToDreamDetail(dream);
                return null;
            }
        );
        recyclerViewRecentDreams.setAdapter(recentDreamsAdapter);
        
        // Analyzed Dreams RecyclerView
        recyclerViewAnalyzedDreams.setLayoutManager(new LinearLayoutManager(requireContext()));
        analyzedDreamsAdapter = new HomeAnalyzedDreamAdapter(
            // Empty initial list
            java.util.Collections.emptyList(),
            // OnClick listener
            dream -> {
                navigateToDreamDetail(dream);
                return null;
            }
        );
        recyclerViewAnalyzedDreams.setAdapter(analyzedDreamsAdapter);
    }
    
    private void loadUserDreams(String userId) {
        viewModel.loadUserDreams(userId);
        
        // Observe dreams list
        viewModel.getDreamsList().observe(getViewLifecycleOwner(), dreams -> {
            try {
                if (dreams != null && !dreams.isEmpty()) {
                    // Update Recent Dreams
                    recentDreamsAdapter.updateDreams(dreams);
                    
                    // Filter analyzed dreams
                    int analyzedCount = 0;
                    for (Dream dream : dreams) {
                        if (dream != null && dream.hasAnalysis()) {
                            analyzedCount++;
                        }
                    }
                    boolean hasAnalyzedDreams = analyzedCount > 0;
                    
                    if (hasAnalyzedDreams) {
                        // Show the RecyclerView with analyzed dreams
                        recyclerViewAnalyzedDreams.setVisibility(View.VISIBLE);
                        layoutNoAnalyzedDreams.setVisibility(View.GONE);
                        
                        // Update adapter
                        analyzedDreamsAdapter.updateDreams(dreams);
                    } else {
                        // Show the empty state
                        recyclerViewAnalyzedDreams.setVisibility(View.GONE);
                        layoutNoAnalyzedDreams.setVisibility(View.VISIBLE);
                    }
                } else {
                    // If no dreams at all, show empty states
                    recentDreamsAdapter.updateDreams(java.util.Collections.emptyList());
                    recyclerViewAnalyzedDreams.setVisibility(View.GONE);
                    layoutNoAnalyzedDreams.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                // Log the error but don't crash
                android.util.Log.e("HomeFragment", "Error updating dreams: " + e.getMessage());
                e.printStackTrace();
                
                // Reset to a safe state
                recentDreamsAdapter.updateDreams(java.util.Collections.emptyList());
                analyzedDreamsAdapter.updateDreams(java.util.Collections.emptyList());
                recyclerViewAnalyzedDreams.setVisibility(View.GONE);
                layoutNoAnalyzedDreams.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void navigateToAddDream() {
        Navigation.findNavController(requireView())
            .navigate(R.id.addDreamFragment);
    }
    
    private void navigateToDreamDetail(Dream dream) {
        // Set the selected dream in the ViewModel
        viewModel.getDreamById(dream.getId());
        
        // Navigate to the dream detail screen
        Navigation.findNavController(requireView())
            .navigate(R.id.dreamDetailFragment);
    }
} 