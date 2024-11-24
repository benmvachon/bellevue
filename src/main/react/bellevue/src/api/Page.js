class Page {
  constructor(
    content = [],
    totalPages = 0,
    totalElements = 0,
    last = true,
    first = true,
    numberOfElements = 0,
    size = 0,
    number = 0,
    empty = true
  ) {
    this.content = content;
    this.totalPages = totalPages;
    this.totalElements = totalElements;
    this.last = last;
    this.first = first;
    this.numberOfElements = numberOfElements;
    this.size = size;
    this.number = number;
    this.empty = empty;
  }

  static fromJSON(json) {
    return new Page(
      json.content,
      json.totalPages,
      json.totalElements,
      json.last,
      json.first,
      json.numberOfElements,
      json.size,
      json.number,
      json.empty
    );
  }

  toJSON() {
    return {
      content: this.content,
      totalPages: this.totalPages,
      totalElements: this.totalElements,
      last: this.last,
      first: this.first,
      numberOfElements: this.numberOfElements,
      size: this.size,
      number: this.number,
      empty: this.empty
    };
  }
}

export default Page;
