package com.village.bellevue.service;

import java.util.List;

import com.village.bellevue.entity.EquipmentEntity;

public interface EquipmentService {

    public List<EquipmentEntity> search(String query);
}
