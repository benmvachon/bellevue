package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.RecipeEquipmentEntity;
import com.village.bellevue.entity.id.RecipeEquipmentId;

@Repository
public interface RecipeEquipmentRepository extends JpaRepository<RecipeEquipmentEntity, RecipeEquipmentId> {
    void deleteAllByRecipe(Long recipe);
}
