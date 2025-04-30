class Favorite {
  constructor(type, entity, created, _links) {
    this.type = type;
    this.entity = entity;
    this.created = created ? new Date(created) : undefined;
    // TODO: process _links
  }

  static favoriteMapper = {
    fromPage(_embedded) {
      const favorites = [];
      if (_embedded) {
        for (const favorite of _embedded.favoriteEntityList) {
          favorites.push(Favorite.fromJSON(favorite));
        }
      }
      return favorites;
    }
  };

  static fromJSON(json) {
    return new Favorite(json.type, json.entity, json.created);
  }

  toJSON() {
    return {
      type: this.type,
      entity: this.entity,
      created: this.created?.toString()
    };
  }
}

export default Favorite;
