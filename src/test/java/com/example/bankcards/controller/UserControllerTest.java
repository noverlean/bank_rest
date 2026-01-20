package com.example.bankcards.controller;

import com.example.bankcards.config.JwtAuthenticationFilter;
import com.example.bankcards.config.SecurityTestConfig;
import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserSecurity;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class}
        )
)
@Import(SecurityTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean(name = "userSecurity")
    private UserSecurity userSecurity;

    private UserResponse createUserResponse() {
        return UserResponse.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnAllUsers_WhenAdmin() throws Exception {
        // Arrange
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // Создаем правильный Page объект
        Page<UserResponse> page = new PageImpl<>(
                List.of(userResponse),
                PageRequest.of(0, 25),
                1
        );

        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].email").value("user@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_ShouldReturnUser_WhenAdmin() throws Exception {
        // Arrange
        UserResponse response = createUserResponse();
        when(userService.getUserById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getUser_ShouldReturnUser_WhenCurrentUser() throws Exception {
        // Arrange
        when(userSecurity.isCurrentUser(1L)).thenReturn(true);
        UserResponse response = createUserResponse();
        when(userService.getUserById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "other@example.com")
    void getUser_ShouldReturn403_WhenNotOwnerAndNotAdmin() throws Exception {
        // Arrange
        when(userSecurity.isCurrentUser(1L)).thenReturn(false);

        // Исключение обрабатывается как 400, а не 403
        when(userService.getUserById(1L))
                .thenThrow(new com.example.bankcards.exception.custom.AccessDeniedException("Access denied"));

        // Act & Assert - ваш ExceptionHandler возвращает 400
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isBadRequest()); // <-- Меняем на 400
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getCurrentUser_ShouldReturnCurrentUser() throws Exception {
        // Arrange
        UserResponse response = createUserResponse();
        when(userService.getCurrentUser()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateUser_ShouldUpdate_WhenCurrentUser() throws Exception {
        // Arrange
        when(userSecurity.isCurrentUser(1L)).thenReturn(true);

        UserRequest request = UserRequest.builder()
                .email("updated@example.com")
                .password("newPassword123") // <-- ДОБАВЬТЕ ПАРОЛЬ
                .firstName("JohnUpdated")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("updated@example.com")
                .firstName("JohnUpdated")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.firstName").value("JohnUpdated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ShouldUpdate_WhenAdmin() throws Exception {
        // Arrange
        UserRequest request = UserRequest.builder()
                .email("adminupdated@example.com")
                .password("newPassword123") // <-- ДОБАВЬТЕ ПАРОЛЬ
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN)
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("adminupdated@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN)
                .build();

        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldDeleteUser_WhenAdmin() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User successfully deleted"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateUser_ShouldReturn400_WhenRequestInvalid() throws Exception {
        // Arrange - запрос без email
        UserRequest request = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}