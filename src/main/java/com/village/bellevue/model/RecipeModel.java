package com.village.bellevue.model;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.RecipeEntity.RecipeCategory;
import com.village.bellevue.entity.RecipeEquipmentEntity;
import com.village.bellevue.entity.RecipeIngredientEntity;
import com.village.bellevue.entity.RecipeSkillEntity;
import com.village.bellevue.entity.RecipeStepEntity;
import com.village.bellevue.entity.ScrubbedUserEntity;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeModel {

  private Long id;

  private String name;
  private ScrubbedUserEntity author;
  private String description;
  private RecipeCategory category;
  private Double rating;

  private Boolean pescetarian;
  private Boolean vegetarian;
  private Boolean vegan;
  private Boolean glutenFree;
  private String allergen;

  private Timestamp created;
  private Timestamp updated;

  private Map<Integer, String> steps;
  private Set<IngredientModel> ingredients;
  private Map<Long, String> skills; // skill id -> skill name
  private Map<Long, String> equipment; // equipment id -> equipment name

  public RecipeModel(RecipeEntity recipe, Optional<AggregateRatingEntity> rating) {
    this.id = recipe.getId();
    this.name = recipe.getName();
    this.author = recipe.getAuthor();
    this.description = recipe.getDescription();
    this.category = recipe.getCategory();

    this.pescetarian = recipe.getPescetarian();
    this.vegetarian = recipe.getVegetarian();
    this.vegan = recipe.getVegan();
    this.glutenFree = recipe.getGlutenFree();
    this.allergen = recipe.getAllergen();

    this.created = recipe.getCreated();
    this.updated = recipe.getUpdated();

    this.steps = new HashMap<>();
    if (recipe.getSteps() != null) {
      for (RecipeStepEntity step : recipe.getSteps()) {
        this.steps.put(step.getOrder(), step.getStep());
      }
    }
    this.ingredients = new HashSet<>();
    if (recipe.getIngredients() != null) {
      for (RecipeIngredientEntity ingredient : recipe.getIngredients()) {
        this.ingredients.add(new IngredientModel(ingredient));
      }
    }
    this.skills = new HashMap<>();
    if (recipe.getSkills() != null) {
      for (RecipeSkillEntity skill : recipe.getSkills()) {
        this.skills.put(skill.getSkill().getId(), skill.getSkill().getName());
      }
    }
    this.equipment = new HashMap<>();
    if (recipe.getEquipment() != null) {
      for (RecipeEquipmentEntity equipmentEntity : recipe.getEquipment()) {
        this.equipment.put(
            equipmentEntity.getEquipment().getId(), equipmentEntity.getEquipment().getName());
      }
    }

    if (rating.isPresent()) {
      this.rating = rating.get().getRating();
    } else {
      this.rating = 0.0;
    }
  }
}
