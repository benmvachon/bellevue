package com.village.bellevue.service.impl;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.CacheConfig.POST_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.NotificationSettingEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.RatingEntity;
import com.village.bellevue.entity.id.FavoriteId;
import com.village.bellevue.entity.id.NotificationSettingId;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.ForumException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.event.type.ForumEvent;
import com.village.bellevue.event.type.PostDeleteEvent;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.model.ForumModelProvider;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.repository.FavoriteRepository;
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.repository.NotificationSettingRepository;
import com.village.bellevue.repository.PostRepository;
import com.village.bellevue.repository.RatingRepository;
import com.village.bellevue.service.ForumService;
import com.village.bellevue.service.UserProfileService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ForumServiceImpl implements ForumService {

  private static final Logger logger = LoggerFactory.getLogger(ForumServiceImpl.class);

  public static final String CAN_READ_CACHE_KEY = "canRead";
  public static final String CAN_UPDATE_CACHE_KEY = "canUpdate";

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

    @Override
    public Long getUnreadCount(ForumEntity forum) {
      if (forum.getId() == 1l) {
        return forumRepository.getUnreadCount(getAuthenticatedUserId());
      }
      return forumRepository.getUnreadCount(forum.getId(), getAuthenticatedUserId());
    }

    @Override
    public boolean isNotify(ForumEntity forum) {
      Optional<NotificationSettingEntity> setting = notificationSettingRepository.findById(new NotificationSettingId(getAuthenticatedUserId(), forum.getId()));
      return setting.isPresent() && setting.get().isNotify();
    }
  };

  private final ForumRepository forumRepository;
  private final PostRepository postRepository;
  private final RatingRepository ratingRepository;
  private final UserProfileService userProfileService;
  private final FavoriteRepository favoriteRepository;
  private final NotificationSettingRepository notificationSettingRepository;
  private final ApplicationEventPublisher publisher;
  @PersistenceContext(unitName = "async")
  private EntityManager entityManager;

  public ForumServiceImpl(
    ForumRepository forumRepository,
    PostRepository postRepository,
    RatingRepository ratingRepository,
    UserProfileService userProfileService,
    FavoriteRepository favoriteRepository,
    NotificationSettingRepository notificationSettingRepository,
    ApplicationEventPublisher publisher
  ) {
    this.forumRepository = forumRepository;
    this.postRepository = postRepository;
    this.ratingRepository = ratingRepository;
    this.userProfileService = userProfileService;
    this.favoriteRepository = favoriteRepository;
    this.notificationSettingRepository = notificationSettingRepository;
    this.publisher = publisher;
  }

  @Override
  public ForumModel create(ForumEntity forum) throws AuthorizationException, ForumException {
    ForumModel model = null;
    try {
      if (Objects.isNull(forum)) throw new ForumException("Cannot save empty forum");
      if (StringUtils.isBlank(forum.getName())) throw new ForumException("'name' field is required for new forum");
      if (StringUtils.isBlank(forum.getDescription())) throw new ForumException("'description' field is required for new forum");
      if (Objects.isNull(forum.getUsers()) || forum.getUsers().isEmpty()) throw new ForumException("'users' field is required for new forum");
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
  public Page<ForumModel> readAll(String query, boolean unread, int page, int size) {
    Page<ForumEntity> forums = forumRepository.searchForums(getAuthenticatedUserId(), query, unread, PageRequest.of(page, size));
    return forums.map(forum -> {
      try {
        return new ForumModel(forum, forumModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    });
  }

  @Override
  @Transactional(value = "asyncTransactionManager", propagation = Propagation.REQUIRES_NEW, timeout = 300)
  public ForumModel update(Long id, ForumEntity forum) throws AuthorizationException, ForumException {
    if (!canUpdate(id)) throw new AuthorizationException("Currently authenticated user is not authorized to update forum");
    ForumModel model = null;
    if (Objects.isNull(forum)) throw new ForumException("Cannot save empty forum");
    if (StringUtils.isBlank(forum.getName())) throw new ForumException("'name' field is required for new forum");
    if (StringUtils.isBlank(forum.getDescription())) throw new ForumException("'description' field is required for new forum");

    // Step 1: Fetch existing forum from DB
    ForumEntity existingForum = forumRepository.findById(id)
      .orElseThrow(() -> new ForumException("Forum not found"));

    List<Long> oldUserIds = existingForum.getUsers();

    List<Long> newUserIds = forum.getUsers();

    // Step 2: Identify removed users
    List<Long> removedUserIds = new ArrayList<>(oldUserIds);
    removedUserIds.removeAll(newUserIds);

    // Step 3: Save updated forum
    forum.setId(id); // ensure ID is preserved
    model = new ForumModel(save(forum), forumModelProvider);
    List<PostDeleteEvent> postDeleteEvents = new ArrayList<>();

    // Step 4: Execute procedure for removed users
    for (Long user : removedUserIds) {
      // remove all posts
      for (Long post : postRepository.findAllByUserInForum(user, id)) {
        Optional<PostEntity> postEntity = postRepository.findById(post);
        if (postEntity.isEmpty()) continue;
        entityManager.unwrap(Session.class).doWork(connection -> {
          try (
            CallableStatement stmt = connection.prepareCall("{call delete_post(?)}")
          ) {
            stmt.setLong(1, post);
            stmt.executeUpdate();
          } catch (SQLException e) {
            logger.error("Error deleting post: {}", e.getMessage(), e);
          }
          Long parent = null;
          if (Objects.nonNull(postEntity.get().getParent())) parent = postEntity.get().getParent().getId();
          postDeleteEvents.add(new PostDeleteEvent(user, post, parent, id));
        });
      }
      // remove all ratings
      for (RatingEntity rating : ratingRepository.findAllByUserInForum(user, id)) {
        entityManager.unwrap(Session.class).doWork(connection -> {
          try (
            CallableStatement stmt = connection.prepareCall("{call delete_rating(?, ?)}")
          ) {
            stmt.setLong(1, user);
            stmt.setLong(2, rating.getPost());
            stmt.executeUpdate();
          } catch (SQLException e) {
            logger.error("Error deleting rating: {}", e.getMessage(), e);
          }
        });
      }
    }

    publisher.publishEvent(new ForumEvent(getAuthenticatedUserId(), model));
    for (PostDeleteEvent postDeleteEvent : postDeleteEvents) {
      publisher.publishEvent(postDeleteEvent);
    }

    return model;
  }

  @Override
  @Transactional(value = "asyncTransactionManager", propagation = Propagation.REQUIRES_NEW, timeout = 300)
  public boolean removeSelf(Long id) throws AuthorizationException, ForumException {
    Long user = getAuthenticatedUserId();
    if (!canRead(id)) return false;
    ForumEntity forum = forumRepository.findById(id).orElseThrow(() -> new ForumException("Cannot access forum: " + id));
    if (!forum.getUsers().remove(user)) return false;
    forumRepository.save(forum);
    List<PostDeleteEvent> postDeleteEvents = new ArrayList<>();

    for (Long post : postRepository.findAllByUserInForum(user, id)) {
      Optional<PostEntity> postEntity = postRepository.findById(post);
      if (postEntity.isEmpty()) continue;
      entityManager.unwrap(Session.class).doWork(connection -> {
        try (
          CallableStatement stmt = connection.prepareCall("{call delete_post(?)}")
        ) {
          stmt.setLong(1, post);
          stmt.executeUpdate();
        } catch (SQLException e) {
          logger.error("Error deleting post: {}", e.getMessage(), e);
        }
        Long parent = null;
        if (Objects.nonNull(postEntity.get().getParent())) parent = postEntity.get().getParent().getId();
        postDeleteEvents.add(new PostDeleteEvent(user, post, parent, id));
      });
    }
    // remove all ratings
    for (RatingEntity rating : ratingRepository.findAllByUserInForum(user, id)) {
      entityManager.unwrap(Session.class).doWork(connection -> {
        try (
          CallableStatement stmt = connection.prepareCall("{call delete_rating(?, ?)}")
        ) {
          stmt.setLong(1, user);
          stmt.setLong(2, rating.getPost());
          stmt.executeUpdate();
        } catch (SQLException e) {
          logger.error("Error deleting rating: {}", e.getMessage(), e);
        }
      });
    }
    for (PostDeleteEvent postDeleteEvent : postDeleteEvents) {
      publisher.publishEvent(postDeleteEvent);
    }

    return true;
  }

  @Override
  @Transactional(timeout = 30)
  public boolean turnOnNotifications(Long forum) throws AuthorizationException {
    if (canRead(forum)) notificationSettingRepository.save(new NotificationSettingEntity(getAuthenticatedUserId(), forum, true));
    else return false;
    return true;
  }

  @Override
  @Transactional(timeout = 30)
  public boolean turnOffNotifications(Long forum) throws AuthorizationException {
    if (canRead(forum)) notificationSettingRepository.save(new NotificationSettingEntity(getAuthenticatedUserId(), forum, false));
    else return false;
    return true;
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

  @Transactional(timeout = 30)
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

  @Cacheable(
    value = POST_SECURITY_CACHE_NAME,
    key =
        "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.ForumServiceImpl).CAN_UPDATE_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #id)")
  @Override
  public boolean canUpdate(Long id) {
    return forumRepository.canUpdate(id, getAuthenticatedUserId());
  }
}
