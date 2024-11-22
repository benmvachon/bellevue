package com.village.bellevue.service;

import com.village.bellevue.entity.EquipmentEntity;
import java.util.List;

public interface EquipmentService {

  public List<EquipmentEntity> search(String query);
}
