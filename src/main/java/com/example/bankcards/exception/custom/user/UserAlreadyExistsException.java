package com.example.bankcards.exception.custom.user;

import com.example.bankcards.exception.custom.ConflictException;

public class UserAlreadyExistsException extends ConflictException {
    public UserAlreadyExistsException() { super("Пользователь с таким email уже существует"); }
    public UserAlreadyExistsException(String message) { super(message); }
}
