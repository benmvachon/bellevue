package com.village.bellevue.converter;

import com.village.bellevue.entity.FriendEntity.FriendshipStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FriendshipStatusConverter implements AttributeConverter<FriendshipStatus, String> {
    @Override
    public String convertToDatabaseColumn(FriendshipStatus value) {
        return value != null ? value.toString().toLowerCase() : null;
    }

    @Override
    public FriendshipStatus convertToEntityAttribute(String value) {
        return value != null ? FriendshipStatus.valueOf(value.toUpperCase()) : null;
    }
}
