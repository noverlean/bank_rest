package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardNumberMasker {

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        return String.format("**** **** **** %s", lastFourDigits);
    }

    public String maskEncryptedCard(String encryptedCardNumber) {
        return maskCardNumber(encryptedCardNumber);
    }
}