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
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.repository.ProfileRepository;
import com.village.bellevue.service.ForumService;
import com.village.bellevue.service.NotificationService;

@Service
public class ForumServiceImpl implements ForumService {

  public static final String CAN_READ_CACHE_KEY = "canRead";

  private final ForumRepository forumRepository;
  private final ProfileRepository profileRepository;
  private final NotificationService notificationService;

  public ForumServiceImpl(
    ForumRepository forumRepository,
    ProfileRepository profileRepository,
    NotificationService notificationService
  ) {
    this.forumRepository = forumRepository;
    this.profileRepository = profileRepository;
    this.notificationService = notificationService;
  }

  @Override
  @Transactional
  public ForumEntity create(ForumEntity forum) {
    boolean saved = false;
    try {
      forum = save(forum);
      saved = true;
      return forum;
    } finally {
      if (saved) notificationService.notifyFriends(1l, forum.getId());
    }
  }

  @Override
  public Optional<ForumEntity> read(Long id) throws AuthorizationException {
    if (canRead(id)) {
      try {
        return forumRepository.findById(id);
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
  public Page<ForumEntity> readAllByCategory(String category, int page, int size) {
    return forumRepository.findAllByCategory(getAuthenticatedUserId(), category, PageRequest.of(page, size));
  }

  @Override
  public Page<ForumEntity> readAll(int page, int size) {
    return forumRepository.findAll(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  @Transactional
  public boolean delete(Long id) throws AuthorizationException {
    Optional<ForumEntity> forum = read(id);
    if (forum.isEmpty()) return false;
    if (!getAuthenticatedUserId().equals(forum.get().getUser()))
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
