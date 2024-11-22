package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.RecipeSkillEntity;
import com.village.bellevue.entity.id.RecipeSkillId;

@Repository
public interface RecipeSkillRepository extends JpaRepository<RecipeSkillEntity, RecipeSkillId> {
    void deleteAllByRecipe(Long recipe);
}
