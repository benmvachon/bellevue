package com.village.bellevue.entity.id;

import java.io.Serializable;
import java.util.Objects;

import com.village.bellevue.entity.EquipmentEntity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class RecipeEquipmentId implements Serializable {
    private Long recipe;
    private EquipmentEntity equipment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeEquipmentId)) return false;
        RecipeEquipmentId that = (RecipeEquipmentId) o;
        return Objects.equals(recipe, that.recipe) &&
               Objects.equals(equipment.getId(), that.equipment.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, equipment.getId());
    }
}
