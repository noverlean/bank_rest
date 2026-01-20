package com.example.bankcards.api;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/auth")
@Tag(name = "Аутентификация", description = "Регистрация и аутентификация пользователей")
public interface AuthApi {
    @Operation(summary = "Вход в систему")
    @PostMapping("/login")
    JwtResponse login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    UserResponse register(@Valid @RequestBody UserRequest request);
}
