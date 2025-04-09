package com.village.bellevue.service.impl;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
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
import com.village.bellevue.model.PostModel;
import com.village.bellevue.model.PostModelProvider;
import com.village.bellevue.repository.AggregateRatingRepository;
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.repository.PostRepository;
import com.village.bellevue.repository.ProfileRepository;
import com.village.bellevue.service.PostService;

import jakarta.persistence.EntityManager;

@Service
public class PostServiceImpl implements PostService {

  public static final String CAN_READ_CACHE_KEY = "canRead";

  private final PostRepository postRepository;
  private final AggregateRatingRepository ratingRepository;
  private final ForumRepository forumRepository;
  private final ProfileRepository profileRepository;
  private final EntityManager entityManager;

  public PostServiceImpl(
      PostRepository postRepository,
      AggregateRatingRepository ratingRepository,
      ForumRepository forumRepository,
      ProfileRepository profileRepository,
      EntityManager entityManager) {
    this.postRepository = postRepository;
    this.ratingRepository = ratingRepository;
    this.forumRepository = forumRepository;
    this.profileRepository = profileRepository;
    this.entityManager = entityManager;
  }

  @Transactional
  @Override
  public PostModel create(PostEntity post) throws AuthorizationException {
    return new PostModel(save(post), Optional.empty(), 0l);
  }

  @Override
  public Optional<PostModel> read(Long id) throws AuthorizationException {
    if (canRead(id)) {
      Optional<PostEntity> post = postRepository.findById(id);
      if (post.isPresent()) {
        PostEntity postEntity = post.get();

        PostModelProvider helper = new PostModelProvider() {
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
        };

        try {
          return Optional.of(new PostModel(postEntity, helper));
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
  public Page<PostModel> readAllByForum(Long forum, int page, int size) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!forumRepository.canRead(forum, user)) throw new AuthorizationException("User not authorized");
    Page<PostEntity> postEntities = postRepository.findAllTopLevelByForum(
      user,
      forum,
      PageRequest.of(page, size)
    );
    try {
      return postEntities.map(post -> {
        return new PostModel(post, ratingRepository.findById(new AggregateRatingId(user, post.getId())), getChildren(post.getId()));
      });
    } finally {
      ForumEntity forumEntity = new ForumEntity();
      forumEntity.setId(forum);
      profileRepository.setLocation(getAuthenticatedUserId(), forumEntity);
    }
  }

  @Override
  public Page<PostModel> readAllByParent(Long parent, int page, int size) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!canRead(parent)) throw new AuthorizationException("User not authorized");
    Page<PostEntity> postEntities = postRepository.findAllChildren(
      user,
      parent,
      PageRequest.of(page, size)
    );
    return postEntities.map(post -> {
      return new PostModel(post, ratingRepository.findById(new AggregateRatingId(user, post.getId())), getChildren(post.getId()));
    });
  }

  @Override
  public boolean delete(Long id) throws AuthorizationException {
    Optional<PostModel> post = read(id);
    if (post.isEmpty()) return false;
    if (!getAuthenticatedUserId().equals(post.get().getUser().getUser()))
      throw new AuthorizationException("Currently authenticated user is not authorized to delete post");
    postRepository.deleteById(id);
    return true;
  }

  private PostEntity save(PostEntity post) throws AuthorizationException {
    Long userId = getAuthenticatedUserId();
    if (!forumRepository.canRead(post.getForum().getId(), userId)) throw new AuthorizationException("User not authorized");
    ForumEntity forum = entityManager.getReference(ForumEntity.class, post.getForum().getId());
    UserProfileEntity user = entityManager.getReference(UserProfileEntity.class, getAuthenticatedUserId());
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
    return postRepository.canRead(id, getAuthenticatedUserId());
  }
}
