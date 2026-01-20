package com.example.bankcards.controller;

import com.example.bankcards.api.TransferApi;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransferController implements TransferApi {

    private final TransferService transferService;

    @Override
    public TransferResponse createTransfer(@Valid @RequestBody TransferRequest request) {
        return transferService.createTransfer(request);
    }

    @Override
    public Page<TransferResponse> getMyTransfers(@PageableDefault Pageable pageable) {
        return transferService.getUserTransfers(pageable);
    }

    @Override
    public Page<TransferResponse> getCardTransfers(
            @PathVariable Long cardId,
            @PageableDefault Pageable pageable) {
        return transferService.getCardTransfers(cardId, pageable);
    }

    @Override
    public TransferResponse getTransfer(@PathVariable Long transferId) {
        return transferService.getTransferById(transferId);
    }
}