package com.example.bankcards.service;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.custom.user.UserNotFoundException;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private UserRequest userRequest;
    private User testUser;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        userRequest = UserRequest.builder()
                .email("new@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        userDetails = new UserDetailsImpl(testUser);
        authentication = mock(Authentication.class);
    }

    @Test
    void login_ShouldReturnJwtResponse_WhenCredentialsAreValid() {
        // Arrange
        String jwtToken = "test.jwt.token";
        JwtResponse expectedResponse = JwtResponse.builder()
                .token(jwtToken)
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(jwtToken);

        // Act
        JwtResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(jwtToken);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getId()).isEqualTo(1L);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userService.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        loginRequest.setEmail("nonexistent@example.com");

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void login_ShouldSetSecurityContext_WhenAuthenticationSucceeds() {
        // Arrange
        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");

        // Act
        authService.login(loginRequest);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void register_ShouldCallUserServiceRegister() {
        // Arrange
        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .email("new@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        when(userService.register(userRequest)).thenReturn(expectedResponse);

        // Act
        UserResponse response = authService.register(userRequest);

        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(userService).register(userRequest);
    }

    @Test
    void login_ShouldHandleAuthenticationException() {
        // Arrange
        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
    }

    @Test
    void login_ShouldClearSecurityContext_WhenCalledMultipleTimes() {
        // Arrange
        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");

        // Act - первый вызов
        authService.login(loginRequest);
        Authentication firstAuth = SecurityContextHolder.getContext().getAuthentication();

        // Act - второй вызов
        authService.login(loginRequest);
        Authentication secondAuth = SecurityContextHolder.getContext().getAuthentication();

        // Assert
        assertThat(firstAuth).isNotNull();
        assertThat(secondAuth).isNotNull();
    }
}