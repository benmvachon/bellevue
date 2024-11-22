package com.village.bellevue.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.village.bellevue.entity.UserEntity.AvatarType;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AvatarTypeConverterTest {

  private final AvatarTypeConverter converter = new AvatarTypeConverter();

  public static Stream<Arguments> provideAvatarTypeForDatabaseColumn() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(AvatarType.BEE, "bee"),
        Arguments.of(AvatarType.CAT, "cat"),
        Arguments.of(AvatarType.HORSE, "horse"),
        Arguments.of(AvatarType.MONKEY, "monkey"),
        Arguments.of(AvatarType.RAPTOR, "raptor"),
        Arguments.of(AvatarType.WALRUS, "walrus"));
  }

  public static Stream<Arguments> provideStringForEntityAttribute() {
    return Stream.of(
        Arguments.of(null, null, false),
        Arguments.of("bee", AvatarType.BEE, false),
        Arguments.of("cat", AvatarType.CAT, false),
        Arguments.of("horse", AvatarType.HORSE, false),
        Arguments.of("monkey", AvatarType.MONKEY, false),
        Arguments.of("raptor", AvatarType.RAPTOR, false),
        Arguments.of("walrus", AvatarType.WALRUS, false),
        Arguments.of("invalid", null, true));
  }

  @ParameterizedTest
  @MethodSource("provideAvatarTypeForDatabaseColumn")
  void testConvertToDatabaseColumn(AvatarType input, String expected) {
    String result = converter.convertToDatabaseColumn(input);
    assertEquals(expected, result, "Expected " + expected + " for " + input);
  }

  @ParameterizedTest
  @MethodSource("provideStringForEntityAttribute")
  void testConvertToEntityAttribute(String input, AvatarType expected, boolean throwsException) {
    if (throwsException) {
      try {
        throw assertThrows(
            IllegalArgumentException.class,
            () -> {
              converter.convertToEntityAttribute(input);
            },
            "Expected IllegalArgumentException for invalid AvatarType string: " + input);
      } catch (IllegalArgumentException e) {
        // Do nothing, this is expected
      }
    } else {
      AvatarType result = converter.convertToEntityAttribute(input);
      assertEquals(expected, result, "Expected " + expected + " for " + input);
    }
  }
}
