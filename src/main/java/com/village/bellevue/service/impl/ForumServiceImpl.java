package com.village.bellevue.service.impl;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.CacheConfig.POST_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.model.ForumModelProvider;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.repository.ProfileRepository;
import com.village.bellevue.service.ForumService;
import com.village.bellevue.service.NotificationService;
import com.village.bellevue.service.UserProfileService;

@Service
public class ForumServiceImpl implements ForumService {

  public static final String CAN_READ_CACHE_KEY = "canRead";

  public ForumModelProvider forumModelProvider = new ForumModelProvider() {
    @Override
    public boolean canReadForum(ForumEntity forum) {
      return canRead(forum.getId());
    }

    @Override
    public Optional<ProfileModel> getProfile(Long user) {
      if (user != null) {
        try {
          Optional<ProfileModel> optional = userProfileService.read(user);
          if (optional.isPresent()) {
            ProfileModel profile = optional.get();
            profile.setLocation(null); // no locations on forum users (to avoid circular references)
            return Optional.of(profile);
          }
          return optional;
        } catch (FriendshipException e) {
          return Optional.empty();
        }
      }
      return Optional.empty();
    }
  };

  private final ForumRepository forumRepository;
  private final ProfileRepository profileRepository;
  private final NotificationService notificationService;
  private final UserProfileService userProfileService;

  public ForumServiceImpl(
    ForumRepository forumRepository,
    ProfileRepository profileRepository,
    NotificationService notificationService,
    UserProfileService userProfileService
  ) {
    this.forumRepository = forumRepository;
    this.profileRepository = profileRepository;
    this.notificationService = notificationService;
    this.userProfileService = userProfileService;
  }

  @Override
  @Transactional
  public ForumModel create(ForumEntity forum) throws AuthorizationException {
    boolean saved = false;
    try {
      forum = save(forum);
      saved = true;
      return new ForumModel(forum, forumModelProvider);
    } finally {
      if (saved) notificationService.notifyFriends(1l, forum.getId());
    }
  }

  @Override
  public Optional<ForumModel> read(Long id) throws AuthorizationException {
    if (canRead(id)) {
      try {
        Optional<ForumEntity> optional = forumRepository.findById(id);
        if (optional.isEmpty()) return Optional.empty();
        return Optional.of(new ForumModel(optional.get(), forumModelProvider));
      } finally {
        ForumEntity forumEntity = new ForumEntity();
        forumEntity.setId(id);
        profileRepository.setLocation(getAuthenticatedUserId(), forumEntity);
      }
    }
    throw new AuthorizationException("Currently authenticated user is not authorized to read forum");
  }

  @Override
  public Page<String> readAllCategories(int page, int size){
    return forumRepository.findAllCategories(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Page<ForumModel> readAllByCategory(String category, int page, int size) {
    Page<ForumEntity> forums = forumRepository.findAllByCategory(getAuthenticatedUserId(), category, PageRequest.of(page, size));
    return forums.map(forum -> {
      try {
        return new ForumModel(forum, forumModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    });
  }

  @Override
  public Page<ForumModel> readAll(int page, int size) {
    Page<ForumEntity> forums = forumRepository.findAll(getAuthenticatedUserId(), PageRequest.of(page, size));
    return forums.map(forum -> {
      try {
        return new ForumModel(forum, forumModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    });
  }

  @Override
  @Transactional
  public boolean delete(Long id) throws AuthorizationException {
    Optional<ForumModel> forum = read(id);
    if (forum.isEmpty()) return false;
    if (!getAuthenticatedUserId().equals(forum.get().getUser().getId()))
      throw new AuthorizationException("Currently authenticated user is not authorized to delete forum");
    forumRepository.deleteById(id);
    return true;
  }

  private ForumEntity save(ForumEntity forum) {
    forum.setUser(getAuthenticatedUserId());
    return forumRepository.save(forum);
  }

  @Cacheable(
      value = POST_SECURITY_CACHE_NAME,
      key =
          "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.ForumServiceImpl).CAN_READ_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #id)")
  @Override
  public boolean canRead(Long id) {
    return forumRepository.canRead(id, getAuthenticatedUserId());
  }
}
