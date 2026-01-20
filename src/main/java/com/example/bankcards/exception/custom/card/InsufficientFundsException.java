package com.example.bankcards.exception.custom.card;

import com.example.bankcards.util.ErrorMessages;

public class InsufficientFundsException extends CardException {
    public InsufficientFundsException() {
        super(ErrorMessages.INSUFFICIENT_FUNDS);
    }
    public InsufficientFundsException(String message) {
        super(message);
    }
}
