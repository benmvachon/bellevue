package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.FavoriteEntity;
import com.village.bellevue.entity.FavoriteEntity.FavoriteType;

public interface FavoriteService {
  public Optional<FavoriteEntity> favoritePost(Long post);
  public Optional<FavoriteEntity> favoriteForum(Long forum);
  public Optional<FavoriteEntity> favoriteProfile(Long user);
  public Page<FavoriteEntity> readAll(int page, int size);
  public Page<FavoriteEntity> readAllOfType(FavoriteType type, int page, int size);
  public void unfavoritePost(Long post);
  public void unfavoriteForum(Long forum);
  public void unfavoriteProfile(Long user);
}
