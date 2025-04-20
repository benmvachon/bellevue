class Category {
  constructor(name, _links) {
    this.name = name;
    // TODO: process _links
  }

  static categoryMapper = {
    fromPage(_embedded) {
      const categories = [];
      if (_embedded) {
        for (const category of _embedded.categoryModelList) {
          categories.push(Category.fromJSON(category));
        }
      }
      return categories;
    }
  };

  static fromJSON(json) {
    return new Category(json.name);
  }

  toJSON() {
    return {
      name: this.name
    };
  }
}

export default Category;
