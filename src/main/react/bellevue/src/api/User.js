class User {
  constructor(id, name, username, status = 'OFFLINE', avatar = 'CAT') {
    this.id = id;
    this.name = name;
    this.username = username;
    this.status = status;
    this.avatar = avatar;
  }

  static fromJSON(json) {
    return new User(
      json.id,
      json.name,
      json.username,
      json.status,
      json.avatar
    );
  }

  toJSON() {
    return {
      id: this.id,
      name: this.name,
      username: this.username,
      status: this.status,
      avatar: this.avatar
    };
  }
}

export default User;
