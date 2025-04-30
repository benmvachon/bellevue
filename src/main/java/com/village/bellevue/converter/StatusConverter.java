package com.village.bellevue.converter;

import com.village.bellevue.entity.ProfileEntity.Status;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {
  @Override
  public String convertToDatabaseColumn(Status value) {
    return value != null ? value.toString().toUpperCase() : null;
  }

  @Override
  public Status convertToEntityAttribute(String value) {
    return value != null ? Status.valueOf(value.toUpperCase()) : null;
  }
}
