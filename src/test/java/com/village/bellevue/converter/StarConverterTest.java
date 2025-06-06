package com.village.bellevue.converter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.village.bellevue.entity.RatingEntity.Star;

public class StarConverterTest {

  private final StarConverter converter = new StarConverter();

  public static Stream<Arguments> provideStarForDatabaseColumn() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(Star.FIVE, "FIVE"),
        Arguments.of(Star.FOUR, "FOUR"),
        Arguments.of(Star.THREE, "THREE"),
        Arguments.of(Star.TWO, "TWO"),
        Arguments.of(Star.ONE, "ONE"));
  }

  public static Stream<Arguments> provideStringForEntityAttribute() {
    return Stream.of(
        Arguments.of(null, null, false),
        Arguments.of("FIVE", Star.FIVE, false),
        Arguments.of("FOUR", Star.FOUR, false),
        Arguments.of("THREE", Star.THREE, false),
        Arguments.of("TWO", Star.TWO, false),
        Arguments.of("ONE", Star.ONE, false),
        Arguments.of("invalid", null, true));
  }

  @ParameterizedTest
  @MethodSource("provideStarForDatabaseColumn")
  void testConvertToDatabaseColumn(Star input, String expected) {
    String result = converter.convertToDatabaseColumn(input);
    assertEquals(expected, result, "Expected " + expected + " for " + input);
  }

  @ParameterizedTest
  @MethodSource("provideStringForEntityAttribute")
  void testConvertToEntityAttribute(String input, Star expected, boolean throwsException) {
    if (throwsException) {
      try {
        throw assertThrows(
            IllegalArgumentException.class,
            () -> {
              converter.convertToEntityAttribute(input);
            },
            "Expected IllegalArgumentException for invalid Star string: " + input);
      } catch (IllegalArgumentException e) {
        // Do nothing, this is expected
      }
    } else {
      Star result = converter.convertToEntityAttribute(input);
      assertEquals(expected, result, "Expected " + expected + " for " + input);
    }
  }
}
