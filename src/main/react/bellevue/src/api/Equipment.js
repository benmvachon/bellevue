class Equipment {
  constructor(user, item, equipped, unlocked, _links) {
    this.id = item?.id;
    this.user = user;
    this.name = item?.name;
    this.slot = item?.slot;
    this.equipped = equipped;
    this.unlocked = unlocked ? new Date(unlocked) : undefined;
    // TODO: process _links
  }

  static equipmentMapper = {
    fromPage(_embedded) {
      const equipments = [];
      if (_embedded) {
        for (const equipment of _embedded.equipmentEntityList) {
          equipments.push(Equipment.fromJSON(equipment));
        }
      }
      return equipments;
    }
  };

  static fromJSON(json) {
    return new Equipment(json.user, json.item, json.equipped, json.unlocked);
  }

  toJSON() {
    return {
      user: this.user,
      item: {
        id: this.id,
        name: this.name,
        slot: this.slot
      },
      equipped: this.equipped,
      unlocked: this.unlocked?.toString()
    };
  }
}

export default Equipment;
