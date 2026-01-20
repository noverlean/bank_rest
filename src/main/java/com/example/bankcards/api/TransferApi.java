package com.example.bankcards.api;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/transfers")
@Tag(name = "Переводы", description = "Управление переводами между картами")
@SecurityRequirement(name = "bearerAuth")
public interface TransferApi {

    @PostMapping
    @Operation(summary = "Создать перевод между картами")
    TransferResponse createTransfer(@Valid @RequestBody TransferRequest request);

    @GetMapping("/my")
    @Operation(summary = "Получить мои переводы")
    Page<TransferResponse> getMyTransfers(@PageableDefault Pageable pageable);

    @GetMapping("/card/{cardId}")
    @Operation(summary = "Получить переводы по карте")
    Page<TransferResponse> getCardTransfers(
            @PathVariable Long cardId,
            @PageableDefault Pageable pageable
    );

    @GetMapping("/{transferId}")
    @Operation(summary = "Получить перевод по ID")
    TransferResponse getTransfer(@PathVariable Long transferId);
}
