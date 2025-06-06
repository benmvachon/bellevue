class Page {
  constructor(page, _embedded, _links, contentMapper) {
    this.size = page.size;
    this.totalPages = page.totalPages;
    this.totalElements = page.totalElements;
    this.number = page.number;

    this.content = contentMapper?.fromPage(_embedded) || [];

    // TODO: process _links
  }

  static fromJSON(json, contentMapper) {
    return new Page(json.page, json._embedded, json._links, contentMapper);
  }

  toJSON() {
    return {
      size: this.size,
      totalPages: this.totalPages,
      totalElements: this.totalElements,
      number: this.number,
      content: this.content
    };
  }
}

export default Page;
