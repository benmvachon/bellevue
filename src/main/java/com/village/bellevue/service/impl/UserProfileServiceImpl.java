package com.village.bellevue.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.entity.id.FavoriteId;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.model.ProfileModelProvider;
import com.village.bellevue.repository.FavoriteRepository;
import com.village.bellevue.repository.ProfileRepository;
import com.village.bellevue.repository.UserProfileRepository;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.UserProfileService;

@Service
public class UserProfileServiceImpl implements UserProfileService {

  private final ProfileModelProvider profileModelProvider = new ProfileModelProvider() {
    @Override
    public Optional<String> getFriendshipStatus(Long user) {
      try {
        if (getAuthenticatedUserId().equals(user)) return Optional.of("SELF");
        Optional<String> friendshipStatus = friendService.getStatus(user);
        if (friendshipStatus.isEmpty()) return Optional.of("UNSET");
        return friendshipStatus;
      } catch (FriendshipException ex) {
        return Optional.of("UNSET");
      }
    }
    
    @Override
    public boolean isFavorite(Long user) {
      return favoriteRepository.existsById(new FavoriteId(getAuthenticatedUserId(), FavoriteType.PROFILE, user));
    }
  };

  private final UserProfileRepository userProfileRepository;
  private final ProfileRepository profileRepository;
  private final FriendService friendService;
  private final FavoriteRepository favoriteRepository;

  public UserProfileServiceImpl(
    UserProfileRepository userProfileRepository,
    ProfileRepository profileRepository,
    FriendService friendService,
    FavoriteRepository favoriteRepository
  ) {
    this.userProfileRepository = userProfileRepository;
    this.profileRepository = profileRepository;
    this.friendService = friendService;
    this.favoriteRepository = favoriteRepository;
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
    return profileEntities.map(profile -> {
      return new ProfileModel(profile, profileModelProvider);
    });
  }

  @Override
  public Page<ProfileModel> readNonFriendsByLocation(Long forum, int page, int size) {
    Page<UserProfileEntity> profileEntities = userProfileRepository.findAllNonFriendsByLocation(getAuthenticatedUserId(), forum, PageRequest.of(page, size));
    return profileEntities.map(profile -> {
      return new ProfileModel(profile, profileModelProvider);
    });
  }

  @Override
  public Page<ProfileModel> readByNamePrefix(String prefix, int page, int size) {
    Page<UserProfileEntity> profileEntities = userProfileRepository.findByNameOrUsernameStartsWith(getAuthenticatedUserId(), prefix, PageRequest.of(page, size));
    return profileEntities.map(profile -> {
      return new ProfileModel(profile, profileModelProvider);
    });
  }

  @Override
  public void setBlackboard(String blackboard) {
    profileRepository.setBlackboard(getAuthenticatedUserId(), blackboard);
  }
}
