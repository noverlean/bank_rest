package com.example.bankcards.exception.custom.card;

import com.example.bankcards.exception.custom.ConflictException;
import com.example.bankcards.util.ErrorMessages;

public class SameCardTransferException extends ConflictException {
    public SameCardTransferException() {
        super(ErrorMessages.SAME_CARD_TRANSFER);
    }
    public SameCardTransferException(String message) {
        super(message);
    }
}
