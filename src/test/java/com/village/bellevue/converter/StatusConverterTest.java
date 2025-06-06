package com.village.bellevue.converter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.village.bellevue.entity.ProfileEntity.Status;

public class StatusConverterTest {

  private final StatusConverter converter = new StatusConverter();

  public static Stream<Arguments> provideStatusForDatabaseColumn() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(Status.IDLE, "IDLE"),
        Arguments.of(Status.OFFLINE, "OFFLINE"),
        Arguments.of(Status.ACTIVE, "ACTIVE"));
  }

  public static Stream<Arguments> provideStringForEntityAttribute() {
    return Stream.of(
        Arguments.of(null, null, false),
        Arguments.of("IDLE", Status.IDLE, false),
        Arguments.of("OFFLINE", Status.OFFLINE, false),
        Arguments.of("ACTIVE", Status.ACTIVE, false),
        Arguments.of("invalid", null, true));
  }

  @ParameterizedTest
  @MethodSource("provideStatusForDatabaseColumn")
  void testConvertToDatabaseColumn(Status input, String expected) {
    String result = converter.convertToDatabaseColumn(input);
    assertEquals(expected, result, "Expected " + expected + " for " + input);
  }

  @ParameterizedTest
  @MethodSource("provideStringForEntityAttribute")
  void testConvertToEntityAttribute(String input, Status expected, boolean throwsException) {
    if (throwsException) {
      try {
        throw assertThrows(
            IllegalArgumentException.class,
            () -> {
              converter.convertToEntityAttribute(input);
            },
            "Expected IllegalArgumentException for invalid Status string: " + input);
      } catch (IllegalArgumentException e) {
        // Do nothing, this is expected
      }
    } else {
      Status result = converter.convertToEntityAttribute(input);
      assertEquals(expected, result, "Expected " + expected + " for " + input);
    }
  }
}
