package com.example.bankcards.exception.custom.card;

import com.example.bankcards.util.ErrorMessages;

public class CardIsBlockedException extends CardException {
    public CardIsBlockedException() {
        super(ErrorMessages.CARD_BLOCKED);
    }
    public CardIsBlockedException(String message) {
        super(message);
    }
}
