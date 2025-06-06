package com.village.bellevue.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.entity.id.EquipmentId;

@Repository
public interface EquipmentRepository extends JpaRepository<EquipmentEntity, EquipmentId> {
  Page<EquipmentEntity> findByUser(Long user, Pageable pageable);

  @Query(
    "SELECT e FROM EquipmentEntity e " +
    "WHERE e.user = :user AND e.item.slot = :slot"
  )
  @Transactional(readOnly = true)
  Page<EquipmentEntity> findByUserAndSlot(Long user, String slot, Pageable pageable);

  @Query(
    "SELECT DISTINCT e FROM EquipmentEntity e " +
    "WHERE e.user = :user AND e.item.slot = :slot AND e.equipped = true"
  )
  @Transactional(readOnly = true)
  Optional<EquipmentEntity> findByUserAndSlotAndEquippedTrue(Long user, String slot);
}
