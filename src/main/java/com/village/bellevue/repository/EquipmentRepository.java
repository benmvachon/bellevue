package com.village.bellevue.repository;

import com.village.bellevue.entity.EquipmentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Long> {

  List<EquipmentEntity> findByNameStartingWithIgnoreCase(String prefix);
}
