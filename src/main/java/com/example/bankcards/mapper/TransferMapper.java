package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.entity.Transfer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    TransferResponse toDto(Transfer transfer);
    Transfer toModel(TransferResponse response);
}
