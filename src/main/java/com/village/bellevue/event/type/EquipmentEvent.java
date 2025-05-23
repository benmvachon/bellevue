package com.village.bellevue.event.type;

import com.village.bellevue.entity.EquipmentEntity;

import lombok.Data;

@Data
public class EquipmentEvent implements UserEvent {
  private final Long user;
  private final EquipmentEntity equipment;
}
