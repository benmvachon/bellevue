package com.village.bellevue.entity;

import com.village.bellevue.entity.id.RecipeIngredientId;
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
@Table(name = "recipe_ingredient")
@IdClass(RecipeIngredientId.class)
public class RecipeIngredientEntity {
  @Id private Long recipe;

  @Id
  @ManyToOne
  @JoinColumn(name = "ingredient", nullable = false)
  private IngredientEntity ingredient;

  private Double quantity;
  private String unit;
}
