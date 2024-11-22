package com.village.bellevue.converter;

import com.village.bellevue.entity.UserEntity.UserStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {
    @Override
    public String convertToDatabaseColumn(UserStatus value) {
        return value != null ? value.toString().toLowerCase() : null;
    }

    @Override
    public UserStatus convertToEntityAttribute(String value) {
        return value != null ? UserStatus.valueOf(value.toUpperCase()) : null;
    }
}
