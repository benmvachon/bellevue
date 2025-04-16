package com.village.bellevue.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.repository.UserProfileRepository;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.UserProfileService;

@Service
public class UserProfileServiceImpl implements UserProfileService {
  private final UserProfileRepository userProfileRepository;
  private final FriendService friendService;

  public UserProfileServiceImpl(
    UserProfileRepository userProfileRepository,
    FriendService friendService
  ) {
    this.userProfileRepository = userProfileRepository;
    this.friendService = friendService;
  }

  @Override
  public Optional<UserProfileEntity> read(Long user) throws FriendshipException {
    if (friendService.isBlockedBy(user)) {
      return Optional.empty();
    }
    return Optional.of(userProfileRepository.findById(user).orElseThrow(() -> new FriendshipException("User not found with id: " + user)));
  }

  @Override
  public Page<UserProfileEntity> readFriendsByLocation(Long forum, int page, int size) {
    return userProfileRepository.findAllFriendsByLocation(getAuthenticatedUserId(), forum, PageRequest.of(page, size));
  }

  @Override
  public Page<UserProfileEntity> readNonFriendsByLocation(Long forum, int page, int size) {
    return userProfileRepository.findAllNonFriendsByLocation(getAuthenticatedUserId(), forum, PageRequest.of(page, size));
  }

  @Override
  public Page<UserProfileEntity> readByNamePrefix(String prefix, int page, int size) {
    return userProfileRepository.findByNameOrUsernameStartsWith(getAuthenticatedUserId(), prefix, PageRequest.of(page, size));
  }
}
