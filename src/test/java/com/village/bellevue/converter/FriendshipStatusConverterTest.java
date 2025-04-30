package com.village.bellevue.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.village.bellevue.entity.FriendEntity.FriendshipStatus;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FriendshipStatusConverterTest {

  private final FriendshipStatusConverter converter = new FriendshipStatusConverter();

  public static Stream<Arguments> provideFriendshipStatusForDatabaseColumn() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(FriendshipStatus.ACCEPTED, "ACCEPTED"),
        Arguments.of(FriendshipStatus.BLOCKED_THEM, "BLOCKED_THEM"),
        Arguments.of(FriendshipStatus.BLOCKED_YOU, "BLOCKED_YOU"),
        Arguments.of(FriendshipStatus.PENDING_THEM, "PENDING_THEM"),
        Arguments.of(FriendshipStatus.PENDING_YOU, "PENDING_YOU"));
  }

  public static Stream<Arguments> provideStringForEntityAttribute() {
    return Stream.of(
        Arguments.of(null, null, false),
        Arguments.of("ACCEPTED", FriendshipStatus.ACCEPTED, false),
        Arguments.of("BLOCKED_THEM", FriendshipStatus.BLOCKED_THEM, false),
        Arguments.of("BLOCKED_YOU", FriendshipStatus.BLOCKED_YOU, false),
        Arguments.of("PENDING_THEM", FriendshipStatus.PENDING_THEM, false),
        Arguments.of("PENDING_YOU", FriendshipStatus.PENDING_YOU, false),
        Arguments.of("invalid", null, true));
  }

  @ParameterizedTest
  @MethodSource("provideFriendshipStatusForDatabaseColumn")
  void testConvertToDatabaseColumn(FriendshipStatus input, String expected) {
    String result = converter.convertToDatabaseColumn(input);
    assertEquals(expected, result, "Expected " + expected + " for " + input);
  }

  @ParameterizedTest
  @MethodSource("provideStringForEntityAttribute")
  void testConvertToEntityAttribute(
      String input, FriendshipStatus expected, boolean throwsException) {
    if (throwsException) {
      try {
        throw assertThrows(
            IllegalArgumentException.class,
            () -> {
              converter.convertToEntityAttribute(input);
            },
            "Expected IllegalArgumentException for invalid FriendshipStatus string: " + input);
      } catch (IllegalArgumentException e) {
        // Do nothing, this is expected
      }
    } else {
      FriendshipStatus result = converter.convertToEntityAttribute(input);
      assertEquals(expected, result, "Expected " + expected + " for " + input);
    }
  }
}
