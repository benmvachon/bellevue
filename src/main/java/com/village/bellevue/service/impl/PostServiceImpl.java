package com.village.bellevue.service.impl;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.CacheConfig.POST_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.entity.id.AggregateRatingId;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.event.PostEvent;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.model.ForumModelProvider;
import com.village.bellevue.model.PostModel;
import com.village.bellevue.model.PostModelProvider;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.repository.AggregateRatingRepository;
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.repository.PostRepository;
import com.village.bellevue.repository.ProfileRepository;
import com.village.bellevue.repository.UserProfileRepository;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.PostService;

@Service
public class PostServiceImpl implements PostService {

  public static final String CAN_READ_CACHE_KEY = "canRead";

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
      return new ProfileModel(user, (Long user1) -> {
        try {
          if (getAuthenticatedUserId().equals(user1)) {
            return Optional.of("self");
          }
          Optional<String> friendshipStatus = friendService.getStatus(user1);
          if (friendshipStatus.isEmpty()) return Optional.of("unset");
          return friendshipStatus;
        } catch (FriendshipException ex) {
          return Optional.of("unset");
        }
      });
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
            return Optional.of(new ProfileModel(userProfileRepository.getReferenceById(user), (Long user1) -> {
              try {
                if (getAuthenticatedUserId().equals(user1)) {
                  return Optional.of("self");
                }
                Optional<String> friendshipStatus = friendService.getStatus(user1);
                if (friendshipStatus.isEmpty()) return Optional.of("unset");
                return friendshipStatus;
              } catch (FriendshipException ex) {
                return Optional.of("unset");
              }
            }));
          }
          return Optional.empty();
        }
      });
    }
  };

  private final PostRepository postRepository;
  private final AggregateRatingRepository ratingRepository;
  private final ForumRepository forumRepository;
  private final ProfileRepository profileRepository;
  private final UserProfileRepository userProfileRepository;
  private final FriendService friendService;
  private final ApplicationEventPublisher publisher;

  public PostServiceImpl(
    PostRepository postRepository,
    AggregateRatingRepository ratingRepository,
    ForumRepository forumRepository,
    ProfileRepository profileRepository,
    UserProfileRepository userProfileRepository,
    FriendService friendService,
    ApplicationEventPublisher publisher
  ) {
    this.postRepository = postRepository;
    this.ratingRepository = ratingRepository;
    this.forumRepository = forumRepository;
    this.profileRepository = profileRepository;
    this.userProfileRepository = userProfileRepository;
    this.friendService = friendService;
    this.publisher = publisher;
  }

  @Override
  @Transactional
  public PostModel post(Long forum, String content) throws AuthorizationException {
    PostModel model = null;
    try {
      PostEntity post = new PostEntity();
      ForumEntity forumEntity = forumRepository.getReferenceById(forum);
      post.setForum(forumEntity);
      post.setContent(content);
      model = new PostModel(save(post), postModelProvider);
      return model;
    } finally {
      if (model != null) {
        publisher.publishEvent(new PostEvent(getAuthenticatedUserId(), model));
      }
    }
  }

  @Override
  @Transactional
  public PostModel reply(Long forum, Long parent, String content) throws AuthorizationException {
    PostModel model = null;
    try {
      PostEntity post = new PostEntity();
      ForumEntity forumEntity = forumRepository.getReferenceById(forum);
      PostEntity parentEntity = postRepository.getReferenceById(parent);
      post.setForum(forumEntity);
      post.setParent(parentEntity);
      post.setContent(content);
      model = new PostModel(save(post), postModelProvider);
      return model;
    } finally {
      if (model != null) {
        publisher.publishEvent(new PostEvent(getAuthenticatedUserId(), model));
      }
    }
  }

  @Override
  public Optional<PostModel> read(Long id) throws AuthorizationException {
    if (canRead(id)) {
      Optional<PostEntity> post = postRepository.findById(id);
      if (post.isPresent()) {
        PostEntity postEntity = post.get();
        try {
          return Optional.of(new PostModel(postEntity, postModelProvider));
        } finally {
          profileRepository.setLocation(getAuthenticatedUserId(), postEntity.getForum());
        }
      }
      return Optional.empty();
    }
    throw new AuthorizationException(
        "Currently authenticated user is not authorized to read post");
  }

  @Override
  public Page<PostModel> readAllByForum(Long forum, int page, int size, boolean sortByRelevance) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!forumRepository.canRead(forum, user)) throw new AuthorizationException("User not authorized");
    Page<PostEntity> postEntities = sortByRelevance ?
      postRepository.findRelevantTopLevelByForum(
        user,
        forum,
        PageRequest.of(page, size)
      ) : 
      postRepository.findRecentTopLevelByForum(
        user,
        forum,
        PageRequest.of(page, size)
      );
    try {
      return postEntities.map(post -> {
        try {
          return new PostModel(post, postModelProvider);
        } catch (AuthorizationException e) {
          return null;
        }
      });
    } finally {
      ForumEntity forumEntity = new ForumEntity();
      forumEntity.setId(forum);
      profileRepository.setLocation(getAuthenticatedUserId(), forumEntity);
    }
  }

  @Override
  public Page<PostModel> readAllByParent(Long parent, int page, int size, boolean sortByRelevance) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!canRead(parent)) throw new AuthorizationException("User not authorized");
    Page<PostEntity> postEntities = sortByRelevance ?
      postRepository.findRelevantChildren(
        user,
        parent,
        PageRequest.of(page, size)
      ) :
      postRepository.findRecentChildren(
        user,
        parent,
        PageRequest.of(page, size)
      );
    return postEntities.map(post -> {
      try {
        return new PostModel(post, postModelProvider);
      } catch (AuthorizationException e) {
        return null;
      }
    });
  }

  @Override
  @Transactional
  public boolean delete(Long id) throws AuthorizationException {
    Optional<PostModel> post = read(id);
    if (post.isEmpty()) return false;
    if (!getAuthenticatedUserId().equals(post.get().getUser().getId()))
      throw new AuthorizationException("Currently authenticated user is not authorized to delete post");
    postRepository.deleteById(id);
    return true;
  }

  private PostEntity save(PostEntity post) throws AuthorizationException {
    Long userId = getAuthenticatedUserId();
    if (!forumRepository.canRead(post.getForum().getId(), userId)) throw new AuthorizationException("User not authorized");
    if (post.getParent() != null && !canRead(post.getParent().getId())) throw new AuthorizationException("User not authorized");
    ForumEntity forum = forumRepository.getReferenceById(post.getForum().getId());
    UserProfileEntity user = userProfileRepository.getReferenceById(userId);
    post.setForum(forum);
    post.setUser(user);
    profileRepository.setLocation(userId, forum);

    return postRepository.save(post);
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
