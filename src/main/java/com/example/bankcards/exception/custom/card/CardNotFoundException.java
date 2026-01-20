package com.example.bankcards.exception.custom.card;

import com.example.bankcards.exception.custom.NotFoundException;
import com.example.bankcards.util.ErrorMessages;

public class CardNotFoundException extends NotFoundException {
    public CardNotFoundException() {
        super(ErrorMessages.CARD_NOT_FOUND);
    }
    public CardNotFoundException(String message) {
        super(message);
    }
}
