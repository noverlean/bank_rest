package com.example.bankcards.exception.custom.token;

public class InvalidRefreshTokenException extends TokenException {
    public InvalidRefreshTokenException() {
        super("Refresh token invalid");
    }
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
