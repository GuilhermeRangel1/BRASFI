package com.brasfi.webapp.entities;

public class PostSaida {
    private String authorName; 
    private String messageContent; 

    public PostSaida(String authorName, String messageContent) {
        this.authorName = authorName;
        this.messageContent = messageContent;
    }

    public PostSaida() {
    }

    // Getters
    public String getAuthorName() {
        return authorName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}