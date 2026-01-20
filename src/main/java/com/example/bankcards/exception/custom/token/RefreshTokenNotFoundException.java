package com.example.bankcards.exception.custom.token;

public class RefreshTokenNotFoundException extends TokenException {
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
