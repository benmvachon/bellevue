class Ingredient {
  constructor(id, name, quantity, unit) {
    this.id = id;
    this.name = name;
    this.quantity = quantity;
    this.unit = unit;
  }

  static fromJSON(json) {
    return new Ingredient(json.id, json.name, json.quantity, json.unit);
  }

  toJSON() {
    return {
      id: this.id,
      name: this.name,
      quantity: this.quantity,
      unit: this.unit
    };
  }
}

export default Ingredient;
