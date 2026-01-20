package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.custom.AccessDeniedException;
import com.example.bankcards.exception.custom.card.CardNotFoundException;
import com.example.bankcards.exception.custom.card.InsufficientFundsException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CardNumberMasker;
import com.example.bankcards.util.CardStatus;
import org.jasypt.encryption.StringEncryptor;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private StringEncryptor cardEncryptor;

    @Mock
    private CardNumberMasker cardNumberMasker;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private User testUser;
    private User adminUser;
    private Card testCard;
    private CardRequest cardRequest;
    private CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN)
                .build();

        testCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted_1234")
                .maskedNumber("**** **** **** 1234")
                .owner("JOHN DOE")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .requestedBlock(false)
                .balance(BigDecimal.valueOf(1000.00))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        cardRequest = CardRequest.builder()
                .userId(1L)
                .owner("JOHN DOE")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.valueOf(1000.00))
                .build();

        cardResponse = CardResponse.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .owner("JOHN DOE")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .requestedBlock(false)
                .balance(BigDecimal.valueOf(1000.00))
                .build();
    }

    @Test
    void createCard_ShouldCreateActiveCard_WhenExpiryDateIsFuture() {
        // Arrange
        // Используем anyString() вместо конкретного значения, так как номер генерируется случайно
        String encryptedNumber = "encrypted_1234";
        String maskedNumber = "**** **** **** 3456";

        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(cardEncryptor.encrypt(anyString())).thenReturn(encryptedNumber);
        when(cardNumberMasker.maskCardNumber(anyString())).thenReturn(maskedNumber);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        CardResponse result = cardService.createCard(cardRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).save(any(Card.class));
        verify(cardEncryptor).encrypt(anyString());
        verify(cardNumberMasker).maskCardNumber(anyString());
    }

    @Test
    void createCard_ShouldCreateExpiredCard_WhenExpiryDateIsPast() {
        // Arrange
        cardRequest.setExpiryDate(LocalDate.now().minusDays(1));
        testCard.setStatus(CardStatus.EXPIRED);
        cardResponse.setStatus(CardStatus.EXPIRED);

        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(cardEncryptor.encrypt(anyString())).thenReturn("encrypted");
        when(cardNumberMasker.maskCardNumber(anyString())).thenReturn("**** **** **** 1234");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        CardResponse result = cardService.createCard(cardRequest);

        // Assert
        assertThat(result.getStatus()).isEqualTo(CardStatus.EXPIRED);
    }

    @Test
    void createCard_ShouldGenerateAndEncryptCardNumber() {
        // Arrange
        String encryptedNumber = "encrypted_1234";
        String maskedNumber = "**** **** **** 3456";

        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(cardEncryptor.encrypt(anyString())).thenReturn(encryptedNumber);
        when(cardNumberMasker.maskCardNumber(anyString())).thenReturn(maskedNumber);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        cardService.createCard(cardRequest);

        // Assert
        verify(cardEncryptor).encrypt(argThat(number ->
                number.matches("\\d{4} \\d{4} \\d{4} \\d{4}"))); // Проверяем формат
        verify(cardNumberMasker).maskCardNumber(anyString());
    }

    @Test
    void createCard_ShouldSetCorrectCardStatusBasedOnExpiryDate() {
        // Arrange
        // Тест с будущей датой
        cardRequest.setExpiryDate(LocalDate.now().plusDays(1));
        testCard.setStatus(CardStatus.ACTIVE);

        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(cardEncryptor.encrypt(anyString())).thenReturn("encrypted");
        when(cardNumberMasker.maskCardNumber(anyString())).thenReturn("masked");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        CardResponse result = cardService.createCard(cardRequest);

        // Assert
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE);
    }

    // Остальные тесты остаются без изменений...

    @Test
    void updateCard_ShouldUpdateFieldsAndStatus_WhenExpiryDateChanges() {
        // Arrange
        LocalDate newExpiryDate = LocalDate.now().plusYears(3);
        cardRequest.setExpiryDate(newExpiryDate);

        Card updatedCard = Card.builder()
                .id(1L)
                .owner("JOHN SMITH")
                .expiryDate(newExpiryDate)
                .status(CardStatus.ACTIVE)
                .build();

        CardResponse updatedResponse = CardResponse.builder()
                .id(1L)
                .owner("JOHN SMITH")
                .expiryDate(newExpiryDate)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);
        when(cardMapper.toDto(updatedCard)).thenReturn(updatedResponse);

        // Act
        CardResponse result = cardService.updateCard(1L, cardRequest);

        // Assert
        assertThat(result.getOwner()).isEqualTo("JOHN SMITH");
        assertThat(result.getExpiryDate()).isEqualTo(newExpiryDate);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void updateCard_ShouldSetExpiredStatus_WhenNewExpiryDateIsPast() {
        // Arrange
        cardRequest.setExpiryDate(LocalDate.now().minusDays(1));
        testCard.setStatus(CardStatus.ACTIVE);

        Card updatedCard = Card.builder()
                .id(1L)
                .owner("JOHN DOE")
                .expiryDate(LocalDate.now().minusDays(1))
                .status(CardStatus.EXPIRED)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);
        when(cardMapper.toDto(updatedCard)).thenReturn(cardResponse);

        // Act
        cardService.updateCard(1L, cardRequest);

        // Assert
        verify(cardRepository).save(argThat(card ->
                card.getStatus() == CardStatus.EXPIRED));
    }

    @Test
    void requestToBlockCard_ShouldSetRequestedBlockTrue_WhenUserOwnsCard() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        CardResponse result = cardService.requestToBlockCard(1L);

        // Assert
        verify(cardRepository).save(argThat(card ->
                card.getRequestedBlock() == true));
    }

    @Test
    void requestToBlockCard_ShouldThrowAccessDenied_WhenUserDoesNotOwnCard() {
        // Arrange
        User anotherUser = User.builder().id(999L).build();
        when(userService.getCurrentUserEntity()).thenReturn(anotherUser);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThatThrownBy(() -> cardService.requestToBlockCard(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void blockCard_ShouldSetStatusToBlocked() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        cardService.blockCard(1L);

        // Assert
        verify(cardRepository).save(argThat(card ->
                card.getStatus() == CardStatus.BLOCKED));
    }

    @Test
    void activateCard_ShouldSetStatusToActive_WhenCardNotExpired() {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        cardService.activateCard(1L);

        // Assert
        verify(cardRepository).save(argThat(card ->
                card.getStatus() == CardStatus.ACTIVE));
    }

    @Test
    void activateCard_ShouldSetStatusToExpired_WhenCardIsExpired() {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        testCard.setExpiryDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        cardService.activateCard(1L);

        // Assert
        verify(cardRepository).save(argThat(card ->
                card.getStatus() == CardStatus.EXPIRED));
    }

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() {
        // Arrange
        when(cardRepository.existsById(1L)).thenReturn(true);

        // Act
        cardService.deleteCard(1L);

        // Assert
        verify(cardRepository).deleteById(1L);
    }

    @Test
    void deleteCard_ShouldThrowException_WhenCardNotExists() {
        // Arrange
        when(cardRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> cardService.deleteCard(1L))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void getCardById_ShouldReturnCard_WhenUserIsOwner() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        CardResponse result = cardService.getCardById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getCardById_ShouldReturnCard_WhenUserIsAdmin() {
        // Arrange
        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        CardResponse result = cardService.getCardById(1L);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void getCardById_ShouldThrowAccessDenied_WhenUserNotOwnerAndNotAdmin() {
        // Arrange
        User anotherUser = User.builder().id(999L).role(User.Role.USER).build();
        when(userService.getCurrentUserEntity()).thenReturn(anotherUser);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThatThrownBy(() -> cardService.getCardById(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUserCards_ShouldReturnAllCards_WhenUserIsAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));

        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        Page<CardResponse> result = cardService.getUserCards(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void getUserCards_ShouldReturnUserCards_WhenUserIsRegular() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(cardRepository.findByUserId(1L, pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        Page<CardResponse> result = cardService.getUserCards(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(cardRepository).findByUserId(1L, pageable);
    }

    @Test
    void updateBalance_ShouldIncreaseBalance_WhenIsCreditTrue() {
        // Arrange
        BigDecimal amount = BigDecimal.valueOf(500.00);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        cardService.updateBalance(1L, amount, true);

        // Assert
        verify(cardRepository).save(argThat(card ->
                card.getBalance().equals(BigDecimal.valueOf(1500.00))));
    }

    @Test
    void updateBalance_ShouldDecreaseBalance_WhenIsCreditFalse() {
        // Arrange
        BigDecimal amount = BigDecimal.valueOf(200.00);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        cardService.updateBalance(1L, amount, false);

        // Assert
        verify(cardRepository).save(argThat(card ->
                card.getBalance().equals(BigDecimal.valueOf(800.00))));
    }

    @Test
    void updateBalance_ShouldThrowException_WhenInsufficientFunds() {
        // Arrange
        BigDecimal amount = BigDecimal.valueOf(2000.00);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThatThrownBy(() -> cardService.updateBalance(1L, amount, false))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void isCardActive_ShouldReturnTrue_WhenCardIsActive() {
        // Arrange
        testCard.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        boolean result = cardService.isCardActive(1L);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isCardActive_ShouldReturnFalse_WhenCardIsBlocked() {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        boolean result = cardService.isCardActive(1L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void findCardEntityById_ShouldThrowException_WhenCardNotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.findCardEntityById(1L))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void createCard_ShouldUseCorrectCardNumberFormat() {
        // Arrange
        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(cardEncryptor.encrypt(anyString())).thenReturn("encrypted");
        when(cardNumberMasker.maskCardNumber(anyString())).thenReturn("masked");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(cardResponse);

        // Act
        cardService.createCard(cardRequest);

        // Assert
        verify(cardEncryptor).encrypt(argThat(number -> {
            // Убираем пробелы для проверки длины
            String cleanNumber = number.replace(" ", "");
            return cleanNumber.length() == 16 && cleanNumber.matches("\\d{16}");
        }));
    }
}