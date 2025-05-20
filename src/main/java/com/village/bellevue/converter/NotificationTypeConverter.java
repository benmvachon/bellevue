package com.village.bellevue.converter;

import com.village.bellevue.entity.NotificationEntity.NotificationType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {
  @Override
  public String convertToDatabaseColumn(NotificationType value) {
    return value != null ? value.toString().toUpperCase() : null;
  }

  @Override
  public NotificationType convertToEntityAttribute(String value) {
    return value != null ? NotificationType.valueOf(value.toUpperCase()) : null;
  }
}
