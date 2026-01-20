package com.example.bankcards.exception.custom;

import com.example.bankcards.exception.CustomException;

public class NotFoundException extends CustomException {
    public NotFoundException(String message) {
        super(message);
    }
}
