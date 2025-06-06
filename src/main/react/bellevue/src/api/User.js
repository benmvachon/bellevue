class User {
  constructor(id, name, username, email, password) {
    this.id = id;
    this.name = name;
    this.username = username;
    this.email = email;
    this.password = password;
  }

  static fromJSON(json) {
    return new User(
      json.id,
      json.name,
      json.username,
      json.email,
      json.password
    );
  }

  toJSON() {
    return {
      id: this.id,
      name: this.name,
      username: this.username,
      email: this.email,
      password: this.password
    };
  }
}

export default User;
