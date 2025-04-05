package com.somnium.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.somnium.app.R;
import com.somnium.app.models.Dream;
import com.somnium.app.viewmodels.DreamViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;

public class AddDreamFragmentNew extends Fragment {

    private static final String TAG = "AddDreamFragmentNew";
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    
    private TextInputEditText editTextTitle, editTextContent;
    private TextInputLayout textInputLayoutTitle, textInputLayoutContent;
    private Button buttonSave, buttonCancel;
    private ProgressBar progressBar;
    private DatePicker datePicker;
    private FloatingActionButton fabVoice;
    
    private DreamViewModel viewModel;
    private Observer<Boolean> loadingObserver;
    private Observer<String> errorObserver;
    private boolean isSaving = false;
    private boolean navigateAfterSave = false;

    public AddDreamFragmentNew() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_dream, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextContent = view.findViewById(R.id.editTextContent);
        textInputLayoutTitle = view.findViewById(R.id.textInputLayoutTitle);
        textInputLayoutContent = view.findViewById(R.id.textInputLayoutContent);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        progressBar = view.findViewById(R.id.progressBar);
        datePicker = view.findViewById(R.id.datePicker);
        fabVoice = view.findViewById(R.id.fabVoice);
        
        // Initialize ViewModel - use the fragment's viewmodel for better lifecycle management
        viewModel = new ViewModelProvider(this).get(DreamViewModel.class);

        // Reset state
        isSaving = false;
        navigateAfterSave = false;

        // Setup observers
        setupObservers();

        // Setup save button click listener
        buttonSave.setOnClickListener(v -> saveDream());

        // Setup cancel button click listener
        buttonCancel.setOnClickListener(v -> safeNavigateUp());
        
        // Setup voice input button
        fabVoice.setOnClickListener(v -> startVoiceInput());
        
        // Limit the date picker to today's date (no future dates)
        limitDatePickerToCurrentDate();
    }
    
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me about your dream...");
        
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Sorry, your device doesn't support speech input", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error starting voice input: " + e.getMessage());
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                // Get the current content and append the new content
                String currentContent = editTextContent.getText() != null ? 
                                      editTextContent.getText().toString() : "";
                
                // If there's already content, add a newline before appending
                if (!currentContent.isEmpty() && !currentContent.endsWith("\n")) {
                    currentContent += "\n";
                }
                
                // Append the spoken text
                String updatedContent = currentContent + result.get(0);
                editTextContent.setText(updatedContent);
                
                // Set cursor at the end
                editTextContent.setSelection(updatedContent.length());
            }
        }
    }
    
    private void setupObservers() {
        // Loading observer
        loadingObserver = isLoading -> {
            if (!isAdded()) return; // Safety check
            
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            buttonSave.setEnabled(!isLoading);
            
            // If we were saving and loading completes (isLoading = false) and there's no error
            if (isSaving && !isLoading) {
                String errorMsg = viewModel.getErrorMessage().getValue();
                if (errorMsg == null || errorMsg.isEmpty()) {
                    // Success! Clear the form instead of navigating back
                    Toast.makeText(requireContext(), "Dream saved successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                }
                isSaving = false;
            }
        };
        
        // Error observer
        errorObserver = errorMessage -> {
            if (!isAdded()) return; // Safety check
            
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearError();
                isSaving = false;
            }
        };
        
        // Observe loading and error states
        viewModel.isLoading().observe(getViewLifecycleOwner(), loadingObserver);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorObserver);
    }

    private void saveDream() {
        // Validate inputs
        boolean isValid = true;

        String title = editTextTitle.getText() != null ? editTextTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            textInputLayoutTitle.setError("Title is required");
            isValid = false;
        } else {
            textInputLayoutTitle.setError(null);
        }

        String content = editTextContent.getText() != null ? editTextContent.getText().toString().trim() : "";
        if (content.isEmpty()) {
            textInputLayoutContent.setError("Content is required");
            isValid = false;
        } else {
            textInputLayoutContent.setError(null);
        }

        if (!isValid) {
            return;
        }

        // Get current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                       FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        
        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "You must be logged in to save dreams", Toast.LENGTH_LONG).show();
            return;
        }

        // Get selected date from DatePicker
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
        Date selectedDate = calendar.getTime();
        
        Log.d(TAG, "Saving dream - Title: " + title + ", Content: " + content + ", User ID: " + userId + ", Date: " + selectedDate);
        
        // Set flag to indicate we're in the process of saving
        isSaving = true;
        
        // Call the saveDream method with individual parameters
        viewModel.saveDream(title, content, userId, selectedDate);
    }
    
    // Safe navigation that checks if the fragment is still attached to the activity
    private void safeNavigateUp() {
        if (isAdded() && getView() != null) {
            try {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigateUp();
            } catch (Exception e) {
                Log.e(TAG, "Error navigating up", e);
                // Fallback if navigation fails
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        }
    }
    
    // Add new method to clear the form
    private void clearForm() {
        editTextTitle.setText("");
        editTextContent.setText("");
        textInputLayoutTitle.setError(null);
        textInputLayoutContent.setError(null);
        
        // Set focus back to title field
        editTextTitle.requestFocus();
    }
    
    // Add a new method to limit the date picker
    private void limitDatePickerToCurrentDate() {
        Calendar today = Calendar.getInstance();
        
        // Set the max date to today (no future dates allowed)
        datePicker.setMaxDate(today.getTimeInMillis());
        
        // Optional: Set minimum date to something reasonable like 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePicker.setMinDate(minDate.getTimeInMillis());
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up observers to prevent memory leaks
        if (viewModel != null) {
            if (loadingObserver != null) {
                viewModel.isLoading().removeObserver(loadingObserver);
            }
            if (errorObserver != null) {
                viewModel.getErrorMessage().removeObserver(errorObserver);
            }
        }
    }
} 