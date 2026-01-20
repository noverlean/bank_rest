package com.example.bankcards.service;

import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    UserResponse register(UserRequest request);
    User getUserEntityById(Long id);
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
    User getUserByEmail(String email);
    User getCurrentUserEntity();
    UserResponse getCurrentUser();
}