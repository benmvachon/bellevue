package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.village.bellevue.controller.EquipmentController;
import com.village.bellevue.entity.EquipmentEntity;

@Component
public class EquipmentModelAssembler implements RepresentationModelAssembler<EquipmentEntity, EntityModel<EquipmentEntity>> {
  @Override
  public EntityModel<EquipmentEntity> toModel(EquipmentEntity equipment) {
    return EntityModel.of(
      equipment,
      linkTo(methodOn(EquipmentController.class).equip(equipment.getItem())).withRel("equip"),
      linkTo(methodOn(EquipmentController.class).unequip(equipment.getItem())).withRel("unequip")
    );
  }
}

