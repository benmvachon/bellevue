package com.village.bellevue.service;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.village.bellevue.config.security.SecurityConfig;
import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.FriendEntity.FriendshipStatus;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.service.impl.FriendServiceImpl;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {

  @InjectMocks private FriendServiceImpl friendService;

  @Mock private FriendRepository friendRepository;

  private final ScrubbedUserEntity currentUser =
      new ScrubbedUserEntity(1L, "Foo", "foo", UserStatus.ONLINE, AvatarType.BEE);
  private final ScrubbedUserEntity friend =
      new ScrubbedUserEntity(2L, "Bar", "bar", UserStatus.ONLINE, AvatarType.WALRUS);

  public static Stream<Arguments> friendshipStatusProvider() {
    return Stream.of(
        Arguments.of(FriendshipStatus.ACCEPTED, true, false),
        Arguments.of(FriendshipStatus.PENDING_THEM, false, false),
        Arguments.of(FriendshipStatus.BLOCKED_YOU, false, true));
  }

  @ParameterizedTest
  @MethodSource("friendshipStatusProvider")
  public void testFriendshipStatus(
      FriendshipStatus status, boolean expectedIsFriend, boolean expectedIsBlockedBy)
      throws FriendshipException {
    FriendEntity friendship = new FriendEntity(currentUser.getId(), friend, status, null, null);

    try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
      mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(currentUser.getId());
      when(friendRepository.findById(any())).thenReturn(Optional.of(friendship));

      assertEquals(expectedIsFriend, friendService.isFriend(friend.getId()));
      assertEquals(expectedIsBlockedBy, friendService.isBlockedBy(friend.getId()));

      Optional<String> retrievedStatus = friendService.getStatus(friend.getId());
      assertTrue(retrievedStatus.isPresent());
      assertEquals(friendship.getStatus().name(), retrievedStatus.get());

      verify(friendRepository, atLeast(1)).findById(any());
    }
  }
}
