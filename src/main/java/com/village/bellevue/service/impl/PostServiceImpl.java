package com.village.bellevue.service.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.CacheConfig.POST_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.NotificationSettingEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.entity.NotificationEntity.NotificationType;
import com.village.bellevue.entity.RatingEntity.Star;
import com.village.bellevue.entity.id.AggregateRatingId;
import com.village.bellevue.entity.id.FavoriteId;
import com.village.bellevue.entity.id.NotificationSettingId;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.error.PostException;
import com.village.bellevue.error.RatingException;
import com.village.bellevue.event.type.ForumReadCountEvent;
import com.village.bellevue.event.type.NotificationReadEvent;
import com.village.bellevue.event.type.PopularityEvent;
import com.village.bellevue.event.type.PostDeleteEvent;
import com.village.bellevue.event.type.PostEvent;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.model.ForumModelProvider;
import com.village.bellevue.model.PostModel;
import com.village.bellevue.model.PostModelProvider;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.model.ProfileModelProvider;
import com.village.bellevue.repository.*;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.PostService;
import com.village.bellevue.service.RatingService;

@Service
public class PostServiceImpl implements PostService {

  public static final String CAN_READ_CACHE_KEY = "canRead";
  private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

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

  private final PostModelProvider postModelProvider = new PostModelProvider() {
    @Override
    public Optional<AggregateRatingEntity> getAggregateRating(Long postId) {
      return ratingRepository.findById(
        new AggregateRatingId(getAuthenticatedUserId(), postId)
      );
    }

    @Override
    public Long getChildrenCount(Long postId) {
      return getChildren(postId);
    }

    @Override
    public boolean canReadPost(PostEntity post) {
      return canRead(post.getId());
    }

    @Override
    public ProfileModel getProfile(UserProfileEntity user) {
      return new ProfileModel(user, profileModelProvider);
    }

    @Override
    public ForumModel getForum(ForumEntity forum) throws AuthorizationException {
      return new ForumModel(forum, new ForumModelProvider() {
        @Override
        public boolean canReadForum(ForumEntity forum) {
          return forumRepository.canRead(forum.getId(), getAuthenticatedUserId());
        }
    
        @Override
        public Optional<ProfileModel> getProfile(Long user) {
          if (user != null) {
            return Optional.of(new ProfileModel(userProfileRepository.getReferenceById(user), profileModelProvider));
          }
          return Optional.empty();
        }

        @Override
        public boolean isFavorite(ForumEntity forum) {
          return favoriteRepository.existsById(new FavoriteId(getAuthenticatedUserId(), FavoriteType.FORUM, forum.getId()));
        }

        @Override
        public Long getUnreadCount(ForumEntity forum) {
          return forumRepository.getUnreadCount(forum.getId(), getAuthenticatedUserId());
        }

        @Override
        public boolean isNotify(ForumEntity forum) {
          Optional<NotificationSettingEntity> setting = notificationSettingRepository.findById(new NotificationSettingId(getAuthenticatedUserId(), forum.getId()));
          return setting.isPresent() && setting.get().isNotify();
        }
      });
    }

    @Override
    public boolean isFavorite(Long post) {
      return favoriteRepository.existsById(new FavoriteId(getAuthenticatedUserId(), FavoriteType.POST, post));
    }
  };

  private final PostRepository postRepository;
  private final FavoriteRepository favoriteRepository;
  private final AggregateRatingRepository ratingRepository;
  private final ForumRepository forumRepository;
  private final UserProfileRepository userProfileRepository;
  private final NotificationSettingRepository notificationSettingRepository;
  private final NotificationRepository notificationRepository;
  private final FriendService friendService;
  private final RatingService ratingService;
  private final DataSource dataSource;
  private final ApplicationEventPublisher publisher;

  public PostServiceImpl(
    PostRepository postRepository,
    FavoriteRepository favoriteRepository,
    AggregateRatingRepository ratingRepository,
    ForumRepository forumRepository,
    UserProfileRepository userProfileRepository,
    NotificationSettingRepository notificationSettingRepository,
    NotificationRepository notificationRepository,
    FriendService friendService,
    RatingService ratingService,
    DataSource dataSource,
    ApplicationEventPublisher publisher
  ) {
    this.postRepository = postRepository;
    this.favoriteRepository = favoriteRepository;
    this.ratingRepository = ratingRepository;
    this.forumRepository = forumRepository;
    this.userProfileRepository = userProfileRepository;
    this.notificationSettingRepository = notificationSettingRepository;
    this.notificationRepository = notificationRepository;
    this.friendService = friendService;
    this.ratingService = ratingService;
    this.dataSource = dataSource;
    this.publisher = publisher;
  }

  @Override
  public PostModel post(Long forum, String content) throws AuthorizationException, PostException {
    PostModel model = null;
    try {
      PostEntity post = new PostEntity();
      ForumEntity forumEntity = forumRepository.getReferenceById(forum);
      post.setForum(forumEntity);
      post.setContent(content);
      model = new PostModel(save(post), postModelProvider);
      try {
        ratingService.rate(model.getId(), Star.FIVE);
      } catch (RatingException e) {
        throw new PostException("Could not rate new post");
      }
      return model;
    } finally {
      if (model != null) {
        publisher.publishEvent(new PostEvent(getAuthenticatedUserId(), model));
      }
    }
  }

  @Override
  public PostModel reply(Long forum, Long parent, String content) throws AuthorizationException, PostException {
    Long user = getAuthenticatedUserId();
    int depth = 1;
    PostModel post = read(parent).orElseThrow(() -> new AuthorizationException("Cannot reply to " + parent));
    List<PopularityEvent> events = new ArrayList<>();
    while (Objects.nonNull(post)) {
      if (!canRead(post.getId())) throw new AuthorizationException("Cannot reply to " + parent);
      PostModel parentPost = post.getParent();
      events.add(new PopularityEvent(user, post.getId(), Objects.nonNull(parentPost) ? parentPost.getId() : null, post.getForum().getId()));
      depth ++;
      post = parentPost;
    }
    if (depth >= 9) throw new PostException("Post too deep for replies");
    PostModel model = null;
    try (
      Connection connection = dataSource.getConnection();
      CallableStatement stmt = connection.prepareCall("{call add_reply(?, ?, ?, ?, ?)}")
    ) {
      stmt.setLong(1, user);
      stmt.setLong(2, parent);
      stmt.setLong(3, forum);
      stmt.setString(4, content);
      stmt.registerOutParameter(5, Types.INTEGER);
      stmt.executeUpdate();
      Long reply = stmt.getLong(5);
      model = new PostModel(postRepository.getReferenceById(reply), postModelProvider);
      try {
        ratingService.rate(model.getId(), Star.FIVE);
      } catch (RatingException e) {
        throw new PostException("Could not rate new reply");
      }
      return model;
    } catch (SQLException e) {
      logger.error("Error adding reply: {}", e.getMessage(), e);
      throw new PostException("Failed to create reply. SQL command error: " + e.getMessage(), e);
    } finally {
      if (model != null) {
        publisher.publishEvent(new PostEvent(user, model));
        for (PopularityEvent event : events) publisher.publishEvent(event);
      }
    }
  }

  @Override
  public Optional<PostModel> read(Long id) throws AuthorizationException {
    if (canRead(id)) {
      Optional<PostEntity> post = postRepository.findById(id);
      if (post.isPresent()) {
        return Optional.of(new PostModel(post.get(), postModelProvider));
      }
      return Optional.empty();
    }
    throw new AuthorizationException(
        "Currently authenticated user is not authorized to read post");
  }

  @Override
  public List<PostModel> readAll(Timestamp createdCursor, Long idCursor, Long limit) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    List<PostEntity> postEntities = postRepository.findRecentTopLevel(
      user,
      createdCursor,
      idCursor,
      limit
    );
    return postEntities.stream().map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  @Override
  public List<PostModel> readAll(Long popularityCursor, Long idCursor, Long limit) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    List<PostEntity> postEntities = postRepository.findPopularTopLevel(
      user,
      popularityCursor,
      idCursor,
      limit
    );
    return postEntities.stream().map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  @Override
  public List<PostModel> readAllByForum(Long forum, Timestamp createdCursor, Long idCursor, Long limit) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!forumRepository.canRead(forum, user)) throw new AuthorizationException("User not authorized");
    List<PostEntity> postEntities = postRepository.findRecentTopLevelByForum(
      user,
      forum,
      createdCursor,
      idCursor,
      limit
    );
    return postEntities.stream().map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  @Override
  public List<PostModel> readAllByForum(Long forum, Long popularityCursor, Long idCursor, Long limit) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!forumRepository.canRead(forum, user)) throw new AuthorizationException("User not authorized");
    List<PostEntity> postEntities = postRepository.findPopularTopLevelByForum(
      user,
      forum,
      popularityCursor,
      idCursor,
      limit
    );
    return postEntities.stream().map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  @Override
  public Long countAllByForum(Long forum) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!forumRepository.canRead(forum, user)) throw new AuthorizationException("User not authorized");
    return postRepository.countTopLevelByForum(user, forum);
  }

  @Override
  public List<PostModel> readAllByParent(Long parent, Timestamp createdCursor, Long idCursor, Long limit) throws AuthorizationException {
    if (!canRead(parent)) throw new AuthorizationException("User not authorized");
    List<PostEntity> postEntities = postRepository.findRecentChildren(
      getAuthenticatedUserId(),
      parent,
      createdCursor,
      idCursor,
      limit
    );
    return postEntities.stream().map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  @Override
  public List<PostModel> readAllByParent(Long parent, Long popularityCursor, Long idCursor, Long limit) throws AuthorizationException {
    if (!canRead(parent)) throw new AuthorizationException("User not authorized");
    List<PostEntity> postEntities = postRepository.findPopularChildren(
      getAuthenticatedUserId(),
      parent,
      popularityCursor,
      idCursor,
      limit
    );
    return postEntities.stream().map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  @Override
  public Long countAllByParent(Long parent) throws AuthorizationException {
    if (!canRead(parent)) throw new AuthorizationException("User not authorized");
    return postRepository.countChildren(getAuthenticatedUserId(), parent);
  }

  @Override
  public List<PostModel> readOthersByParent(Long parent, Long child, Timestamp createdCursor, Long idCursor, Long limit) throws AuthorizationException {
    if (!canRead(parent)) throw new AuthorizationException("User not authorized");
    List<PostEntity> postEntities = postRepository.findRecentOtherChildren(
      getAuthenticatedUserId(),
      parent,
      child,
      createdCursor,
      idCursor,
      limit
    );
    return postEntities.stream().map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  @Override
  public List<PostModel> readOthersByParent(Long parent, Long child, Long popularityCursor, Long idCursor, Long limit) throws AuthorizationException {
    if (!canRead(parent)) throw new AuthorizationException("User not authorized");
    List<PostEntity> postEntities = postRepository.findPopularOtherChildren(
      getAuthenticatedUserId(),
      parent,
      child,
      popularityCursor,
      idCursor,
      limit
    );
    return postEntities.stream().map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  @Override
  public Long countOthersByParent(Long parent, Long child) throws AuthorizationException {
    if (!canRead(parent)) throw new AuthorizationException("User not authorized");
    return postRepository.countOtherChildren(getAuthenticatedUserId(), parent, child);
  }

  @Override
  @Transactional
  public boolean delete(Long id) throws AuthorizationException, PostException {
    Optional<PostModel> post = read(id);
    if (post.isEmpty()) return false;
    if (!getAuthenticatedUserId().equals(post.get().getUser().getId()))
      throw new AuthorizationException("Currently authenticated user is not authorized to delete post");
    try (
      Connection connection = dataSource.getConnection();
      CallableStatement stmt = connection.prepareCall("{call delete_post(?)}")
    ) {
      stmt.setLong(1, id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Error deleting post: {}", e.getMessage(), e);
      throw new PostException("Failed to delete post. SQL command error: " + e.getMessage(), e);
    }
    Long parent = null;
    if (Objects.nonNull(post.get().getParent())) {
      parent = post.get().getParent().getId();
    }
    publisher.publishEvent(new PostDeleteEvent(getAuthenticatedUserId(), id, parent, post.get().getForum().getId()));
    return true;
  }

  @Override
  @Transactional(timeout = 30)
  public boolean markAsRead(Long id) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!canRead(id)) throw new AuthorizationException("User not authorized");
    boolean marked = ratingRepository.markAsRead(user, id) > 0;
    if (marked) {
      read(id).ifPresent((post) -> { publisher.publishEvent(new ForumReadCountEvent(user, post.getForum().getId()));});
      if (notificationRepository.markPostAsRead(id, user) > 0)
        publisher.publishEvent(new NotificationReadEvent(user, notificationRepository.findId(user, NotificationType.POST, id)));
    }
    return marked;
  }

  @Override
  @Transactional(timeout = 30)
  public boolean markForumAsRead(Long forum) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!forumRepository.canRead(user, forum)) throw new AuthorizationException("User not authorized");
    boolean marked = ratingRepository.markForumAsRead(user, forum) > 0;
    if (marked) {
      publisher.publishEvent(new ForumReadCountEvent(user, forum));
    }
    return marked;
  }

  @Override
  @Transactional(timeout = 30)
  public boolean markAllAsRead() {
    Long user = getAuthenticatedUserId();
    boolean marked = ratingRepository.markAllAsRead(getAuthenticatedUserId()) > 0;
    if (marked) {
      publisher.publishEvent(new ForumReadCountEvent(user, null));
    }
    return marked;
  }

  @Transactional(timeout = 30)
  private PostEntity save(PostEntity post) throws AuthorizationException {
    Long userId = getAuthenticatedUserId();
    if (!forumRepository.canRead(post.getForum().getId(), userId)) throw new AuthorizationException("User not authorized");
    if (post.getParent() != null && !canRead(post.getParent().getId())) throw new AuthorizationException("User not authorized");
    ForumEntity forum = forumRepository.getReferenceById(post.getForum().getId());
    UserProfileEntity user = userProfileRepository.getReferenceById(userId);
    post.setForum(forum);
    post.setUser(user);

    post = postRepository.saveAndFlush(post);
    return post;
  }

  private Long getChildren(Long parent) {
    return postRepository.countChildren(getAuthenticatedUserId(), parent);
  }

  @Cacheable(
      value = POST_SECURITY_CACHE_NAME,
      key =
          "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.PostServiceImpl).CAN_READ_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #id)")
  @Override
  public boolean canRead(Long id) {
    if (!postRepository.canRead(id, getAuthenticatedUserId())) return false;
    PostEntity post = postRepository.getReferenceById(id);
    while (post.getParent() != null) {
      post = post.getParent();
      if (!postRepository.canRead(post.getId(), getAuthenticatedUserId())) return false;
    }
    return true;
  }
}
