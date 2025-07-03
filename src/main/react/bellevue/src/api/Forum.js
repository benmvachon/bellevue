import Profile from './Profile';

class Forum {
  constructor(
    id,
    user,
    name,
    description,
    tags,
    users,
    created,
    favorite,
    unreadCount,
    notify,
    _links
  ) {
    this.id = id;
    this.user = user ? Profile.fromJSON(user) : undefined;
    this.name = name;
    this.description = description;
    this.tags = tags;
    this.users = users
      ? users.map((user) =>
          typeof user === 'number' ? user : Profile.fromJSON(user)
        )
      : [];
    this.created = created ? new Date(created) : undefined;
    this.favorite = favorite;
    this.unreadCount = unreadCount;
    this.notify = notify;
    this.custom = user ? true : false;
    // TODO: process _links
  }

  static forumMapper = {
    fromPage(_embedded) {
      const forums = [];
      if (_embedded) {
        for (const forum of _embedded.forumModelList) {
          forums.push(Forum.fromJSON(forum));
        }
      }
      return forums;
    }
  };

  static fromJSON(json) {
    return new Forum(
      json.id,
      json.user,
      json.name,
      json.description,
      json.tags,
      json.users,
      json.created,
      json.favorite,
      json.unreadCount,
      json.notify
    );
  }

  toJSON() {
    return {
      id: this.id,
      user: this.user ? this.user.toJSON() : undefined,
      name: this.name,
      description: this.description,
      tags: this.tags,
      users: this.users
        ? this.users.map((user) =>
            typeof user === 'number' ? user : user.toJSON()
          )
        : [],
      created: this.created?.toString(),
      favorite: this.favorite,
      unreadCount: this.unreadCount,
      notify: this.notify
    };
  }
}

export default Forum;
