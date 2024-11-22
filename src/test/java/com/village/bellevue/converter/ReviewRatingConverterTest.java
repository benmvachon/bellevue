package com.village.bellevue.converter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.village.bellevue.entity.ReviewEntity.ReviewRating;

public class ReviewRatingConverterTest {

    private final ReviewRatingConverter converter = new ReviewRatingConverter();

    public static Stream<Arguments> provideReviewRatingForDatabaseColumn() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(ReviewRating.ADMIRABLY, "admirably"),
                Arguments.of(ReviewRating.DISASTROUSLY, "disastrously"),
                Arguments.of(ReviewRating.JUBILANTLY, "jubilantly"),
                Arguments.of(ReviewRating.MUDDLINGLY, "muddlingly"),
                Arguments.of(ReviewRating.SPLENDIDLY, "splendidly")
        );
    }

    public static Stream<Arguments> provideStringForEntityAttribute() {
        return Stream.of(
                Arguments.of(null, null, false),
                Arguments.of("admirably", ReviewRating.ADMIRABLY, false),
                Arguments.of("disastrously", ReviewRating.DISASTROUSLY, false),
                Arguments.of("jubilantly", ReviewRating.JUBILANTLY, false),
                Arguments.of("muddlingly", ReviewRating.MUDDLINGLY, false),
                Arguments.of("splendidly", ReviewRating.SPLENDIDLY, false),
                Arguments.of("invalid", null, true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideReviewRatingForDatabaseColumn")
    void testConvertToDatabaseColumn(ReviewRating input, String expected) {
        String result = converter.convertToDatabaseColumn(input);
        assertEquals(expected, result, "Expected " + expected + " for " + input);
    }

    @ParameterizedTest
    @MethodSource("provideStringForEntityAttribute")
    void testConvertToEntityAttribute(String input, ReviewRating expected, boolean throwsException) {
        if (throwsException) {
            try {
                throw assertThrows(IllegalArgumentException.class, () -> {
                    converter.convertToEntityAttribute(input);
                }, "Expected IllegalArgumentException for invalid ReviewRating string: " + input);
            } catch (IllegalArgumentException e) {
                // Do nothing, this is expected
            }
        } else {
            ReviewRating result = converter.convertToEntityAttribute(input);
            assertEquals(expected, result, "Expected " + expected + " for " + input);
        }
    }
}
