package com.village.bellevue.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.village.bellevue.entity.UserEntity.UserStatus;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UserStatusConverterTest {

  private final UserStatusConverter converter = new UserStatusConverter();

  public static Stream<Arguments> provideUserStatusForDatabaseColumn() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(UserStatus.COOKING, "cooking"),
        Arguments.of(UserStatus.OFFLINE, "offline"),
        Arguments.of(UserStatus.ONLINE, "online"));
  }

  public static Stream<Arguments> provideStringForEntityAttribute() {
    return Stream.of(
        Arguments.of(null, null, false),
        Arguments.of("cooking", UserStatus.COOKING, false),
        Arguments.of("offline", UserStatus.OFFLINE, false),
        Arguments.of("online", UserStatus.ONLINE, false),
        Arguments.of("invalid", null, true));
  }

  @ParameterizedTest
  @MethodSource("provideUserStatusForDatabaseColumn")
  void testConvertToDatabaseColumn(UserStatus input, String expected) {
    String result = converter.convertToDatabaseColumn(input);
    assertEquals(expected, result, "Expected " + expected + " for " + input);
  }

  @ParameterizedTest
  @MethodSource("provideStringForEntityAttribute")
  void testConvertToEntityAttribute(String input, UserStatus expected, boolean throwsException) {
    if (throwsException) {
      try {
        throw assertThrows(
            IllegalArgumentException.class,
            () -> {
              converter.convertToEntityAttribute(input);
            },
            "Expected IllegalArgumentException for invalid UserStatus string: " + input);
      } catch (IllegalArgumentException e) {
        // Do nothing, this is expected
      }
    } else {
      UserStatus result = converter.convertToEntityAttribute(input);
      assertEquals(expected, result, "Expected " + expected + " for " + input);
    }
  }
}
