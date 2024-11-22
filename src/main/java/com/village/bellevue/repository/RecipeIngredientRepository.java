package com.village.bellevue.repository;

import com.village.bellevue.entity.RecipeIngredientEntity;
import com.village.bellevue.entity.id.RecipeIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeIngredientRepository
    extends JpaRepository<RecipeIngredientEntity, RecipeIngredientId> {
  void deleteAllByRecipe(Long recipe);
}
