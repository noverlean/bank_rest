package com.example.bankcards.controller;

import com.example.bankcards.api.CardApi;
import com.example.bankcards.dto.request.CardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CardController implements CardApi {

    private final CardService cardService;

    @Override
    public CardResponse createCard(@Valid @RequestBody CardRequest request) {
        return cardService.createCard(request);
    }

    @Override
    public CardResponse updateCard(@PathVariable Long cardId, @Valid @RequestBody CardRequest request) {
        return cardService.updateCard(cardId, request);
    }

    @Override
    public Page<CardResponse> getMyCards(@PageableDefault(size = 10) Pageable pageable) {
        return cardService.getUserCards(pageable);
    }

    @Override
    public CardResponse getCard(@PathVariable Long cardId) {
        return cardService.getCardById(cardId);
    }

    @Override
    public CardResponse requestToBlockCard(@PathVariable Long cardId) {
        return cardService.requestToBlockCard(cardId);
    }

    @Override
    public CardResponse blockCard(@PathVariable Long cardId) {
        return cardService.blockCard(cardId);
    }

    @Override
    public CardResponse activateCard(@PathVariable Long cardId) {
        return cardService.activateCard(cardId);
    }

    @Override
    public ResponseEntity<String> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Card successfully deleted");
    }

    @Override
    public Page<CardResponse> searchCards(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable) {
        // Реализация поиска
        return cardService.getUserCards(pageable);
    }

    @Override
    public Page<CardResponse> getAllCards(@PageableDefault(size = 20) Pageable pageable) {
        return cardService.getAllCards(pageable);
    }
}