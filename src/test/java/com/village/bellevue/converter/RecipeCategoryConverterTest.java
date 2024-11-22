package com.village.bellevue.converter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.village.bellevue.entity.RecipeEntity.RecipeCategory;

public class RecipeCategoryConverterTest {

    private final RecipeCategoryConverter converter = new RecipeCategoryConverter();

    public static Stream<Arguments> provideRecipeCategoryForDatabaseColumn() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(RecipeCategory.COCKTAIL, "cocktail"),
                Arguments.of(RecipeCategory.DESSERT, "dessert"),
                Arguments.of(RecipeCategory.MAIN, "main"),
                Arguments.of(RecipeCategory.SALAD, "salad"),
                Arguments.of(RecipeCategory.SIDE, "side"),
                Arguments.of(RecipeCategory.SMOOTHIE, "smoothie"),
                Arguments.of(RecipeCategory.SNACK, "snack"),
                Arguments.of(RecipeCategory.SOUP, "soup")
        );
    }

    public static Stream<Arguments> provideStringForEntityAttribute() {
        return Stream.of(
                Arguments.of(null, null, false),
                Arguments.of("cocktail", RecipeCategory.COCKTAIL, false),
                Arguments.of("dessert", RecipeCategory.DESSERT, false),
                Arguments.of("main", RecipeCategory.MAIN, false),
                Arguments.of("salad", RecipeCategory.SALAD, false),
                Arguments.of("side", RecipeCategory.SIDE, false),
                Arguments.of("smoothie", RecipeCategory.SMOOTHIE, false),
                Arguments.of("snack", RecipeCategory.SNACK, false),
                Arguments.of("soup", RecipeCategory.SOUP, false),
                Arguments.of("invalid", null, true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideRecipeCategoryForDatabaseColumn")
    void testConvertToDatabaseColumn(RecipeCategory input, String expected) {
        String result = converter.convertToDatabaseColumn(input);
        assertEquals(expected, result, "Expected " + expected + " for " + input);
    }

    @ParameterizedTest
    @MethodSource("provideStringForEntityAttribute")
    void testConvertToEntityAttribute(String input, RecipeCategory expected, boolean throwsException) {
        if (throwsException) {
            try {
                throw assertThrows(IllegalArgumentException.class, () -> {
                    converter.convertToEntityAttribute(input);
                }, "Expected IllegalArgumentException for invalid RecipeCategory string: " + input);
            } catch (IllegalArgumentException e) {
                // Do nothing, this is expected
            }
        } else {
            RecipeCategory result = converter.convertToEntityAttribute(input);
            assertEquals(expected, result, "Expected " + expected + " for " + input);
        }
    }
}
