package com.village.bellevue.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.service.EquipmentService;
import com.village.bellevue.service.RatingService;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

  private final EquipmentService equipmentService;

  public EquipmentController(EquipmentService equipmentService, RatingService ratingService) {
    this.equipmentService = equipmentService;
  }

  @GetMapping
  public ResponseEntity<Page<EquipmentEntity>> readAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size) {
    Page<EquipmentEntity> equipments = equipmentService.readAll(page, size);
    return ResponseEntity.status(HttpStatus.OK).body(equipments);
  }

  @GetMapping("/slot/{slot}")
  public ResponseEntity<Page<EquipmentEntity>> readAllBySlot(
      @PathVariable String slot,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size) {
    Page<EquipmentEntity> equipments = equipmentService.readAllBySlot(slot, page, size);
    return ResponseEntity.status(HttpStatus.OK).body(equipments);
  }

  @PutMapping("/{item}/equip")
  public ResponseEntity<String> equip(@PathVariable String item) {
    try {
      equipmentService.equip(item);
      return ResponseEntity.ok("Item equipped.");
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @PutMapping("/{item}/unequip")
  public ResponseEntity<String> unequip(@PathVariable String item) {
    try {
      equipmentService.unequip(item);
      return ResponseEntity.ok("Item equipped.");
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
