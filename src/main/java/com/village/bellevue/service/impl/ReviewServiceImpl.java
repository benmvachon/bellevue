package com.village.bellevue.service.impl;

import static com.village.bellevue.config.CacheConfig.REVIEW_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.CacheConfig.evictKeysByPattern;
import static com.village.bellevue.config.CacheConfig.getEntityCacheKeyPattern;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.ReviewEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.error.ReviewException;
import com.village.bellevue.repository.ReviewRepository;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.RecipeService;
import com.village.bellevue.service.ReviewService;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewServiceImpl implements ReviewService {

  public static final String CAN_READ_CACHE_KEY = "canRead";
  public static final String CAN_UPDATE_CACHE_KEY = "canUpdate";

  private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
  private final ReviewRepository reviewRepository;
  private final RecipeService recipeService;
  private final FriendService friendService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final DataSource dataSource;

  public ReviewServiceImpl(
      ReviewRepository reviewRepository,
      RecipeService recipeService,
      FriendService friendService,
      RedisTemplate<String, Object> redisTemplate,
      DataSource dataSource) {
    this.reviewRepository = reviewRepository;
    this.recipeService = recipeService;
    this.friendService = friendService;
    this.redisTemplate = redisTemplate;
    this.dataSource = dataSource;
  }

  @Override
  @Transactional
  public Long create(ReviewEntity review) throws AuthorizationException, ReviewException {
    if (!recipeService.canRead(review.getRecipe().getId())) {
      throw new AuthorizationException(
          "Currently authenticated user is not authorized to review recipe");
    }
    try (Connection connection = dataSource.getConnection();
        CallableStatement stmt = connection.prepareCall("{call add_review(?, ?, ?, ?, ?)}")) {
      stmt.setLong(1, getAuthenticatedUserId());
      stmt.setLong(2, review.getRecipe().getId());
      stmt.setString(3, review.getReview().toValue());
      stmt.setString(4, review.getContent());
      stmt.registerOutParameter(5, java.sql.Types.INTEGER);
      stmt.executeUpdate();
      return stmt.getLong(5);
    } catch (SQLException e) {
      logger.error("Error creating review: {}", e.getMessage(), e);
      throw new ReviewException("Failed to create review. SQL command error: " + e.getMessage(), e);
    }
  }

  @Override
  public Optional<ReviewEntity> read(Long id) throws AuthorizationException, ReviewException {
    if (!canUpdate(id) && !canRead(id)) {
      throw new AuthorizationException(
          "Currently authenticated user is not friends with review's author");
    }
    return reviewRepository.findById(id);
  }

  @Override
  public Page<ReviewEntity> readAll(int page, int size) {
    return reviewRepository.findAll(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Page<ReviewEntity> readAllByRecipe(Long recipe, int page, int size)
      throws AuthorizationException, ReviewException {
    if (!recipeService.canRead(recipe)) {
      throw new AuthorizationException(
          "Currently authenticated user is not authorized to review recipe");
    }
    return reviewRepository.findByRecipeId(
        recipe, getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Page<ReviewEntity> readAllByAuthor(Long author, int page, int size)
      throws AuthorizationException, ReviewException {
    Long user = getAuthenticatedUserId();
    try {
      if (!user.equals(author) && !friendService.isFriend(author)) {
        throw new AuthorizationException(
            "Currently authenticated user is not authorized to read reviews");
      }
      return reviewRepository.findByAuthorId(author, user, PageRequest.of(page, size));
    } catch (FriendshipException e) {
      throw new ReviewException("User not found: " + author);
    }
  }

  @Override
  public Page<ReviewEntity> readIncomplete(int page, int size) {
    return reviewRepository.findIncomplete(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  @Transactional
  public ReviewEntity update(Long id, ReviewEntity updatedReview)
      throws AuthorizationException, ReviewException {
    if (!canUpdate(id)) {
      throw new AuthorizationException("Currently authenticated user did not author review: " + id);
    }
    try (Connection connection = dataSource.getConnection();
        CallableStatement stmt = connection.prepareCall("{call update_review(?, ?, ?)}")) {
      stmt.setLong(1, id);
      stmt.setString(2, updatedReview.getReview().toValue());
      stmt.setString(3, updatedReview.getContent());
      stmt.executeUpdate();
      return updatedReview;
    } catch (SQLException e) {
      logger.error("Error updating review with ID {}: {}", id, e.getMessage(), e);
      throw new ReviewException("Failed to update review. SQL command error: " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void delete(Long id) throws AuthorizationException, ReviewException {
    if (!canUpdate(id)) {
      throw new AuthorizationException("Currently authenticated user did not author review: " + id);
    }
    try (Connection connection = dataSource.getConnection();
        CallableStatement stmt = connection.prepareCall("{call delete_review(?)}")) {
      stmt.setLong(1, id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Error deleting review with ID {}: {}", id, e.getMessage(), e);
      throw new ReviewException("Failed to delete review. SQL command error: " + e.getMessage(), e);
    } finally {
      evictCaches(getAuthenticatedUserId());
    }
  }

  @Cacheable(
      value = REVIEW_SECURITY_CACHE_NAME,
      key =
          "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.ReviewServiceImpl).CAN_READ_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #id)")
  @Override
  public boolean canRead(Long id) {
    return reviewRepository.canRead(id, getAuthenticatedUserId());
  }

  @Cacheable(
      value = REVIEW_SECURITY_CACHE_NAME,
      key =
          "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.ReviewServiceImpl).CAN_UPDATE_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #id)")
  @Override
  public boolean canUpdate(Long id) {
    return reviewRepository.canUpdate(id, getAuthenticatedUserId());
  }

  private void evictCaches(Long id) {
    evictKeysByPattern(
        redisTemplate,
        REVIEW_SECURITY_CACHE_NAME,
        getEntityCacheKeyPattern(CAN_READ_CACHE_KEY, id));
    evictKeysByPattern(
        redisTemplate,
        REVIEW_SECURITY_CACHE_NAME,
        getEntityCacheKeyPattern(CAN_UPDATE_CACHE_KEY, id));
  }
}
