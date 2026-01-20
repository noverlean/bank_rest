package com.example.bankcards.controller;

import com.example.bankcards.api.UserApi;
import com.example.bankcards.dto.request.UserRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public Page<UserResponse> getAllUsers(@PageableDefault(size = 25) Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @Override
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Override
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Override
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @Override
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User successfully deleted");
    }
}