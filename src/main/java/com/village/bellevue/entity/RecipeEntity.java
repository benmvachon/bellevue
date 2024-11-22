package com.village.bellevue.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "recipe")
public class RecipeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "author", nullable = false)
  private ScrubbedUserEntity author;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private RecipeCategory category;

  private Boolean pescetarian = true;
  private Boolean vegetarian = true;
  private Boolean vegan = true;
  private Boolean glutenFree = true;
  private String allergen;

  private Timestamp created = new Timestamp(System.currentTimeMillis());
  private Timestamp updated = new Timestamp(System.currentTimeMillis());

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<RecipeStepEntity> steps;

  public void addStep(RecipeStepEntity step) {
    steps.add(step);
  }

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<RecipeIngredientEntity> ingredients;

  public void addIngredient(RecipeIngredientEntity ingredient) {
    ingredients.add(ingredient);
  }

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<RecipeSkillEntity> skills;

  public void addSkill(RecipeSkillEntity skill) {
    skills.add(skill);
  }

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<RecipeEquipmentEntity> equipment;

  public void addEquipment(RecipeEquipmentEntity equipment) {
    this.equipment.add(equipment);
  }

  public enum RecipeCategory {
    SOUP,
    SALAD,
    SNACK,
    SIDE,
    MAIN,
    DESSERT,
    COCKTAIL,
    SMOOTHIE;

    @JsonValue
    public String toValue() {
      return this.name().toLowerCase();
    }

    @JsonCreator
    public static RecipeCategory fromString(String value) {
      return RecipeCategory.valueOf(value.toUpperCase());
    }
  }
}
