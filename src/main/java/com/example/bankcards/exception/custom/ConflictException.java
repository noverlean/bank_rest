package com.example.bankcards.exception.custom;

import com.example.bankcards.exception.CustomException;

public class ConflictException extends CustomException {
    public ConflictException(String message) {
        super(message);
    }
}

