package com.village.bellevue.service.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.CacheConfig.POST_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.id.FavoriteId;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.event.ForumEvent;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.model.ForumModelProvider;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.repository.FavoriteRepository;
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.service.ForumService;
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
            profile.setForumLocation(null); // no locations on forum users (to avoid circular references)
            profile.setProfileLocation(null);
            return Optional.of(profile);
          }
          return optional;
        } catch (FriendshipException e) {
          return Optional.empty();
        }
      }
      return Optional.empty();
    }

    @Override
    public boolean isFavorite(ForumEntity forum) {
      return favoriteRepository.existsById(new FavoriteId(getAuthenticatedUserId(), FavoriteType.FORUM, forum.getId()));
    }
  };

  private final ForumRepository forumRepository;
  private final UserProfileService userProfileService;
  private final FavoriteRepository favoriteRepository;
  private final ApplicationEventPublisher publisher;

  public ForumServiceImpl(
    ForumRepository forumRepository,
    UserProfileService userProfileService,
    FavoriteRepository favoriteRepository,
    ApplicationEventPublisher publisher
  ) {
    this.forumRepository = forumRepository;
    this.userProfileService = userProfileService;
    this.favoriteRepository = favoriteRepository;
    this.publisher = publisher;
  }

  @Override
  @Transactional(timeout = 30)
  public ForumModel create(ForumEntity forum) throws AuthorizationException {
    ForumModel model = null;
    try {
      model = new ForumModel(save(forum), forumModelProvider);
      return model;
    } finally {
      if (Objects.nonNull(model)) publisher.publishEvent(new ForumEvent(getAuthenticatedUserId(), model));
    }
  }

  @Override
  public Optional<ForumModel> read(Long id) throws AuthorizationException {
    if (canRead(id)) {
      Optional<ForumEntity> optional = forumRepository.findById(id);
      if (optional.isEmpty()) return Optional.empty();
      return Optional.of(new ForumModel(optional.get(), forumModelProvider));
    }
    throw new AuthorizationException("Currently authenticated user is not authorized to read forum");
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
  @Transactional(timeout = 30)
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
