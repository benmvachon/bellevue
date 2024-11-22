package com.village.bellevue.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.service.EquipmentService;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public List<EquipmentEntity> search(String query) {
        return equipmentRepository.findByNameStartingWithIgnoreCase(query);
    }
}
