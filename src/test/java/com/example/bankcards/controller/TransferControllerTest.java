package com.example.bankcards.controller;

import com.example.bankcards.config.JwtAuthenticationFilter;
import com.example.bankcards.config.SecurityTestConfig;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.exception.custom.card.SameCardTransferException;
import com.example.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = TransferController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class}
        )
)
@Import(SecurityTestConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    private TransferResponse createTransferResponse() {
        return TransferResponse.builder()
                .id(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transfer")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createTransfer_ShouldCreateTransfer() throws Exception {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transfer")
                .build();

        TransferResponse response = createTransferResponse();

        when(transferService.createTransfer(any(TransferRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromCardId").value(1))
                .andExpect(jsonPath("$.toCardId").value(2))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("Test transfer"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createTransfer_ShouldReturn400_WhenRequestInvalid() throws Exception {
        // Arrange - отрицательная сумма
        TransferRequest request = TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(-100.00))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getMyTransfers_ShouldReturnUserTransfers() throws Exception {
        // Arrange
        TransferResponse response = TransferResponse.builder()
                .id(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transfer")
                .createdAt(LocalDateTime.now()) // <-- ДОБАВЬТЕ ВСЕ ПОЛЯ
                .build();

        Page<TransferResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 10),
                1
        );

        when(transferService.getUserTransfers(any(PageRequest.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transfers/my")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].fromCardId").value(1));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getCardTransfers_ShouldReturnTransfersForCard() throws Exception {
        // Arrange
        TransferResponse response = TransferResponse.builder()
                .id(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100.00))
                .createdAt(LocalDateTime.now())
                .build();

        Page<TransferResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 10),
                1
        );

        when(transferService.getCardTransfers(eq(1L), any(PageRequest.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transfers/card/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getTransfer_ShouldReturnTransferById() throws Exception {
        // Arrange
        TransferResponse response = createTransferResponse();
        when(transferService.getTransferById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transfers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromCardId").value(1))
                .andExpect(jsonPath("$.toCardId").value(2));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createTransfer_ShouldValidateSameCardTransfer() throws Exception {
        // Arrange - перевод на ту же карту
        TransferRequest request = TransferRequest.builder()
                .fromCardId(1L)
                .toCardId(1L) // Same card
                .amount(BigDecimal.valueOf(100.00))
                .build();

        when(transferService.createTransfer(any(TransferRequest.class)))
                .thenThrow(new SameCardTransferException());

        // Act & Assert
        mockMvc.perform(post("/api/v1/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Cannot be transferred to the same card"));
    }
}