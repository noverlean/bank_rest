package com.example.bankcards.util;

public enum CardStatus {
    ACTIVE("active"),
    BLOCKED("blocked"),
    EXPIRED("expired");

    private final String description;

    CardStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}