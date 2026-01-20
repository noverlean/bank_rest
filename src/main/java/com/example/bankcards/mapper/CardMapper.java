package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardResponse toDto(Card card);
    Card toModel(CardResponse response);
}
