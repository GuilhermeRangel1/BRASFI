package com.brasfi.webapp.entities;

public class PostSaida {
    private String authorName;
    private String messageContent;
    private Long authorId; 

    public PostSaida(String authorName, String messageContent, Long authorId) {
        this.authorName = authorName;
        this.messageContent = messageContent;
        this.authorId = authorId;
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

    public Long getAuthorId() { 
        return authorId;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public void setAuthorId(Long authorId) { 
        this.authorId = authorId;
    }
}