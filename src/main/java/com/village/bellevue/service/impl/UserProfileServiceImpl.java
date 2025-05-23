package com.village.bellevue.service.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.entity.ProfileEntity.LocationType;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.entity.id.FavoriteId;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.event.type.BlackboardEvent;
import com.village.bellevue.event.type.LocationEvent;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.model.ProfileModelProvider;
import com.village.bellevue.repository.FavoriteRepository;
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.repository.PostRepository;
import com.village.bellevue.repository.ProfileRepository;
import com.village.bellevue.repository.UserProfileRepository;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.UserProfileService;

@Service
public class UserProfileServiceImpl implements UserProfileService {

  private final ProfileModelProvider profileModelProvider = new ProfileModelProvider() {
    public boolean isFavorite(Long user) {
      return favoriteRepository.existsById(new FavoriteId(getAuthenticatedUserId(), FavoriteType.PROFILE, user));
    };

    @Override
    public Optional<String> getFriendshipStatus(Long user) {
      try {
        if (getAuthenticatedUserId().equals(user)) {
          return Optional.of("SELF");
        }
        Optional<String> friendshipStatus = friendService.getStatus(user);
        if (friendshipStatus.isEmpty()) return Optional.of("UNSET");
        return friendshipStatus;
      } catch (FriendshipException ex) {
        return Optional.of("UNSET");
      }
    }

    @Override
    public UserProfileEntity getProfileLocation(Long location) {
      return userProfileRepository.getReferenceById(location);
    }

    @Override
    public ForumEntity getForumLocation(Long location) {
      return forumRepository.getReferenceById(location);
    }

    @Override
    public PostEntity getPostLocation(Long location) {
      return postRepository.getReferenceById(location);
    }
  };

  private final UserProfileRepository userProfileRepository;
  private final ProfileRepository profileRepository;
  private final ForumRepository forumRepository;
  private final PostRepository postRepository;
  private final FriendService friendService;
  private final FavoriteRepository favoriteRepository;
  private final ApplicationEventPublisher publisher;

  public UserProfileServiceImpl(
    UserProfileRepository userProfileRepository,
    ProfileRepository profileRepository,
    ForumRepository forumRepository,
    PostRepository postRepository,
    FriendService friendService,
    FavoriteRepository favoriteRepository,
    ApplicationEventPublisher publisher
  ) {
    this.userProfileRepository = userProfileRepository;
    this.profileRepository = profileRepository;
    this.forumRepository = forumRepository;
    this.postRepository = postRepository;
    this.friendService = friendService;
    this.favoriteRepository = favoriteRepository;
    this.publisher = publisher;
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
  public Page<ProfileModel> readFriendsByLocation(int page, int size) {
    Long user = getAuthenticatedUserId();
    UserProfileEntity profile = userProfileRepository.getReferenceById(user);
    if (Objects.isNull(profile) || Objects.isNull(profile.getLocationType()) || Objects.isNull(profile.getLocation())) {
      return Page.empty();
    }
    Page<UserProfileEntity> profileEntities = userProfileRepository.findAllFriendsByLocation(user, profile.getLocation(), profile.getLocationType(), PageRequest.of(page, size));
    return profileEntities.map(other -> {
      return new ProfileModel(other, profileModelProvider);
    });
  }

  @Override
  public Page<ProfileModel> readNonFriendsByLocation(int page, int size) {
    Long user = getAuthenticatedUserId();
    UserProfileEntity profile = userProfileRepository.getReferenceById(user);
    if (Objects.isNull(profile) || Objects.isNull(profile.getLocationType()) || Objects.isNull(profile.getLocation())) {
      return Page.empty();
    }
    Page<UserProfileEntity> profileEntities = userProfileRepository.findAllNonFriendsByLocation(user, profile.getLocation(), profile.getLocationType(), PageRequest.of(page, size));
    return profileEntities.map(other -> {
      return new ProfileModel(other, profileModelProvider);
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
  public void setLocation(Long location, LocationType locationType) {
    UserProfileEntity profile = userProfileRepository.getReferenceById(getAuthenticatedUserId());
    if (Objects.nonNull(profile) && Objects.nonNull(profile.getLocationType()) && Objects.nonNull(profile.getLocation())) {
      if (profile.getLocationType().equals(locationType) && profile.getLocation().equals(location)) return;
      publisher.publishEvent(new LocationEvent(getAuthenticatedUserId(), profile.getLocation(), profile.getLocationType(), false));
    }
    profileRepository.setLocation(getAuthenticatedUserId(), location, locationType);
    publisher.publishEvent(new LocationEvent(getAuthenticatedUserId(), location, locationType, true));
  }

  @Override
  public void setBlackboard(String blackboard) {
    profileRepository.setBlackboard(getAuthenticatedUserId(), blackboard);
    publisher.publishEvent(new BlackboardEvent(getAuthenticatedUserId(), blackboard));
  }
}
