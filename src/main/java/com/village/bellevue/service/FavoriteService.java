package com.village.bellevue.service;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.FavoriteEntity;
import com.village.bellevue.error.RecipeException;

public interface FavoriteService {
  Page<FavoriteEntity> read(int page, int size);

  void add(Long recipe) throws RecipeException;

  void remove(Long recipe);
}
