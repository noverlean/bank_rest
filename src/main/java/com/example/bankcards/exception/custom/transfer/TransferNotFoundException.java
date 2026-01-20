package com.example.bankcards.exception.custom.transfer;

import com.example.bankcards.exception.custom.NotFoundException;
import com.example.bankcards.util.ErrorMessages;

public class TransferNotFoundException extends NotFoundException {
    public TransferNotFoundException() {
        super(ErrorMessages.TRANSFER_NOT_FOUND);
    }
    public TransferNotFoundException(String message) {
        super(message);
    }
}

