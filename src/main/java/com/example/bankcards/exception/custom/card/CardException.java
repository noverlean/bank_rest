package com.example.bankcards.exception.custom.card;

import com.example.bankcards.exception.CustomException;
import com.example.bankcards.util.ErrorMessages;

public class CardException extends CustomException {
    public CardException() {
        super(ErrorMessages.CARD_UNKNOWN);
    }
    public CardException(String message) {
        super(message);
    }
}
