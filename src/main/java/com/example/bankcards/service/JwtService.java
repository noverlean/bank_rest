package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);
    String generateToken(UserDetails userDetails);
    String generateToken(User user);
    String generateToken(UserDetailsImpl userDetails); // Добавляем этот метод
    boolean isTokenValid(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
}