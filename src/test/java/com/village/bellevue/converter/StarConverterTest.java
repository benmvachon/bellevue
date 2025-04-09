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
        Arguments.of(Star.FIVE, "five"),
        Arguments.of(Star.FOUR, "four"),
        Arguments.of(Star.THREE, "three"),
        Arguments.of(Star.TWO, "two"),
        Arguments.of(Star.ONE, "one"));
  }

  public static Stream<Arguments> provideStringForEntityAttribute() {
    return Stream.of(
        Arguments.of(null, null, false),
        Arguments.of("five", Star.FIVE, false),
        Arguments.of("four", Star.FOUR, false),
        Arguments.of("three", Star.THREE, false),
        Arguments.of("two", Star.TWO, false),
        Arguments.of("one", Star.ONE, false),
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
