package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CardService {
    CardResponse createCard(CardRequest request);
    CardResponse updateCard(Long cardId, CardRequest request);
    CardResponse requestToBlockCard(Long cardId);
    CardResponse blockCard(Long cardId);
    CardResponse activateCard(Long cardId);
    void deleteCard(Long cardId);
    CardResponse getCardById(Long cardId);
    Page<CardResponse> getUserCards(Pageable pageable);
    Page<CardResponse> getAllCards(Pageable pageable);
    List<CardResponse> searchCards(String query);
    boolean isCardBelongsToUser(Long cardId, User user);
    Card findCardEntityById(Long cardId);
    void updateBalance(Long cardId, BigDecimal amount, boolean isCredit);
    boolean isCardActive(Long cardId);
}
