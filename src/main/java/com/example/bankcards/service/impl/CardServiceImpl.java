package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.custom.AccessDeniedException;
import com.example.bankcards.exception.custom.card.CardNotFoundException;
import com.example.bankcards.exception.custom.card.InsufficientFundsException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardNumberMasker;
import com.example.bankcards.util.CardStatus;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserService userService;
    private final StringEncryptor cardEncryptor;
    private final CardNumberMasker cardNumberMasker;
    private final CardMapper cardMapper;
    private final Random random = new Random();

    @Override
    @Transactional
    public CardResponse createCard(CardRequest request) {
        User user = userService.getUserEntityById(request.getUserId());

        // Генерация номера карты
        String cardNumber = generateCardNumber();
        String encryptedNumber = cardEncryptor.encrypt(cardNumber);
        String maskedNumber = cardNumberMasker.maskCardNumber(cardNumber);

        // Проверка срока действия
        CardStatus status = request.getExpiryDate().isBefore(LocalDate.now())
                ? CardStatus.EXPIRED
                : CardStatus.ACTIVE;

        Card card = Card.builder()
                .cardNumber(encryptedNumber)
                .maskedNumber(maskedNumber)
                .owner(request.getOwner())
                .expiryDate(request.getExpiryDate())
                .status(status)
                .requestedBlock(false)
                .balance(request.getBalance())
                .user(user)
                .build();

        Card savedCard = cardRepository.save(card);
        return cardMapper.toDto(savedCard);
    }

    @Override
    @Transactional
    public CardResponse updateCard(Long cardId, CardRequest request) {
        Card card = findCardEntityById(cardId);

        // Обновляем только разрешенные поля
        card.setOwner(request.getOwner());
        card.setExpiryDate(request.getExpiryDate());

        // Обновляем статус если срок изменился
        if (request.getExpiryDate().isBefore(LocalDate.now()) &&
                card.getStatus() != CardStatus.BLOCKED) {
            card.setStatus(CardStatus.EXPIRED);
        }

        Card updatedCard = cardRepository.save(card);
        return cardMapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    public CardResponse requestToBlockCard(Long cardId) {
        User user = userService.getCurrentUserEntity();
        Card card = findCardEntityById(cardId);

        // Проверяем доступ
        if (!card.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException();
        }

        card.setRequestedBlock(true);
        Card updatedCard = cardRepository.save(card);
        return cardMapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    public CardResponse blockCard(Long cardId) {
        Card card = findCardEntityById(cardId);

        card.setStatus(CardStatus.BLOCKED);
        Card updatedCard = cardRepository.save(card);
        return cardMapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    public CardResponse activateCard(Long cardId) {
        Card card = findCardEntityById(cardId);

        // Проверяем, не истек ли срок
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            Card updatedCard = cardRepository.save(card);
            return cardMapper.toDto(updatedCard);
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updatedCard = cardRepository.save(card);
        return cardMapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException();
        }
        cardRepository.deleteById(cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long cardId) {
        User user = userService.getCurrentUserEntity();
        Card card = findCardEntityById(cardId);

        // Проверяем доступ
        if (!card.getUser().getId().equals(user.getId()) &&
                !user.getRole().equals(User.Role.ADMIN)) {
            throw new AccessDeniedException();
        }

        return cardMapper.toDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(Pageable pageable) {
        User user = userService.getCurrentUserEntity();
        Page<Card> cards;

        if (user.getRole().equals(User.Role.ADMIN)) {
            cards = cardRepository.findAll(pageable);
        } else {
            cards = cardRepository.findByUserId(user.getId(), pageable);
        }

        return cards.map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> searchCards(String query) {
        User user = userService.getCurrentUserEntity();
        // Реализация поиска
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCardBelongsToUser(Long cardId, User user) {
        return cardRepository.findByIdAndUserId(cardId, user.getId()).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Card findCardEntityById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException());
    }

    @Override
    @Transactional
    public void updateBalance(Long cardId, BigDecimal amount, boolean isCredit) {
        Card card = findCardEntityById(cardId);

        if (isCredit) {
            card.setBalance(card.getBalance().add(amount));
        } else {
            BigDecimal newBalance = card.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientFundsException();
            }
            card.setBalance(newBalance);
        }

        cardRepository.save(card);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCardActive(Long cardId) {
        Card card = findCardEntityById(cardId);
        return card.getStatus() == CardStatus.ACTIVE;
    }

    private String generateCardNumber() {
        // Генерация 16-значного номера карты
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
            if ((i + 1) % 4 == 0 && i != 15) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
