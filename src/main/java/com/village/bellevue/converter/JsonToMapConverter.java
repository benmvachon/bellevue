package com.village.bellevue.converter;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonToMapConverter implements AttributeConverter<Map<String, String>, String> {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, String> attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, String> convertToEntityAttribute(String dbData) {
    try {
      return objectMapper.readValue(dbData, Map.class);
    } catch (JsonProcessingException e) {
      return new HashMap<>();
    }
  }
}