package com.village.bellevue.converter;

import com.village.bellevue.entity.UserEntity.AvatarType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AvatarTypeConverter implements AttributeConverter<AvatarType, String> {
  @Override
  public String convertToDatabaseColumn(AvatarType value) {
    return value != null ? value.toString().toLowerCase() : null;
  }

  @Override
  public AvatarType convertToEntityAttribute(String value) {
    return value != null ? AvatarType.valueOf(value.toUpperCase()) : null;
  }
}
