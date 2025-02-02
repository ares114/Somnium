package com.somnium.app;

import java.util.List; // Add this import
import java.util.ArrayList; // Add this import

public class ApiRequest {
    private String model = "deepseek/deepseek-r1:free";
    private List<Message> messages = new ArrayList<>();


    public ApiRequest() {}

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}