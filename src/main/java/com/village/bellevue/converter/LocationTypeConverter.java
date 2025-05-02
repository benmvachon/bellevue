package com.village.bellevue.converter;

import com.village.bellevue.entity.ProfileEntity.LocationType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LocationTypeConverter implements AttributeConverter<LocationType, String> {
  @Override
  public String convertToDatabaseColumn(LocationType value) {
    return value != null ? value.toString().toUpperCase() : null;
  }

  @Override
  public LocationType convertToEntityAttribute(String value) {
    return value != null ? LocationType.valueOf(value.toUpperCase()) : null;
  }
}
