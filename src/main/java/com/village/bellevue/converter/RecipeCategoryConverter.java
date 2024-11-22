package com.village.bellevue.converter;

import com.village.bellevue.entity.RecipeEntity.RecipeCategory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RecipeCategoryConverter implements AttributeConverter<RecipeCategory, String> {
    @Override
    public String convertToDatabaseColumn(RecipeCategory value) {
        return value != null ? value.toString().toLowerCase() : null;
    }

    @Override
    public RecipeCategory convertToEntityAttribute(String value) {
        return value != null ? RecipeCategory.valueOf(value.toUpperCase()) : null;
    }
}
