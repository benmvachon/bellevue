package com.village.bellevue.entity;

import com.village.bellevue.entity.id.RecipeStepId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "recipe_step")
@IdClass(RecipeStepId.class)
public class RecipeStepEntity {
  @Id private Long recipe;

  @Id
  @Column(name = "`order`")
  private Integer order;

  private String step;
}
