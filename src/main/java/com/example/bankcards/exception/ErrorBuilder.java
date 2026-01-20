package com.example.bankcards.exception;

import com.example.bankcards.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public class ErrorBuilder {

    static ErrorResponse buildError(HttpStatus status, String code, String message, HttpServletRequest request) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI()
        );
    }
}

