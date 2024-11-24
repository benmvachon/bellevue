package com.village.bellevue.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.RecipeEntity.RecipeCategory;
import com.village.bellevue.entity.RecipeEquipmentEntity;
import com.village.bellevue.entity.RecipeIngredientEntity;
import com.village.bellevue.entity.RecipeSkillEntity;
import com.village.bellevue.entity.RecipeStepEntity;
import com.village.bellevue.entity.ScrubbedUserEntity;

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

  private Set<RecipeStepEntity> steps;
  private Set<IngredientModel> ingredients;
  private Set<RecipeSkillEntity> skills;
  private Set<RecipeEquipmentEntity> equipment;

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

    this.steps = recipe.getSteps();
    this.ingredients = new HashSet<>();
    if (recipe.getIngredients() != null) {
      for (RecipeIngredientEntity ingredient : recipe.getIngredients()) {
        this.ingredients.add(new IngredientModel(ingredient));
      }
    }
    this.skills = recipe.getSkills();
    this.equipment = recipe.getEquipment();

    if (rating.isPresent()) {
      this.rating = rating.get().getRating();
    } else {
      this.rating = 0.0;
    }
  }
}
