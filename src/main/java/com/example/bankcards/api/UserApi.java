package com.example.bankcards.api;

import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Пользователи", description = "Управление пользователями")
public interface UserApi {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить всех пользователей (только для админа)")
    Page<UserResponse> getAllUsers(@PageableDefault(size = 25) Pageable pageable);

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    @Operation(summary = "Получить пользователя по ID")
    UserResponse getUser(@PathVariable Long id);

    @GetMapping("/me")
    @Operation(summary = "Получить информацию о текущем пользователе")
    UserResponse getCurrentUser();

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    @Operation(summary = "Обновить пользователя")
    UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    );

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить пользователя (только для админа)")
    ResponseEntity<?> deleteUser(@PathVariable Long id);
}
