package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private Long jwtExpiration = 86400000L; // 24 hours

    private User testUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        // Устанавливаем значения через reflection или создаем конструктор
        setField(jwtService, "secretKey", secretKey);
        setField(jwtService, "jwtExpiration", jwtExpiration);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        userDetails = new UserDetailsImpl(testUser);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateToken_ShouldCreateValidToken_ForUser() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT имеет 3 части
    }

    @Test
    void generateToken_ShouldCreateValidToken_ForUserDetailsImpl() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_ShouldExtractEmailFromToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForInvalidUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Создаем другого пользователя с ВСЕМИ обязательными полями
        User anotherUser = User.builder()
                .id(2L)
                .email("another@example.com")
                .password("password")
                .firstName("Another")
                .lastName("User")
                .role(User.Role.USER) // <-- ВАЖНО: роль не должна быть null
                .build();

        UserDetailsImpl anotherUserDetails = new UserDetailsImpl(anotherUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, anotherUserDetails);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_ForValidToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertThat(isExpired).isFalse();
    }

    @Test
    void token_ShouldContainCorrectClaims() {
        // Act
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_ShouldContainSubject() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String subject = jwtService.extractUsername(token);

        // Assert
        assertThat(subject).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_ShouldBeValidForOriginalUser() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void extractClaims_ShouldWork() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("test@example.com");
    }
}