class Step {
  constructor(order, description) {
    this.order = order;
    this.description = description;
  }

  static fromJSON(json) {
    return new Step(json.order, json.step);
  }

  toJSON() {
    return {
      order: this.order,
      description: this.description
    };
  }
}

export default Step;
