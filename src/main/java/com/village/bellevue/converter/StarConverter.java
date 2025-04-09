package com.village.bellevue.converter;

import com.village.bellevue.entity.RatingEntity.Star;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StarConverter implements AttributeConverter<Star, String> {
  @Override
  public String convertToDatabaseColumn(Star value) {
    return value != null ? value.toString().toLowerCase() : null;
  }

  @Override
  public Star convertToEntityAttribute(String value) {
    return value != null ? Star.valueOf(value.toUpperCase()) : null;
  }
}
