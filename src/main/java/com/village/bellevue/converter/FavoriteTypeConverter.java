package com.village.bellevue.converter;

import com.village.bellevue.entity.FavoriteEntity.FavoriteType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FavoriteTypeConverter implements AttributeConverter<FavoriteType, String> {
  @Override
  public String convertToDatabaseColumn(FavoriteType value) {
    return value != null ? value.toString().toUpperCase() : null;
  }

  @Override
  public FavoriteType convertToEntityAttribute(String value) {
    return value != null ? FavoriteType.valueOf(value.toUpperCase()) : null;
  }
}
