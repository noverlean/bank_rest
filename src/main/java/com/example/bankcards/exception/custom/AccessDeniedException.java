package com.example.bankcards.exception.custom;

import com.example.bankcards.exception.CustomException;
import com.example.bankcards.util.ErrorMessages;

public class AccessDeniedException extends CustomException {
    public AccessDeniedException() {
        super(ErrorMessages.ACCESS_DENIED);
    }
    public AccessDeniedException(String message) {
        super(message);
    }
}

