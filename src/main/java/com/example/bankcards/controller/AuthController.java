package com.example.bankcards.controller;

import com.example.bankcards.api.AuthApi;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final AuthService authService;

    @Override
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }


    @Override
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return authService.register(request);
    }
}