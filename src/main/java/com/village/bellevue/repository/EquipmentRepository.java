package com.village.bellevue.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.entity.id.EquipmentId;

@Repository
public interface EquipmentRepository extends JpaRepository<EquipmentEntity, EquipmentId> {
  Page<EquipmentEntity> findByUser(Long user, Pageable pageable);
  Page<EquipmentEntity> findByUserAndSlot(Long user, String slot, Pageable pageable);
  Optional<EquipmentEntity> findByUserAndSlotAndEquippedTrue(Long user, String slot);
}
