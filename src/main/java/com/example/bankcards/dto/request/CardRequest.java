package com.example.bankcards.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CardRequest {
    @NotBlank(message = "Владелец обязателен")
    @Size(min = 2, max = 100, message = "Имя владельца должно быть от 2 до 100 символов")
    private String owner;

    @NotNull(message = "Дата окончания срока обязательна")
    @Future(message = "Дата окончания срока должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.0", inclusive = false, message = "Баланс должен быть положительным")
    private BigDecimal balance;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;
}