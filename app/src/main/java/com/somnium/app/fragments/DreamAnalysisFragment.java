package com.somnium.app.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.somnium.app.R;
import com.somnium.app.ai.DreamAnalysis;
import com.somnium.app.ai.DreamAnalyzer;
import com.somnium.app.models.Dream;
import com.somnium.app.viewmodels.DreamViewModel;

import java.util.Locale;

public class DreamAnalysisFragment extends Fragment implements TextToSpeech.OnInitListener {

    private Toolbar toolbar;
    private TextView tvDreamTitle, tvDreamContent;
    private TextView tvAnalysisLoading;
    private ProgressBar progressBar;
    private CardView cardSummary, cardMeaning, cardAdvice;
    private TextView tvSummary, tvMeaning, tvAdvice;
    private Button btnSaveAnalysis;
    private FloatingActionButton fabSpeak;
    
    private DreamViewModel viewModel;
    private Dream currentDream;
    private DreamAnalyzer dreamAnalyzer;
    private DreamAnalysis currentAnalysis;
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    
    public DreamAnalysisFragment() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dream_analysis, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        toolbar = view.findViewById(R.id.toolbar);
        tvDreamTitle = view.findViewById(R.id.textViewDreamTitle);
        tvDreamContent = view.findViewById(R.id.textViewDreamContent);
        tvAnalysisLoading = view.findViewById(R.id.textViewAnalysisLoading);
        progressBar = view.findViewById(R.id.progressBar);
        cardSummary = view.findViewById(R.id.cardSummary);
        cardMeaning = view.findViewById(R.id.cardMeaning);
        cardAdvice = view.findViewById(R.id.cardAdvice);
        tvSummary = view.findViewById(R.id.textViewSummary);
        tvMeaning = view.findViewById(R.id.textViewMeaning);
        tvAdvice = view.findViewById(R.id.textViewAdvice);
        btnSaveAnalysis = view.findViewById(R.id.buttonSaveAnalysis);
        fabSpeak = view.findViewById(R.id.fabSpeak);
        
        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(requireContext(), this);
        
        // Set up toolbar
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        toolbar.setNavigationIcon(R.drawable.ic_back);
        
        // Initialize ViewModel and DreamAnalyzer
        viewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        dreamAnalyzer = new DreamAnalyzer(requireContext());
        
        // Check if dream ID was passed in arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("dream_id")) {
            String dreamId = args.getString("dream_id");
            if (dreamId != null && !dreamId.isEmpty()) {
                // Load dream by ID
                viewModel.getDreamById(dreamId);
                showLoadingState(true);
            }
        }
        
        // Observe selected dream
        viewModel.getSelectedDreamValue().observe(getViewLifecycleOwner(), dream -> {
            if (dream != null) {
                currentDream = dream;
                displayDreamDetails(dream);
                
                // Check if dream already has analysis
                if (dream.hasAnalysis()) {
                    displayExistingAnalysis(dream);
                } else {
                    analyzeDreamWithGemini(dream);
                }
            }
        });
        
        // Save analysis button
        btnSaveAnalysis.setOnClickListener(v -> {
            if (currentDream != null && currentAnalysis != null) {
                saveAnalysisToDream();
            } else {
                Toast.makeText(requireContext(), "No analysis to save", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Set up speak button
        fabSpeak.setOnClickListener(v -> {
            speakAnalysis();
        });
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(requireContext(), "Language not supported", Toast.LENGTH_SHORT).show();
                fabSpeak.setVisibility(View.GONE);
            } else {
                isTtsReady = true;
                fabSpeak.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(requireContext(), "Text-to-speech initialization failed", Toast.LENGTH_SHORT).show();
            fabSpeak.setVisibility(View.GONE);
        }
    }
    
    private void speakAnalysis() {
        if (!isTtsReady || currentAnalysis == null) {
            Toast.makeText(requireContext(), "Speech not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            return;
        }
        
        // Combine the analysis sections into a coherent spoken text
        String textToRead = "Dream Analysis: " + 
                            "Summary: " + currentAnalysis.getSummary() + ". " +
                            "Meaning: " + currentAnalysis.getMeaning() + ". " +
                            "Advice: " + currentAnalysis.getAdvice();
        
        textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "dream_analysis");
    }
    
    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
    
    private void displayDreamDetails(Dream dream) {
        tvDreamTitle.setText(dream.getTitle());
        tvDreamContent.setText(dream.getContent());
    }
    
    private void analyzeDreamWithGemini(Dream dream) {
        dreamAnalyzer.analyzeDream(
            dream.getTitle(),
            dream.getContent(),
            new DreamAnalyzer.AnalysisCallback() {
                @Override
                public void onSuccess(DreamAnalysis analysis) {
                    if (isAdded()) {
                        currentAnalysis = analysis; // Store the analysis
                        displayAnalysisResults(analysis);
                        showLoadingState(false);
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                        showLoadingState(false);
                        
                        // Display a fallback analysis
                        String[] fallbackAnalysis = generateFallbackAnalysis(dream.getContent());
                        displayFallbackResults(fallbackAnalysis);
                        
                        // Create a fallback analysis object
                        currentAnalysis = new DreamAnalysis(
                            "fallback",
                            fallbackAnalysis[0],
                            fallbackAnalysis[1],
                            fallbackAnalysis[2],
                            ""
                        );
                    }
                }
                
                @Override
                public void onLoading() {
                    if (isAdded()) {
                        showLoadingState(true);
                    }
                }
            }
        );
    }
    
    private void showLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        tvAnalysisLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        
        cardSummary.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        cardMeaning.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        cardAdvice.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        btnSaveAnalysis.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        fabSpeak.setVisibility(isLoading ? View.GONE : (isTtsReady ? View.VISIBLE : View.GONE));
    }
    
    private void displayAnalysisResults(DreamAnalysis analysis) {
        tvSummary.setText(analysis.getSummary());
        tvMeaning.setText(analysis.getMeaning());
        tvAdvice.setText(analysis.getAdvice());
    }
    
    private void displayFallbackResults(String[] analysis) {
        // Display fallback results if Gemini fails
        tvSummary.setText(analysis[0]);
        tvMeaning.setText(analysis[1]);
        tvAdvice.setText(analysis[2]);
    }
    
    /**
     * Generate fallback dream analysis if the API call fails
     */
    private String[] generateFallbackAnalysis(String dreamContent) {
        String[] analysis = new String[3];
        
        // Generate a simple summary
        String summary = "Your dream contains themes related to ";
        String content = dreamContent.toLowerCase();
        
        if (content.contains("water") || content.contains("ocean") || content.contains("river")) {
            summary += "emotions and your unconscious mind. ";
        } else if (content.contains("fly") || content.contains("flying")) {
            summary += "freedom and perspective. ";
        } else if (content.contains("fall") || content.contains("falling")) {
            summary += "loss of control and letting go. ";
        } else if (content.contains("teeth") || content.contains("tooth")) {
            summary += "anxiety and self-image. ";
        } else if (content.contains("chase") || content.contains("chased")) {
            summary += "avoidance and unresolved issues. ";
        } else {
            summary += "personal growth and self-discovery. ";
        }
        
        // Generate interpretation
        String interpretation = "This dream reflects your current emotional state. ";
        if (content.contains("anxiety") || content.contains("fear") || content.contains("scared")) {
            interpretation += "You may be experiencing concerns that need addressing in your waking life.";
        } else if (content.contains("happy") || content.contains("joy")) {
            interpretation += "It suggests positive experiences or emotions that you're processing.";
        } else if (content.contains("lost") || content.contains("searching")) {
            interpretation += "You might be seeking direction or clarity in some aspect of your life.";
        } else {
            interpretation += "The imagery suggests you're processing important emotions and experiences.";
        }
        
        // Generate advice
        String advice = "Take time to reflect on the emotions this dream evoked. Consider journaling about what elements felt most significant to you and how they might relate to your current life circumstances.";
        
        analysis[0] = summary;
        analysis[1] = interpretation;
        analysis[2] = advice;
        
        return analysis;
    }
    
    // Display existing analysis from a dream
    private void displayExistingAnalysis(Dream dream) {
        // Create analysis object from dream data
        currentAnalysis = new DreamAnalysis(
            "saved_analysis",
            dream.getAnalysis_summary() != null ? dream.getAnalysis_summary() : "",
            dream.getAnalysis_meaning() != null ? dream.getAnalysis_meaning() : "",
            dream.getAnalysis_advice() != null ? dream.getAnalysis_advice() : "",
            ""
        );
        
        // Display the analysis
        displayAnalysisResults(currentAnalysis);
        showLoadingState(false);
        
        // Change button text to indicate analysis is already saved
        btnSaveAnalysis.setText(R.string.generate_new_analysis);
        btnSaveAnalysis.setEnabled(true);
        
        // Change button behavior to generate new analysis
        btnSaveAnalysis.setOnClickListener(v -> {
            analyzeDreamWithGemini(currentDream);
            
            // Restore original save button functionality
            btnSaveAnalysis.setOnClickListener(saveView -> {
                if (currentDream != null && currentAnalysis != null) {
                    saveAnalysisToDream();
                } else {
                    Toast.makeText(requireContext(), "No analysis to save", Toast.LENGTH_SHORT).show();
                }
            });
            
            // Reset button text
            btnSaveAnalysis.setText("Save Analysis");
        });
    }
    
    private void saveAnalysisToDream() {
        if (currentDream == null || currentDream.getId() == null) {
            Toast.makeText(requireContext(), "Cannot save analysis: Invalid dream", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnSaveAnalysis.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        
        // Use the ViewModel to save the analysis
        viewModel.saveDreamAnalysis(currentDream.getId(), currentAnalysis);
        
        // Observe the operation completion
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (!isLoading) {
                progressBar.setVisibility(View.GONE);
                
                // Check for errors
                String error = viewModel.getErrorMessage().getValue();
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                    btnSaveAnalysis.setEnabled(true);
                } else {
                    Toast.makeText(requireContext(), "Analysis saved to journal!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
} 