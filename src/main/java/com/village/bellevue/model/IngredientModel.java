package com.village.bellevue.model;

import com.village.bellevue.entity.RecipeIngredientEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientModel {

    private Long id;
    private String name;
    private double quantity;
    private String unit;

    public IngredientModel(RecipeIngredientEntity ingredient) {
        this.id = ingredient.getIngredient().getId();
        this.name = ingredient.getIngredient().getName();
        this.quantity = ingredient.getQuantity();
        this.unit = ingredient.getUnit();
    }
}