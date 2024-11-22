package com.village.bellevue.repository;

import com.village.bellevue.entity.RecipeStepEntity;
import com.village.bellevue.entity.id.RecipeStepId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeStepRepository extends JpaRepository<RecipeStepEntity, RecipeStepId> {
  void deleteAllByRecipe(Long recipe);
}
