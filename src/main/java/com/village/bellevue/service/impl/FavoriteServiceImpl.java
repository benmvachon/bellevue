package com.village.bellevue.service.impl;

import static com.village.bellevue.config.security.SecurityConfig.*;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.village.bellevue.entity.FavoriteEntity;
import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.entity.id.FavoriteId;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.repository.FavoriteRepository;
import com.village.bellevue.service.FavoriteService;
import com.village.bellevue.service.ForumService;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.PostService;

@Service
public class FavoriteServiceImpl implements FavoriteService {
  private final FavoriteRepository favoriteRepository;
  private final PostService postService;
  private final ForumService forumService;
  private final FriendService friendService;

  public FavoriteServiceImpl(
    FavoriteRepository favoriteRepository,
    PostService postService,
    ForumService forumService,
    FriendService friendService
  ) {
    this.favoriteRepository = favoriteRepository;
    this.postService = postService;
    this.forumService = forumService;
    this.friendService = friendService;
  }

  @Override
  public Optional<FavoriteEntity> favoritePost(Long post) {
    if (postService.canRead(post)) return Optional.of(create(FavoriteType.POST, post));
    return Optional.empty();
  }

  @Override
  public Optional<FavoriteEntity> favoriteForum(Long forum) {
    if (forumService.canRead(forum)) return Optional.of(create(FavoriteType.FORUM, forum));
    return Optional.empty();
  }

  @Override
  public Optional<FavoriteEntity> favoriteProfile(Long user) {
    try {
      if (friendService.isFriend(user)) return Optional.of(create(FavoriteType.PROFILE, user));
    } catch (FriendshipException e) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  @Override
  public Page<FavoriteEntity> readAll(int page, int size) {
    return favoriteRepository.findByUser(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Page<FavoriteEntity> readAllOfType(FavoriteType type, int page, int size) {
    return favoriteRepository.findByUserAndType(getAuthenticatedUserId(), type, PageRequest.of(page, size));
  }

  @Override
  public void unfavoritePost(Long post) {
    delete(FavoriteType.POST, post);
  }

  @Override
  public void unfavoriteForum(Long forum) {
    delete(FavoriteType.FORUM, forum);
  }

  @Override
  public void unfavoriteProfile(Long user) {
    delete(FavoriteType.PROFILE, user);
  }

  private void delete(FavoriteType type, Long entity) {
    favoriteRepository.deleteById(new FavoriteId(getAuthenticatedUserId(), type, entity));
  }

  private FavoriteEntity create(FavoriteType type, Long entity) {
    FavoriteEntity favorite = new FavoriteEntity();
    favorite.setType(type);
    favorite.setEntity(entity);
    return save(favorite);
  }

  private FavoriteEntity save(FavoriteEntity favorite) {
    favorite.setUser(getAuthenticatedUserId());
    return favoriteRepository.save(favorite);
  }
}
