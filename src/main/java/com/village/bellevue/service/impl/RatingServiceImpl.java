package com.village.bellevue.service.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.RatingEntity.Star;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.RatingException;
import com.village.bellevue.event.RatingEvent;
import com.village.bellevue.repository.PostRepository;
import com.village.bellevue.service.RatingService;

@Service
public class RatingServiceImpl implements RatingService {

  private static final Logger logger = LoggerFactory.getLogger(RatingServiceImpl.class);
  private final PostRepository postRepository;
  private final DataSource dataSource;
  private final ApplicationEventPublisher publisher;

  public RatingServiceImpl(
    PostRepository postRepository,
    DataSource dataSource,
    ApplicationEventPublisher publisher
  ) {
    this.postRepository = postRepository;
    this.dataSource = dataSource;
    this.publisher = publisher;
  }

  @Override
  @Transactional
  public boolean rate(Long post, Star rating) throws AuthorizationException, RatingException {
    Long user = getAuthenticatedUserId();
    if (!postRepository.canRead(post, user)) {
      throw new AuthorizationException(
          "Currently authenticated user is not authorized to rating post");
    }
    boolean success = false;
    try (Connection connection = dataSource.getConnection();
        CallableStatement stmt = connection.prepareCall("{call add_or_update_rating(?, ?, ?)}")) {
      stmt.setLong(1, user);
      stmt.setLong(2, post);
      stmt.setString(3, rating.toValue());
      stmt.executeUpdate();
      success = true;
      return success;
    } catch (SQLException e) {
      logger.error("Error creating rating: {}", e.getMessage(), e);
      throw new RatingException("Failed to create rating. SQL command error: " + e.getMessage(), e);
    } finally {
      if (success) publisher.publishEvent(new RatingEvent(user, post, postRepository.getAuthor(post, user), postRepository.getForum(post, user), rating));
    }
  }
}
