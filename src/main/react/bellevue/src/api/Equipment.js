class Equipment {
  constructor(id, name) {
    this.id = id;
    this.name = name;
  }

  static fromJSON(json) {
    return new Equipment(json.id, json.name);
  }

  toJSON() {
    return {
      id: this.id,
      name: this.name
    };
  }
}

export default Equipment;
