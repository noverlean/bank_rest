package com.example.bankcards.api;

import com.example.bankcards.dto.request.CardRequest;
import com.example.bankcards.dto.response.CardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/cards")
@Tag(name = "Карты", description = "Управление банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public interface CardApi {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую карту")
    CardResponse createCard(@Valid @RequestBody CardRequest request);

    @PatchMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить данные карты")
    CardResponse updateCard(@PathVariable Long cardId, @Valid @RequestBody CardRequest request);

    @GetMapping("/my")
    @Operation(summary = "Получить мои карты")
    Page<CardResponse> getMyCards(@PageableDefault(size = 10) Pageable pageable);

    @GetMapping("/{cardId}")
    @Operation(summary = "Получить карту по ID")
    CardResponse getCard(@PathVariable Long cardId);

    @PatchMapping("/{cardId}/request-block")
    @Operation(summary = "Подать заявку на блокировку карты")
    CardResponse requestToBlockCard(@PathVariable Long cardId);

    @PatchMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать карту")
    CardResponse blockCard(@PathVariable Long cardId);

    @PatchMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать карту")
    CardResponse activateCard(@PathVariable Long cardId);

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту")
    ResponseEntity<String> deleteCard(@PathVariable Long cardId);

    @GetMapping("/search")
    @Operation(summary = "Поиск карт")
    Page<CardResponse> searchCards(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable
    );

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все карты (только для админа)")
    Page<CardResponse> getAllCards(@PageableDefault(size = 20) Pageable pageable);
}
