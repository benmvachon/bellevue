package com.village.bellevue.repository;

import com.village.bellevue.entity.IngredientEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {

  List<IngredientEntity> findByNameStartingWithIgnoreCase(String prefix);
}
