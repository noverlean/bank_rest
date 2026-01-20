package com.example.bankcards.service;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferService {
    TransferResponse createTransfer(TransferRequest request);
    Page<TransferResponse> getUserTransfers(Pageable pageable);
    Page<TransferResponse> getCardTransfers(Long cardId, Pageable pageable);
    TransferResponse getTransferById(Long transferId);
}