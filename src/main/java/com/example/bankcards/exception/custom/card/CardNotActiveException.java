package com.example.bankcards.exception.custom.card;

import com.example.bankcards.util.ErrorMessages;

public class CardNotActiveException extends CardException {
    public CardNotActiveException() {
        super(ErrorMessages.CARD_NOT_ACTIVE);
    }
    public CardNotActiveException(String message) {
        super(message);
    }
}
