package com.village.bellevue.repository;

import com.village.bellevue.entity.RecipeSkillEntity;
import com.village.bellevue.entity.id.RecipeSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeSkillRepository extends JpaRepository<RecipeSkillEntity, RecipeSkillId> {
  void deleteAllByRecipe(Long recipe);
}
