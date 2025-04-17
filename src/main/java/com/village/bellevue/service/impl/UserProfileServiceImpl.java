package com.village.bellevue.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.model.ProfileModelProvider;
import com.village.bellevue.repository.UserProfileRepository;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.UserProfileService;

@Service
public class UserProfileServiceImpl implements UserProfileService {
  private final ProfileModelProvider profileModelProvider = new ProfileModelProvider() {
    @Override
    public Optional<String> getFriendshipStatus(Long user) {
      try {
        return friendService.getStatus(user);
      } catch (FriendshipException ex) {
        return Optional.empty();
      }
    }
    
  };

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
  public Optional<ProfileModel> read(Long user) throws FriendshipException {
    if (friendService.isBlockedBy(user)) {
      return Optional.empty();
    }
    UserProfileEntity profile = userProfileRepository.findById(user).orElseThrow(() -> new FriendshipException("User not found with id: " + user));
    return Optional.of(new ProfileModel(profile, profileModelProvider));
  }

  @Override
  public Page<ProfileModel> readFriendsByLocation(Long forum, int page, int size) {
    Page<UserProfileEntity> profileEntities = userProfileRepository.findAllFriendsByLocation(getAuthenticatedUserId(), forum, PageRequest.of(page, size));
    return profileEntities.map(post -> {
      return new ProfileModel(post, profileModelProvider);
    });
  }

  @Override
  public Page<ProfileModel> readNonFriendsByLocation(Long forum, int page, int size) {
    Page<UserProfileEntity> profileEntities = userProfileRepository.findAllNonFriendsByLocation(getAuthenticatedUserId(), forum, PageRequest.of(page, size));
    return profileEntities.map(post -> {
      return new ProfileModel(post, profileModelProvider);
    });
  }

  @Override
  public Page<ProfileModel> readByNamePrefix(String prefix, int page, int size) {
    Page<UserProfileEntity> profileEntities = userProfileRepository.findByNameOrUsernameStartsWith(getAuthenticatedUserId(), prefix, PageRequest.of(page, size));
    return profileEntities.map(post -> {
      return new ProfileModel(post, profileModelProvider);
    });
  }
}
