package com.village.bellevue.service;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.RecipeException;
import com.village.bellevue.model.RecipeModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface RecipeService {

  public RecipeModel create(RecipeEntity recipe) throws AuthorizationException, RecipeException;

  public Optional<RecipeModel> read(Long id) throws AuthorizationException, RecipeException;

  public Optional<AggregateRatingEntity> readRating(Long id) throws AuthorizationException;

  public Page<SimpleRecipeEntity> readAll(Long author, int page, int size)
      throws AuthorizationException, RecipeException;

  public RecipeModel update(Long id, RecipeEntity updatedRecipe)
      throws AuthorizationException, RecipeException;

  public Long cook(Long id) throws AuthorizationException, RecipeException;

  public void delete(Long id) throws AuthorizationException, RecipeException;

  public Page<SimpleRecipeEntity> search(
      String query,
      List<Long> ingredients,
      List<Long> skills,
      List<Long> equipment,
      int page,
      int size)
      throws AuthorizationException, RecipeException;

  public boolean canRead(Long id);

  public boolean canUpdate(Long id);
}
