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
      json.ratingCount
    );
  }

  toJSON() {
    return {
      id: this.id,
      user: this.user ? this.user.fromJSON() : null,
      parent: this.parent ? this.parent.fromJSON() : null,
      forum: this.forum ? this.forum.fromJSON() : null,
      content: this.content,
      created: this.created?.toString(),
      children: this.children,
      rating: this.rating,
      ratingCount: this.ratingCount
    };
  }
}

export default Post;
