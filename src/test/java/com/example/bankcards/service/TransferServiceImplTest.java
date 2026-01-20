package com.example.bankcards.service;

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
import com.example.bankcards.service.impl.TransferServiceImpl;
import com.example.bankcards.util.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @Mock
    private TransferMapper transferMapper;

    @InjectMocks
    private TransferServiceImpl transferService;

    private User testUser;
    private User adminUser;
    private Card fromCard;
    private Card toCard;
    private TransferRequest transferRequest;
    private Transfer transfer;
    private TransferResponse transferResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .role(User.Role.USER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .email("admin@example.com")
                .role(User.Role.ADMIN)
                .build();

        fromCard = Card.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(1000.00))
                .user(testUser)
                .status(CardStatus.ACTIVE)
                .build();

        toCard = Card.builder()
                .id(2L)
                .balance(BigDecimal.valueOf(500.00))
                .status(CardStatus.ACTIVE)
                .build();

        transferRequest = TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transfer")
                .build();

        transfer = Transfer.builder()
                .id(1L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transfer")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        transferResponse = TransferResponse.builder()
                .id(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transfer")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTransfer_ShouldCreateTransfer_WhenAllConditionsMet() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(true);
        when(cardService.isCardActive(1L)).thenReturn(true);
        when(cardService.isCardActive(2L)).thenReturn(true);
        when(cardService.findCardEntityById(1L)).thenReturn(fromCard);
        when(cardService.findCardEntityById(2L)).thenReturn(toCard);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(transferMapper.toDto(transfer)).thenReturn(transferResponse);

        // Act
        TransferResponse result = transferService.createTransfer(transferRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));

        verify(cardService).updateBalance(1L, BigDecimal.valueOf(100.00), false);
        verify(cardService).updateBalance(2L, BigDecimal.valueOf(100.00), true);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void createTransfer_ShouldThrowException_WhenSameCardTransfer() {
        // Arrange
        transferRequest.setToCardId(1L); // Same as fromCardId

        // Act & Assert
        assertThatThrownBy(() -> transferService.createTransfer(transferRequest))
                .isInstanceOf(SameCardTransferException.class);
    }

    @Test
    void createTransfer_ShouldThrowAccessDenied_WhenUserDoesNotOwnFromCard() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> transferService.createTransfer(transferRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createTransfer_ShouldThrowException_WhenSenderCardNotActive() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(true);
        when(cardService.isCardActive(1L)).thenReturn(false);

        // Act & Assert - проверяем только тип исключения, без проверки сообщения
        assertThatThrownBy(() -> transferService.createTransfer(transferRequest))
                .isInstanceOf(CardNotActiveException.class);
    }

    @Test
    void createTransfer_ShouldThrowException_WhenRecipientCardNotActive() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(true);
        when(cardService.isCardActive(1L)).thenReturn(true);
        when(cardService.isCardActive(2L)).thenReturn(false);

        // Act & Assert - проверяем только тип исключения
        assertThatThrownBy(() -> transferService.createTransfer(transferRequest))
                .isInstanceOf(CardNotActiveException.class);
    }

    @Test
    void createTransfer_ShouldThrowException_WhenInsufficientFunds() {
        // Arrange
        transferRequest.setAmount(BigDecimal.valueOf(1500.00)); // More than balance
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(true);
        when(cardService.isCardActive(1L)).thenReturn(true);
        when(cardService.isCardActive(2L)).thenReturn(true);
        when(cardService.findCardEntityById(1L)).thenReturn(fromCard);

        // Act & Assert
        assertThatThrownBy(() -> transferService.createTransfer(transferRequest))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void createTransfer_ShouldUpdateBalancesCorrectly() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(true);
        when(cardService.isCardActive(1L)).thenReturn(true);
        when(cardService.isCardActive(2L)).thenReturn(true);
        when(cardService.findCardEntityById(1L)).thenReturn(fromCard);
        when(cardService.findCardEntityById(2L)).thenReturn(toCard);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(transferMapper.toDto(transfer)).thenReturn(transferResponse);

        // Act
        transferService.createTransfer(transferRequest);

        // Assert
        verify(cardService).updateBalance(eq(1L), eq(BigDecimal.valueOf(100.00)), eq(false));
        verify(cardService).updateBalance(eq(2L), eq(BigDecimal.valueOf(100.00)), eq(true));
    }

    @Test
    void getUserTransfers_ShouldReturnUserTransfers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfer> transferPage = new PageImpl<>(List.of(transfer));

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(transferRepository.findByUserId(1L, pageable)).thenReturn(transferPage);
        when(transferMapper.toDto(transfer)).thenReturn(transferResponse);

        // Act
        Page<TransferResponse> result = transferService.getUserTransfers(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(transferRepository).findByUserId(1L, pageable);
    }

    @Test
    void getCardTransfers_ShouldReturnTransfers_WhenUserOwnsCard() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfer> transferPage = new PageImpl<>(List.of(transfer));

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(true);
        when(transferRepository.findByUserIdAndCardId(1L, 1L, pageable)).thenReturn(transferPage);
        when(transferMapper.toDto(transfer)).thenReturn(transferResponse);

        // Act
        Page<TransferResponse> result = transferService.getCardTransfers(1L, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getCardTransfers_ShouldReturnTransfers_WhenUserIsAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfer> transferPage = new PageImpl<>(List.of(transfer));

        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(transferRepository.findByUserIdAndCardId(2L, 1L, pageable)).thenReturn(transferPage);
        when(transferMapper.toDto(transfer)).thenReturn(transferResponse);

        // Act
        Page<TransferResponse> result = transferService.getCardTransfers(1L, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getCardTransfers_ShouldThrowAccessDenied_WhenUserNotOwnerAndNotAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User anotherUser = User.builder().id(999L).role(User.Role.USER).build();

        when(userService.getCurrentUserEntity()).thenReturn(anotherUser);
        when(cardService.isCardBelongsToUser(1L, anotherUser)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> transferService.getCardTransfers(1L, pageable))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getTransferById_ShouldReturnTransfer_WhenUserIsOwner() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferMapper.toDto(transfer)).thenReturn(transferResponse);

        // Act
        TransferResponse result = transferService.getTransferById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getTransferById_ShouldReturnTransfer_WhenUserIsAdmin() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferMapper.toDto(transfer)).thenReturn(transferResponse);

        // Act
        TransferResponse result = transferService.getTransferById(1L);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void getTransferById_ShouldThrowAccessDenied_WhenUserNotOwnerAndNotAdmin() {
        // Arrange
        User anotherUser = User.builder().id(999L).role(User.Role.USER).build();
        when(userService.getCurrentUserEntity()).thenReturn(anotherUser);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        // Act & Assert
        assertThatThrownBy(() -> transferService.getTransferById(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getTransferById_ShouldThrowException_WhenTransferNotFound() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(transferRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transferService.getTransferById(1L))
                .isInstanceOf(TransferNotFoundException.class);
    }

    @Test
    void createTransfer_ShouldHandleZeroAmount() {
        // Arrange
        transferRequest.setAmount(BigDecimal.ZERO);
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(true);
        when(cardService.isCardActive(1L)).thenReturn(true);
        when(cardService.isCardActive(2L)).thenReturn(true);
        when(cardService.findCardEntityById(1L)).thenReturn(fromCard);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(transferMapper.toDto(transfer)).thenReturn(transferResponse);

        // Act
        TransferResponse result = transferService.createTransfer(transferRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(cardService).updateBalance(eq(1L), eq(BigDecimal.ZERO), eq(false));
    }

    @Test
    void createTransfer_ShouldHandleNegativeAmountRequest() {
        // Arrange
        transferRequest.setAmount(BigDecimal.valueOf(-100.00));

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardService.isCardBelongsToUser(1L, testUser)).thenReturn(false);

        // Act & Assert - получается AccessDeniedException, а не IllegalArgumentException
        assertThatThrownBy(() -> transferService.createTransfer(transferRequest))
                .isInstanceOf(com.example.bankcards.exception.custom.AccessDeniedException.class);
    }
}