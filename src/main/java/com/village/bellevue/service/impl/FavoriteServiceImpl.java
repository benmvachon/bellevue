package com.village.bellevue.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.FavoriteEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.id.FavoriteId;
import com.village.bellevue.error.RecipeException;
import com.village.bellevue.repository.FavoriteRepository;
import com.village.bellevue.repository.RecipeRepository;
import com.village.bellevue.service.FavoriteService;

@Service
public class FavoriteServiceImpl implements FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final RecipeRepository recipeRepository;

  public FavoriteServiceImpl(FavoriteRepository favoriteRepository, RecipeRepository recipeRepository) {
    this.favoriteRepository = favoriteRepository;
    this.recipeRepository = recipeRepository;
  }

  @Override
  public Page<FavoriteEntity> read(int page, int size) {
    return favoriteRepository.findAllByUser(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public void add(Long recipe) throws RecipeException{
    Long user = getAuthenticatedUserId();
    if (!recipeRepository.canRead(recipe, user)) {
      throw new RecipeException("Cannot find recipe: " + recipe);
    }
    RecipeEntity recipeEntity = recipeRepository.findById(recipe).orElseThrow(() -> new RecipeException("Cannot find recipe: " + recipe));
    favoriteRepository.save(new FavoriteEntity(user, recipeEntity));
  }

  @Override
  public void remove(Long recipe) {
    favoriteRepository.deleteById(new FavoriteId(getAuthenticatedUserId(), recipe));
  }
}
