package com.village.bellevue.service.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.CacheConfig.FRIENDSHIP_STATUS_CACHE_NAME;
import static com.village.bellevue.config.CacheConfig.POST_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.CacheConfig.evictKeysByPattern;
import static com.village.bellevue.config.CacheConfig.getCacheKey;
import static com.village.bellevue.config.CacheConfig.getUserCacheKeyPattern;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.id.FriendId;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.NotificationService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class FriendServiceImpl implements FriendService {

  public static final String STATUS_CACHE_KEY = "status";
  public static final String IS_FRIEND_CACHE_KEY = "isFriend";
  public static final String IS_BLOCKING_CACHE_KEY = "isBlocking";

  private static final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);
  private final FriendRepository friendRepository;
  private final NotificationService notificationService;
  private final DataSource dataSource;
  private final CacheManager cacheManager;
  private final RedisTemplate<String, Object> redisTemplate;

  @PersistenceContext
  private EntityManager entityManager;

  public FriendServiceImpl(
      FriendRepository friendRepository,
      NotificationService notificationService,
      DataSource dataSource,
      CacheManager cacheManager,
      RedisTemplate<String, Object> redisTemplate) {
    this.friendRepository = friendRepository;
    this.notificationService = notificationService;
    this.dataSource = dataSource;
    this.cacheManager = cacheManager;
    this.redisTemplate = redisTemplate;
  }

  @Override
  @Transactional
  public void request(Long user) throws FriendshipException {
    if (isBlockedBy(user)) {
      return;
    }
    boolean success = false;
    try (Connection connection = dataSource.getConnection(); CallableStatement stmt = connection.prepareCall("{call request_friend(?, ?)}")) {
      stmt.setLong(1, getAuthenticatedUserId());
      stmt.setLong(2, user);
      stmt.executeUpdate();
      success = true;
    } catch (SQLException e) {
      logger.error("Error requesting friend: {}", e.getMessage(), e);
      throw new FriendshipException(
          "Failed to request friendship. SQL command error: " + e.getMessage(), e);
    } finally {
      entityManager.flush();
      entityManager.clear(); // ensure the database is updated
      if (success) notificationService.notifyFriend(user, 5l, getAuthenticatedUserId());
    }
  }

  @Cacheable(
      value = FRIENDSHIP_STATUS_CACHE_NAME,
      key
      = "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.FriendServiceImpl).STATUS_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #user)")
  @Override
  public Optional<String> getStatus(Long user) throws FriendshipException {
    Long currentUser = getAuthenticatedUserId();
    if (currentUser.equals(user)) {
      return Optional.empty();
    }
    Optional<FriendEntity> friendship = friendRepository.findById(new FriendId(currentUser, user));
    if (friendship.isPresent()) {
      return Optional.of(friendship.get().getStatus().name());
    }
    return Optional.empty();
  }

  @Cacheable(
      value = FRIENDSHIP_STATUS_CACHE_NAME,
      key
      = "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.FriendServiceImpl).IS_FRIEND_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #user)")
  @Override
  public boolean isFriend(Long user) throws FriendshipException {
    if (getAuthenticatedUserId().equals(user)) {
      return false;
    }
    Optional<String> status = getStatus(user);
    if (status.isEmpty()) {
      return false;
    }
    return FriendEntity.FriendshipStatus.ACCEPTED.equals(status.get());
  }

  @Cacheable(
      value = FRIENDSHIP_STATUS_CACHE_NAME,
      key
      = "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.FriendServiceImpl).IS_BLOCKING_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #user)")
  @Override
  public boolean isBlockedBy(Long user) throws FriendshipException {
    if (getAuthenticatedUserId().equals(user)) {
      return false;
    }
    Optional<String> status = getStatus(user);
    if (status.isEmpty()) {
      return false;
    }
    return FriendEntity.FriendshipStatus.BLOCKED_YOU.equals(status.get());
  }

  @Override
  public Page<FriendEntity> readAll(Long user, int page, int size) throws FriendshipException {
    return friendRepository.findFriendsExcludingBlocked(
        user, getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  @Transactional
  public void accept(Long user) throws FriendshipException {
    boolean success = false;
    try (Connection connection = dataSource.getConnection(); CallableStatement stmt = connection.prepareCall("{call accept_friend(?, ?)}")) {
      stmt.setLong(1, getAuthenticatedUserId());
      stmt.setLong(2, user);
      stmt.executeUpdate();
      success = true;
    } catch (SQLException e) {
      logger.error("Error accepting friend: {}", e.getMessage(), e);
      throw new FriendshipException(
          "Failed to accept friendship. SQL command error: " + e.getMessage(), e);
    } finally {
      evictCaches(user);
      if (success) notificationService.notifyFriend(user, 6l, getAuthenticatedUserId());
    }
  }

  @Override
  @Transactional
  public void block(Long user) throws FriendshipException {
    try (Connection connection = dataSource.getConnection(); CallableStatement stmt = connection.prepareCall("{call block_friend(?, ?)}")) {
      stmt.setLong(1, getAuthenticatedUserId());
      stmt.setLong(2, user);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Error blocking user: {}", e.getMessage(), e);
      throw new FriendshipException(
          "Failed to block user. SQL command error: " + e.getMessage(), e);
    } finally {
      entityManager.flush();
      entityManager.clear(); // ensure the database is updated
      evictCaches(user);
    }
  }

  @Override
  @Transactional
  public void remove(Long user) throws FriendshipException {
    try (Connection connection = dataSource.getConnection(); CallableStatement stmt = connection.prepareCall("{call remove_friend(?, ?)}")) {
      stmt.setLong(1, getAuthenticatedUserId());
      stmt.setLong(2, user);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Error removing friend: {}", e.getMessage(), e);
      throw new FriendshipException(
          "Failed to remove friend. SQL command error: " + e.getMessage(), e);
    } finally {
      entityManager.flush();
      entityManager.clear(); // ensure the database is updated
      evictCaches(user);
    }
  }

  private void evictCaches(Long user) {
    Long currentUser = getAuthenticatedUserId();

    Cache cache = cacheManager.getCache(FRIENDSHIP_STATUS_CACHE_NAME);
    if (cache != null) {
      cache.evict(getCacheKey(STATUS_CACHE_KEY, currentUser, user));
      cache.evict(getCacheKey(IS_FRIEND_CACHE_KEY, currentUser, user));
      cache.evict(getCacheKey(IS_BLOCKING_CACHE_KEY, currentUser, user));

      cache.evict(getCacheKey(STATUS_CACHE_KEY, user, currentUser));
      cache.evict(getCacheKey(IS_FRIEND_CACHE_KEY, user, currentUser));
      cache.evict(getCacheKey(IS_BLOCKING_CACHE_KEY, user, currentUser));
    }

    evictKeysByPattern(
        redisTemplate,
        POST_SECURITY_CACHE_NAME,
        getUserCacheKeyPattern(PostServiceImpl.CAN_READ_CACHE_KEY, currentUser));
    evictKeysByPattern(
        redisTemplate,
        POST_SECURITY_CACHE_NAME,
        getUserCacheKeyPattern(PostServiceImpl.CAN_READ_CACHE_KEY, user));
  }
}
