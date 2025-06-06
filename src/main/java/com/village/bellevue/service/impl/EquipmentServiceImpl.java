package com.village.bellevue.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.entity.ItemEntity;
import com.village.bellevue.entity.id.EquipmentId;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.service.EquipmentService;

@Service
public class EquipmentServiceImpl implements EquipmentService {

  private final EquipmentRepository equipmentRepository;

  public EquipmentServiceImpl(EquipmentRepository equipmentRepository) {
    this.equipmentRepository = equipmentRepository;
  }

  @Override
  @Transactional(timeout = 30)
  public void equip(Long item) throws AuthorizationException {
    Optional<EquipmentEntity> equipment = equipmentRepository.findById(new EquipmentId(getAuthenticatedUserId(), new ItemEntity(item)));
    if (equipment.isPresent()) {
      EquipmentEntity equipmentEntity = equipment.get();
      Optional<EquipmentEntity> equipped = equipmentRepository.findByUserAndSlotAndEquippedTrue(getAuthenticatedUserId(), equipmentEntity.getItem().getSlot());
      if (equipped.isPresent()) {
        EquipmentEntity equippedEntity = equipped.get();
        equippedEntity.setEquipped(false);
        equipmentRepository.save(equippedEntity);
      }
      equipmentEntity.setEquipped(true);
      equipmentRepository.save(equipmentEntity);
    }
  }

  @Override
  @Transactional(timeout = 30)
  public void unequip(Long item) throws AuthorizationException {
    Optional<EquipmentEntity> equipment = equipmentRepository.findById(new EquipmentId(getAuthenticatedUserId(), new ItemEntity(item)));
    if (equipment.isPresent()) {
      EquipmentEntity equipmentEntity = equipment.get();
      equipmentEntity.setEquipped(false);
      equipmentRepository.save(equipmentEntity);
    }
  }

  @Override
  public Page<EquipmentEntity> readAll(int page, int size) {
    return equipmentRepository.findByUser(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Page<EquipmentEntity> readAllBySlot(String slot, int page, int size) {
    return equipmentRepository.findByUserAndSlot(getAuthenticatedUserId(), slot, PageRequest.of(page, size));
  }
  
}
