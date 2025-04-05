package com.somnium.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.somnium.app.R;
import com.somnium.app.adapters.DreamAdapter;
import com.somnium.app.viewmodels.DreamViewModel;

public class JournalFragment extends Fragment {
    
    private RecyclerView recyclerViewDreams;
    private DreamAdapter dreamAdapter;
    private DreamViewModel dreamViewModel;
    private View progressBar;
    private View textViewNoDreams;
    private FloatingActionButton fabAddDream;

    public JournalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerViewDreams = view.findViewById(R.id.recyclerViewDreams);
        progressBar = view.findViewById(R.id.progressBar);
        textViewNoDreams = view.findViewById(R.id.textViewNoDreams);
        fabAddDream = view.findViewById(R.id.floatingActionButton);
        
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        
        // Load dreams for current user
        loadUserDreams();
    }
    
    private void setupViewModel() {
        dreamViewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        
        // Observe dreams LiveData
        dreamViewModel.getDreamsList().observe(getViewLifecycleOwner(), dreams -> {
            dreamAdapter.updateDreams(dreams);
            progressBar.setVisibility(View.GONE);
            
            if (dreams.isEmpty()) {
                textViewNoDreams.setVisibility(View.VISIBLE);
            } else {
                textViewNoDreams.setVisibility(View.GONE);
            }
        });
        
        // Observe loading state
        dreamViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe errors
        dreamViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                dreamViewModel.clearError();
            }
        });
    }
    
    private void setupRecyclerView() {
        dreamAdapter = new DreamAdapter(java.util.Collections.emptyList(), 
            dream -> {
                // Handle dream click - navigate to detail view
                dreamViewModel.getDreamById(dream.getId());
                
                // Create and add the detail fragment
                try {
                    DreamDetailFragment detailFragment = new DreamDetailFragment();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, detailFragment)
                            .addToBackStack("dream_detail")
                            .commit();
                } catch (Exception e) {
                    // If there's an issue with navigation, at least show a toast
                    Toast.makeText(requireContext(), "Error navigating to dream details", Toast.LENGTH_SHORT).show();
                }
                
                return kotlin.Unit.INSTANCE;
            }
        );
        
        recyclerViewDreams.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewDreams.setAdapter(dreamAdapter);
    }
    
    private void setupListeners() {
        fabAddDream.setOnClickListener(v -> {
            // Navigate to add dream fragment
            AddDreamFragmentNew addDreamFragment = new AddDreamFragmentNew();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, addDreamFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
    
    private void loadUserDreams() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                
        if (userId != null) {
            dreamViewModel.loadUserDreams(userId);
        } else {
            // Handle user not logged in
            Toast.makeText(requireContext(), "You must be logged in to view your dreams", Toast.LENGTH_SHORT).show();
        }
    }
} 