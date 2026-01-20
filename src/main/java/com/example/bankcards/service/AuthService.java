package com.example.bankcards.service;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.UserResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    UserResponse register(UserRequest request);
}
