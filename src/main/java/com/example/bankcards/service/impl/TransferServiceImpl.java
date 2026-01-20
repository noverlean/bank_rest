package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.custom.AccessDeniedException;
import com.example.bankcards.exception.custom.card.CardNotActiveException;
import com.example.bankcards.exception.custom.card.InsufficientFundsException;
import com.example.bankcards.exception.custom.card.SameCardTransferException;
import com.example.bankcards.exception.custom.transfer.TransferNotFoundException;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final CardService cardService;
    private final UserService userService;
    private final TransferMapper transferMapper;

    @Override
    @Transactional
    public TransferResponse createTransfer(TransferRequest request) {
        User user = userService.getCurrentUserEntity();

        // Проверка на перевод на ту же карту
        if (request.getFromCardId().equals(request.getToCardId())) {
            throw new SameCardTransferException();
        }

        // Проверка принадлежности карт пользователю
        if (!cardService.isCardBelongsToUser(request.getFromCardId(), user)) {
            throw new AccessDeniedException();
        }

        // Проверка активности карты отправителя
        if (!cardService.isCardActive(request.getFromCardId())) {
            throw new CardNotActiveException(ErrorMessages.SENDER_CARD_NOT_ACTIVE);
        }

        // Проверка активности карты получателя
        if (!cardService.isCardActive(request.getToCardId())) {
            throw new CardNotActiveException(ErrorMessages.RECIPIENT_CARD_NOT_ACTIVE);
        }

        // Проверка достаточности средств
        Card fromCard = cardService.findCardEntityById(request.getFromCardId());
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException();
        }

        // Выполняем перевод
        cardService.updateBalance(request.getFromCardId(), request.getAmount(), false); // Списание
        cardService.updateBalance(request.getToCardId(), request.getAmount(), true); // Зачисление

        Card toCard = cardService.findCardEntityById(request.getToCardId());

        // Создаем запись о переводе
        Transfer transfer = Transfer.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(request.getAmount())
                .description(request.getDescription())
                .user(user)
                .build();

        Transfer savedTransfer = transferRepository.save(transfer);
        return transferMapper.toDto(savedTransfer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferResponse> getUserTransfers(Pageable pageable) {
        User user = userService.getCurrentUserEntity();
        return transferRepository.findByUserId(user.getId(), pageable)
                .map(transferMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferResponse> getCardTransfers(Long cardId, Pageable pageable) {
        User user = userService.getCurrentUserEntity();

        // Проверяем доступ к карте
        if (!cardService.isCardBelongsToUser(cardId, user) &&
                !user.getRole().equals(User.Role.ADMIN)) {
            throw new AccessDeniedException();
        }

        return transferRepository.findByUserIdAndCardId(user.getId(), cardId, pageable)
                .map(transferMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long transferId) {
        User user = userService.getCurrentUserEntity();

        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(TransferNotFoundException::new);

        // Проверяем доступ
        if (!transfer.getUser().getId().equals(user.getId()) &&
                !user.getRole().equals(User.Role.ADMIN)) {
            throw new AccessDeniedException();
        }

        return transferMapper.toDto(transfer);
    }
}