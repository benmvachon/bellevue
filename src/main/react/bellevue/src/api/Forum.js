import Profile from './Profile';

class Forum {
  constructor(
    id,
    category,
    name,
    user,
    created,
    favorite,
    unreadCount,
    notify,
    _links
  ) {
    this.id = id;
    this.category = category;
    this.name = name;
    this.user = user ? Profile.fromJSON(user) : undefined;
    this.created = created ? new Date(created) : undefined;
    this.favorite = favorite;
    this.unreadCount = unreadCount;
    this.notify = notify;
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
      json.category,
      json.name,
      json.user,
      json.created,
      json.favorite,
      json.unreadCount,
      json.notify
    );
  }

  toJSON() {
    return {
      id: this.id,
      category: this.category,
      name: this.name,
      user: this.user ? this.user.toJSON() : null,
      created: this.created?.toString(),
      favorite: this.favorite,
      unreadCount: this.unreadCount,
      notify: this.notify
    };
  }
}

export default Forum;
