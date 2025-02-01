package com.somnium.app;

public class ApiResponse {
    private List<Choice> choices;

    public static class Choice {
        private Message message;

        public static class Message {
            private String content;
            // Getters and setters
        }
        // Getters and setters
    }
    // Getters and setters
}