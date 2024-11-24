import User from './User.js';
import Ingredient from './Ingredient.js';
import Equipment from './Equipment.js';
import Skill from './Skill.js';
import Step from './Step.js';

class Recipe {
  constructor(
    id,
    author,
    name,
    category,
    description,
    rating,
    vegan,
    vegetarian,
    glutenFree,
    pescetarian,
    allergen,
    ingredients,
    equipment,
    skills,
    steps
  ) {
    this.id = id;
    this.author = author;
    this.name = name;
    this.category = category;
    this.description = description;
    this.rating = rating;
    this.vegan = vegan;
    this.vegetarian = vegetarian;
    this.glutenFree = glutenFree;
    this.pescetarian = pescetarian;
    this.allergen = allergen;
    this.ingredients = ingredients;
    this.equipment = equipment;
    this.skills = skills;
    this.steps = steps;
  }

  static fromJSON(json) {
    return new Recipe(
      json.id,
      User.fromJSON(json.author),
      json.name,
      json.category,
      json.description,
      json.rating,
      json.vegan,
      json.vegetarian,
      json.glutenFree,
      json.pescetarian,
      json.allergen,
      json.ingredients?.map(Ingredient.fromJSON),
      json.equipment?.map((e) => Equipment.fromJSON(e.equipment)),
      json.skills?.map((s) => Skill.fromJSON(s.skill)),
      json.steps?.sort((a, b) => a.order - b.order).map(Step.fromJSON)
    );
  }

  toJSON() {
    return {
      id: this.id,
      author: this.author.toJSON(),
      name: this.name,
      category: this.category,
      description: this.description,
      rating: this.rating,
      vegan: this.vegan,
      vegetarian: this.vegetarian,
      glutenFree: this.glutenFree,
      pescetarian: this.pescetarian,
      allergen: this.allergen,
      ingredients: this.ingredients,
      equipment: this.equipment,
      skills: this.skills,
      steps: this.steps
    };
  }
}

export default Recipe;
