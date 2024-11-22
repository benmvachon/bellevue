package com.village.bellevue.repository;

import com.village.bellevue.entity.RecipeEquipmentEntity;
import com.village.bellevue.entity.id.RecipeEquipmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeEquipmentRepository
    extends JpaRepository<RecipeEquipmentEntity, RecipeEquipmentId> {
  void deleteAllByRecipe(Long recipe);
}
