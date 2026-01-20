package com.example.bankcards.service;

import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.custom.user.UserAlreadyExistsException;
import com.example.bankcards.exception.custom.user.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import com.example.bankcards.util.CurrentUserProvider;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        userRequest = UserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail("test@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenUserNotExists() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getUserEntityById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserEntityById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserEntityById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserEntityById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void register_ShouldCreateUser_WhenEmailIsUnique() {
        // Arrange
        User newUser = User.builder()
                .email("new@example.com")
                .password("encodedPassword")
                .firstName("Jane")
                .lastName("Smith")
                .role(User.Role.USER)
                .build();

        UserResponse newUserResponse = UserResponse.builder()
                .id(2L)
                .email("new@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .role(User.Role.USER)
                .build();

        UserRequest newUserRequest = UserRequest.builder()
                .email("new@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Smith")
                .role(User.Role.USER)
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDto(newUser)).thenReturn(newUserResponse);

        // Act
        UserResponse result = userService.register(newUserRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        userRequest.setEmail("existing@example.com");

        // Act & Assert
        assertThatThrownBy(() -> userService.register(userRequest))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void register_ShouldEncodePassword() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false); // <-- anyString()
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(userResponse);

        // Act
        userService.register(userRequest); // userRequest из setUp()

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedPassword123")));
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(testUser)).thenReturn(userResponse);

        // Act
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenEmailIsUnique() {
        // Arrange
        UserRequest updateRequest = UserRequest.builder()
                .email("updated@example.com")
                .password("newPassword123")
                .firstName("JohnUpdated")
                .lastName("DoeUpdated")
                .role(User.Role.ADMIN)
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .email("updated@example.com")
                .password("encodedNewPassword")
                .firstName("JohnUpdated")
                .lastName("DoeUpdated")
                .role(User.Role.ADMIN)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .email("updated@example.com")
                .firstName("JohnUpdated")
                .lastName("DoeUpdated")
                .role(User.Role.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedResponse);

        // Act
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Assert
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getFirstName()).isEqualTo("JohnUpdated");
        assertThat(result.getRole()).isEqualTo(User.Role.ADMIN);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedNewPassword")));
    }

    @Test
    void updateUser_ShouldNotUpdatePassword_WhenPasswordIsNull() {
        // Arrange
        UserRequest updateRequest = UserRequest.builder()
                .email("test@example.com")
                .password(null)
                .firstName("JohnUpdated")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(userResponse);

        // Act
        userService.updateUser(1L, updateRequest);

        // Assert
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedPassword")));
    }

    @Test
    void updateUser_ShouldNotUpdatePassword_WhenPasswordIsEmpty() {
        // Arrange
        UserRequest updateRequest = UserRequest.builder()
                .email("test@example.com")
                .password("")
                .firstName("JohnUpdated")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(userResponse);

        // Act
        userService.updateUser(1L, updateRequest);

        // Assert
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_ShouldThrowException_WhenNewEmailAlreadyExists() {
        // Arrange
        UserRequest updateRequest = UserRequest.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail("test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserByEmail_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getCurrentUserEntity_ShouldReturnCurrentUser() {
        // Arrange
        String currentEmail = "current@example.com";
        User currentUser = User.builder()
                .id(3L)
                .email(currentEmail)
                .build();

        try (var mockedStatic = mockStatic(CurrentUserProvider.class)) {
            mockedStatic.when(CurrentUserProvider::getCurrentEmail).thenReturn(currentEmail);
            when(userRepository.findByEmail(currentEmail)).thenReturn(Optional.of(currentUser));

            // Act
            User result = userService.getCurrentUserEntity();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(currentEmail);
        }
    }

    @Test
    void getCurrentUser_ShouldReturnCurrentUserResponse() {
        // Arrange
        String currentEmail = "current@example.com";
        User currentUser = User.builder()
                .id(3L)
                .email(currentEmail)
                .build();

        UserResponse currentUserResponse = UserResponse.builder()
                .id(3L)
                .email(currentEmail)
                .build();

        try (var mockedStatic = mockStatic(CurrentUserProvider.class)) {
            mockedStatic.when(CurrentUserProvider::getCurrentEmail).thenReturn(currentEmail);
            when(userRepository.findByEmail(currentEmail)).thenReturn(Optional.of(currentUser));
            when(userMapper.toDto(currentUser)).thenReturn(currentUserResponse);

            // Act
            UserResponse result = userService.getCurrentUser();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(currentEmail);
        }
    }

    @Test
    void updateUser_ShouldAllowSameEmail_WhenEmailNotChanged() {
        // Arrange
        UserRequest updateRequest = UserRequest.builder()
                .email("test@example.com") // Same as existing
                .firstName("JohnUpdated")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(userResponse);

        // Act & Assert - should not throw exception
        UserResponse result = userService.updateUser(1L, updateRequest);
        assertThat(result).isNotNull();
    }

    @Test
    void register_ShouldUseDefaultRole_WhenRoleNotProvided() {
        // Arrange
        UserRequest requestWithoutRole = UserRequest.builder()
                .email("new@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Smith")
                .build(); // No role specified

        User newUser = User.builder()
                .email("new@example.com")
                .password("encodedPassword")
                .firstName("Jane")
                .lastName("Smith")
                .role(User.Role.USER) // Should default to USER
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDto(newUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.register(requestWithoutRole);

        // Assert
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
    }
}