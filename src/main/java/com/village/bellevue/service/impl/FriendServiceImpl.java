package com.village.bellevue.service.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.CacheConfig.FRIENDSHIP_STATUS_CACHE_NAME;
import static com.village.bellevue.config.CacheConfig.POST_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.CacheConfig.evictKeysByPattern;
import static com.village.bellevue.config.CacheConfig.getCacheKey;
import static com.village.bellevue.config.CacheConfig.getUserCacheKeyPattern;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.entity.id.FavoriteId;
import com.village.bellevue.entity.id.FriendId;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.event.type.AcceptanceEvent;
import com.village.bellevue.event.type.FriendRemovalEvent;
import com.village.bellevue.event.type.RequestEvent;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.model.ProfileModelProvider;
import com.village.bellevue.model.SuggestedFriendModel;
import com.village.bellevue.repository.FavoriteRepository;
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.PostRepository;
import com.village.bellevue.repository.UserProfileRepository;
import com.village.bellevue.service.FriendService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class FriendServiceImpl implements FriendService {

  private final ProfileModelProvider profileModelProvider = new ProfileModelProvider() {
    public boolean isFavorite(Long user) {
      return favoriteRepository.existsById(new FavoriteId(getAuthenticatedUserId(), FavoriteType.PROFILE, user));
    };

    public Optional<String> getFriendshipStatus(Long user) {
      try {
        if (getAuthenticatedUserId().equals(user)) return Optional.of("SELF");
        Optional<String> friendshipStatus = getStatus(user);
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

  public static final String STATUS_CACHE_KEY = "status";
  public static final String IS_FRIEND_CACHE_KEY = "isFriend";

  private static final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);
  private final FriendRepository friendRepository;
  private final FavoriteRepository favoriteRepository;
  private final UserProfileRepository userProfileRepository;
  private final ForumRepository forumRepository;
  private final PostRepository postRepository;
  private final DataSource dataSource;
  private final DataSource asyncDataSource;
  private final CacheManager cacheManager;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ApplicationEventPublisher publisher;

  @PersistenceContext
  private EntityManager entityManager;

  public FriendServiceImpl(
    FriendRepository friendRepository,
    FavoriteRepository favoriteRepository,
    UserProfileRepository userProfileRepository,
    ForumRepository forumRepository,
    PostRepository postRepository,
    @Qualifier("dataSource") DataSource dataSource,
    @Qualifier("asyncDataSource") DataSource asyncDataSource,
    CacheManager cacheManager,
    RedisTemplate<String, Object> redisTemplate,
    ApplicationEventPublisher publisher
  ) {
    this.friendRepository = friendRepository;
    this.favoriteRepository = favoriteRepository;
    this.userProfileRepository = userProfileRepository;
    this.forumRepository = forumRepository;
    this.postRepository = postRepository;
    this.dataSource = dataSource;
    this.asyncDataSource = asyncDataSource;
    this.cacheManager = cacheManager;
    this.redisTemplate = redisTemplate;
    this.publisher = publisher;
  }

  @Override
  @Transactional(timeout = 30)
  public void request(Long user) throws FriendshipException {
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
      if (success) publisher.publishEvent(new RequestEvent(getAuthenticatedUserId(), user));
      evictCaches(user);
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
      return Optional.of(friendship.get().getStatus().toValue());
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

  @Override
  public Page<ProfileModel> readAll(String query, List<Long> excluded, int page, int size) throws FriendshipException {
    Page<UserProfileEntity> entities = friendRepository.findFriends(getAuthenticatedUserId(), query, excluded, PageRequest.of(page, size));
    return entities.map(entity -> {
      return new ProfileModel(entity, profileModelProvider);
    });
  }

  @Override
  public Page<ProfileModel> readSuggestions(int page, int size) throws FriendshipException {
    Page<SuggestedFriendModel> friendModels = friendRepository.findSuggestedFriends(getAuthenticatedUserId(), PageRequest.of(page, size));
    return friendModels.map(friendModel -> {
      return new ProfileModel(friendModel.getFriend(), profileModelProvider);
    });
  }

  @Override
  public Page<ProfileModel> readAll(Long user, String query, int page, int size) throws FriendshipException {
    Page<UserProfileEntity> entities = friendRepository.findFriends(user, getAuthenticatedUserId(), query, PageRequest.of(page, size));
    return entities.map(entity -> {
      return new ProfileModel(entity, profileModelProvider);
    });
  }

  @Async
  @Override
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void accept(Long user) throws FriendshipException {
    boolean success = false;
    try (Connection connection = asyncDataSource.getConnection(); CallableStatement stmt = connection.prepareCall("{call accept_friend(?, ?)}")) {
      stmt.setLong(1, getAuthenticatedUserId());
      stmt.setLong(2, user);
      stmt.executeUpdate();
      success = true;
    } catch (SQLException e) {
      logger.error("Error accepting friend: {}", e.getMessage(), e);
      success = false;
      throw new FriendshipException("Failed to accept friendship. SQL command error: " + e.getMessage(), e);
    } finally {
      evictCaches(user);
      if (success) publisher.publishEvent(new AcceptanceEvent(getAuthenticatedUserId(), user));
    }
  }

  @Async
  @Override
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void remove(Long user) throws FriendshipException {
    boolean success = false;
    try (Connection connection = asyncDataSource.getConnection(); CallableStatement stmt = connection.prepareCall("{call remove_friend(?, ?)}")) {
      stmt.setLong(1, getAuthenticatedUserId());
      stmt.setLong(2, user);
      stmt.executeUpdate();
      success = true;
    } catch (SQLException e) {
      logger.error("Error removing friend: {}", e.getMessage(), e);
      success = false;
      throw new FriendshipException("Failed to remove friend. SQL command error: " + e.getMessage(), e);
    } finally {
      evictCaches(user);
      if (success) publisher.publishEvent(new FriendRemovalEvent(getAuthenticatedUserId(), user));
    }
  }

  private void evictCaches(Long user) {
    Long currentUser = getAuthenticatedUserId();

    Cache cache = cacheManager.getCache(FRIENDSHIP_STATUS_CACHE_NAME);
    if (cache != null) {
      cache.evict(getCacheKey(STATUS_CACHE_KEY, currentUser, user));
      cache.evict(getCacheKey(IS_FRIEND_CACHE_KEY, currentUser, user));

      cache.evict(getCacheKey(STATUS_CACHE_KEY, user, currentUser));
      cache.evict(getCacheKey(IS_FRIEND_CACHE_KEY, user, currentUser));
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
