import Forum from './Forum';
import Post from './Post';

class Profile {
  constructor(
    id,
    name,
    username,
    status,
    avatar,
    equipment,
    locationType,
    forumLocation,
    profileLocation,
    postLocation,
    location,
    lastSeen,
    blackboard,
    friendshipStatus,
    favorite,
    _links
  ) {
    this.id = id;
    this.name = name;
    this.username = username;
    this.status = status;
    this.avatar = avatar;
    this.equipment = equipment;
    this.locationType = locationType;
    this.location = forumLocation
      ? Forum.fromJSON(forumLocation)
      : profileLocation
        ? Profile.fromJSON(profileLocation)
        : postLocation
          ? Post.fromJSON(postLocation)
          : location;
    this.lastSeen = new Date(lastSeen);
    this.blackboard = blackboard;
    this.friendshipStatus = friendshipStatus || 'UNSET';
    this.favorite = favorite;
    // TODO: process _links
  }

  static profileMapper = {
    fromPage(_embedded) {
      const profiles = [];
      if (_embedded) {
        for (const profile of _embedded.profileModelList) {
          profiles.push(Profile.fromJSON(profile));
        }
      }
      return profiles;
    }
  };

  static userEntityMapper = {
    fromPage(_embedded) {
      const profiles = [];
      if (_embedded) {
        for (const profile of _embedded.userProfileEntityList) {
          profiles.push(Profile.fromJSON(profile));
        }
      }
      return profiles;
    }
  };

  static fromJSON(json) {
    return new Profile(
      json.id ? json.id : json.user,
      json.name,
      json.username,
      json.status,
      json.avatar,
      json.equipment,
      json.locationType,
      json.forumLocation,
      json.profileLocation,
      json.postLocation,
      json.location,
      json.lastSeen?.toString(),
      json.blackboard,
      json.friendshipStatus,
      json.favorite
    );
  }

  toJSON() {
    const json = {
      id: this.id,
      name: this.name,
      username: this.username,
      status: this.status,
      avatar: this.avatar,
      equipment: this.equipment,
      locationType: this.locationType,
      lastSeen: this.lastSeen,
      blackboard: this.blackboard,
      friendshipStatus: this.friendshipStatus,
      favorite: this.favorite
    };
    if (this.locationType === 'FORUM') {
      json.forumLocation = this.location.toJSON();
    } else if (this.locationType === 'PROFILE') {
      json.profileLocation = this.location.toJSON();
    } else if (this.locationType === 'POST') {
      json.postLocation = this.location.toJSON();
    }
    return json;
  }
}

export default Profile;
