package com.village.bellevue.entity;

import com.village.bellevue.entity.id.RecipeEquipmentId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "recipe_equipment")
@IdClass(RecipeEquipmentId.class)
public class RecipeEquipmentEntity {
    @Id
    private Long recipe;

    @Id
    @ManyToOne
    @JoinColumn(name = "equipment", nullable = false)
    private EquipmentEntity equipment;
}
