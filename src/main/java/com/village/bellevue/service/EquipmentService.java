package com.village.bellevue.service;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.error.AuthorizationException;

public interface EquipmentService {
  public void equip(Long item) throws AuthorizationException;
  public void unequip(Long item) throws AuthorizationException;
  public Page<EquipmentEntity> readAll(int page, int size);
  public Page<EquipmentEntity> readAllBySlot(String slot, int page, int size);
}
