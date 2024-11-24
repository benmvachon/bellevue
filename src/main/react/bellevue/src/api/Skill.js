class Skill {
  constructor(id, name) {
    this.id = id;
    this.name = name;
  }

  static fromJSON(json) {
    return new Skill(json.id, json.name);
  }

  toJSON() {
    return {
      id: this.id,
      name: this.name
    };
  }
}

export default Skill;
