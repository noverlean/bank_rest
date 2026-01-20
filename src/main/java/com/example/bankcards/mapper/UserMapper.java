package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);
    User toModel(UserResponse response);
}
