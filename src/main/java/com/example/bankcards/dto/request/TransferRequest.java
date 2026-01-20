package com.example.bankcards.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class TransferRequest {
    @NotNull(message = "Карта отправителя обязательна")
    private Long fromCardId;

    @NotNull(message = "Карта получателя обязательна")
    private Long toCardId;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @DecimalMax(value = "1000000", message = "Максимальная сумма перевода 1,000,000")
    private BigDecimal amount;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
}