package com.village.bellevue.converter;

import com.village.bellevue.entity.ReviewEntity.ReviewRating;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReviewRatingConverter implements AttributeConverter<ReviewRating, String> {
    @Override
    public String convertToDatabaseColumn(ReviewRating value) {
        return value != null ? value.toString().toLowerCase() : null;
    }

    @Override
    public ReviewRating convertToEntityAttribute(String value) {
        return value != null ? ReviewRating.valueOf(value.toUpperCase()) : null;
    }
}
