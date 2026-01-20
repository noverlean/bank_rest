package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.custom.user.UserNotFoundException;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.JwtService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public JwtResponse login(LoginRequest request) {
        userService.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtService.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .role(userDetails.getRole())
                .build();

        return response;
    }

    @Override
    @Transactional
    public UserResponse register(UserRequest request) {
        return userService.register(request);
    }
}
