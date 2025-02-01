package com.somnium.app;

import java.util.List; // Add this import
import java.util.ArrayList; // Add this import

public class ApiRequest {
    private String model = "deepseek-chat";
    private List<Message> messages = new ArrayList<>(); // Initialize the list

    // Constructor
    public ApiRequest() {}

    // Getters and setters
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    // Message inner class
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getters and setters
        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}