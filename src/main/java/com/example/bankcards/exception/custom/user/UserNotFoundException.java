package com.example.bankcards.exception.custom.user;

import com.example.bankcards.exception.custom.NotFoundException;
import com.example.bankcards.util.ErrorMessages;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super(ErrorMessages.USER_NOT_FOUND);
    }
    public UserNotFoundException(String message) {
        super(message);
    }
}
