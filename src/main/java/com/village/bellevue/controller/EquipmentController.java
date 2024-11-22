package com.village.bellevue.controller;

import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.service.EquipmentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

  private final EquipmentService equipmentService;

  public EquipmentController(EquipmentService equipmentService) {
    this.equipmentService = equipmentService;
  }

  @GetMapping("/search")
  public ResponseEntity<List<EquipmentEntity>> search(@RequestParam String query) {
    List<EquipmentEntity> equipment = equipmentService.search(query);
    return ResponseEntity.ok(equipment);
  }
}
