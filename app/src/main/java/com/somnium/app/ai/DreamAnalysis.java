package com.somnium.app.ai;

public class DreamAnalysis {
    private final String fullAnalysis;
    private String mainThemes;
    private String symbolicMeanings;
    private String emotionalAnalysis;
    private String possibleInterpretations;
    private String actionSuggestions;

    public DreamAnalysis(String fullAnalysis) {
        this.fullAnalysis = fullAnalysis;
        parseAnalysisSections();
    }

    private void parseAnalysisSections() {
        // TODO: Implement proper parsing of sections
        // For now, just store the full analysis
    }

    public String getFullAnalysis() {
        return fullAnalysis;
    }

    public String getMainThemes() {
        return mainThemes;
    }

    public String getSymbolicMeanings() {
        return symbolicMeanings;
    }

    public String getEmotionalAnalysis() {
        return emotionalAnalysis;
    }

    public String getPossibleInterpretations() {
        return possibleInterpretations;
    }

    public String getActionSuggestions() {
        return actionSuggestions;
    }
} 