import Profile from './Profile';
import Forum from './Forum';

class Post {
  constructor(
    id,
    user,
    parent,
    forum,
    content,
    created,
    children,
    rating,
    ratingCount,
    popularity,
    favorite,
    read,
    _links
  ) {
    this.id = id;
    this.user = Profile.fromJSON(user);
    this.parent = parent ? Post.fromJSON(parent) : null;
    this.forum = Forum.fromJSON(forum);
    this.content = content;
    this.created = new Date(created);
    this.children = children;
    this.rating = rating;
    this.ratingCount = ratingCount;
    this.popularity = popularity;
    this.favorite = favorite;
    this.read = read;
    // TODO: process _links
  }

  static postMapper = {
    fromPage(_embedded) {
      const posts = [];
      if (_embedded) {
        for (const post of _embedded.postModelList) {
          posts.push(Post.fromJSON(post));
        }
      }
      return posts;
    }
  };

  static fromJSON(json) {
    return new Post(
      json.id,
      json.user,
      json.parent,
      json.forum,
      json.content,
      json.created,
      json.children,
      json.rating,
      json.ratingCount,
      json.popularity,
      json.favorite,
      json.read
    );
  }

  toJSON() {
    return {
      id: this.id,
      user: this.user ? this.user.toJSON() : null,
      parent: this.parent ? this.parent.toJSON() : null,
      forum: this.forum ? this.forum.toJSON() : null,
      content: this.content,
      created: this.created?.toLocaleString(),
      children: this.children,
      rating: this.rating,
      ratingCount: this.ratingCount,
      popularity: this.popularity,
      favorite: this.favorite,
      read: this.read
    };
  }
}

export default Post;
