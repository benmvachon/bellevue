import axios from 'axios';
import Page from './Page.js';
import User from './User.js';
import Profile from './Profile.js';
import Category from './Category.js';
import Forum from './Forum.js';
import Post from './Post.js';
import Notification from './Notification.js';
import Message from './Message.js';
import Equipment from './Equipment.js';
import Favorite from './Favorite.js';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

export const api = axios.create({
  baseURL: `${API_BASE_URL ? API_BASE_URL : ''}/api`,
  withCredentials: true
});

export const socket = new SockJS('/ws');

export const client = new Client({
  webSocketFactory: () => socket,
  onConnect: () => {
    console.log('WebSocket connected');
    if (connectedPromise) connectedPromise.resolve(); // resolve the connection promise
  }
});

// Create a promise to await connection
export const connectedPromise = createDeferred();
client.activate();

// Helper function to create a deferred promise
function createDeferred() {
  let resolve;
  const promise = new Promise((res) => {
    resolve = res;
  });
  return { promise, resolve };
}

// Expose a function to wait until connected
export const waitForConnection = () => connectedPromise?.promise;

export const subscriptions = new Map();

export const subscribe = async (destination, onMessage) => {
  await waitForConnection();
  if (client && client.connected) {
    subscriptions.set(
      destination,
      client.subscribe(destination, (message) => {
        onMessage(JSON.parse(message.body));
      })
    );
  }
};

export const unsubscribe = (destination) => {
  const subscription = subscriptions.get(destination);
  if (subscription) {
    subscription.unsubscribe();
    subscriptions.delete(destination);
  }
};

export const login = (username, password, callback, error) => {
  const formData = new URLSearchParams();
  formData.append('username', username);
  formData.append('password', password);
  api
    .post('/user/login', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
    .then(callback)
    .catch(error);
};

export const logout = (callback, error) => {
  api.post('/user/logout').then(callback).catch(error);
};

export const signup = (name, username, email, password, callback, error) => {
  const newUser = new User(name, username, email, password);
  api.post('/user/signup', newUser.toJSON()).then(callback).catch(error);
};

export const getProfile = (friend, callback, error) => {
  api
    .get(`/user/${friend}`)
    .then((response) => callback(Profile.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const getFriendshipStatus = (friend, callback, error) => {
  api
    .get(`/friend/${friend}/status`)
    .then((response) => callback(response.data))
    .catch(error);
};

export const requestFriend = (user, callback, error) => {
  api.post(`/friend/${user}/request`).then(callback).catch(error);
};

export const acceptFriend = (user, callback, error) => {
  api.post(`/friend/${user}/accept`).then(callback).catch(error);
};

export const removeFriend = (user, callback, error) => {
  api.delete(`/friend/${user}/remove`).then(callback).catch(error);
};

export const blockUser = (user, callback, error) => {
  api.post(`/friend/${user}/bock`).then(callback).catch(error);
};

export const updateBlackboard = (blackboard, callback, error) => {
  api
    .put('/user/blackboard', blackboard, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then(callback)
    .catch((err) => error(err));
};

export const updateLocation = (location, locationType, callback, error) => {
  if (location && locationType) {
    api
      .put(`/user/location/${locationType}/${location}`)
      .then(callback)
      .catch((err) => error(err));
  } else {
    api
      .put('/user/location')
      .then(callback)
      .catch((err) => error(err));
  }
};

export const getFriends = (friend, callback, error, page = 0) => {
  api
    .get(`/friend/${friend}/friends?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getFriendsInLocation = (callback, error, page = 0) => {
  api
    .get(`/user/location/friends?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getOthersInLocation = (callback, error, page = 0) => {
  api
    .get(`/user/location/nonfriends?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const onProfileUpdate = async (profile, onProfileUpdate) => {
  subscribe(`/topic/profile/${profile}`, onProfileUpdate);
};

export const unsubscribeProfile = (profile) => {
  unsubscribe(`/topic/profile/${profile}`);
};

export const getCategories = (callback, error, page = 0) => {
  api
    .get(`/forum/category?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Category.categoryMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getForums = (category, callback, error, page = 0) => {
  api
    .get(`/forum/category/${category}?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Forum.forumMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getForum = (id, callback, error) => {
  api
    .get(`/forum/${id}`)
    .then((response) => callback(Forum.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const addForum = (category, name, callback, error) => {
  api
    .post('/forum', new Forum(undefined, category, name).toJSON())
    .then((response) => callback(Forum.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const onForumUpdate = async (forum, onForumUpdate) => {
  subscribe(`/topic/forum/${forum}`, onForumUpdate);
};

export const unsubscribeForum = (forum) => {
  unsubscribe(`/topic/forum/${forum}`);
};

export const getPosts = (
  forum,
  callback,
  error,
  page = 0,
  sortByRelevance = true,
  size = 10
) => {
  api
    .get(
      `/post/forum/${forum}?page=${page}&size=${size}&sortByRelevance=${sortByRelevance}`
    )
    .then((response) => {
      const page = Page.fromJSON(response.data, Post.postMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getReplies = (
  post,
  callback,
  error,
  page = 0,
  sortByRelevance = true,
  size = 5,
  selectedChildId
) => {
  let uri = `/post/children/${post}?page=${page}&size=${size}&sortByRelevance=${sortByRelevance}`;
  if (selectedChildId)
    uri = `/post/children/${post}/${selectedChildId}?page=${page}&size=${size}&sortByRelevance=${sortByRelevance}`;
  api
    .get(uri)
    .then((response) => {
      const page = Page.fromJSON(response.data, Post.postMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getPost = (id, callback, error) => {
  api
    .get(`/post/${id}`)
    .then((response) => callback(Post.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const addPost = (forum, content, callback, error) => {
  api
    .post(`/post/${forum}`, content, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then((response) => callback(Post.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const addReply = (forum, parent, content, callback, error) => {
  api
    .post(`/post/${forum}/${parent}`, content, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then((response) => callback(Post.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const ratePost = (post, rating, callback, error) => {
  api
    .put(`/rating/${post}/${rating}`)
    .then(callback)
    .catch((err) => error(err));
};

export const onPostUpdate = async (post, onPostUpdate) => {
  subscribe(`/topic/post/${post}`, onPostUpdate);
};

export const unsubscribePost = (post) => {
  unsubscribe(`/topic/post/${post}`);
};

export const getNotificationCount = (callback, error) => {
  api
    .get('/notification/unread')
    .then((response) => {
      callback(Number.parseInt(response.data));
    })
    .catch((err) => error(err));
};

export const getNotifications = (callback, error, page = 0) => {
  api
    .get(`/notification?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(
        response.data,
        Notification.notificationMapper
      );
      callback(page);
    })
    .catch((err) => error(err));
};

export const markNotificationsRead = (callback, error) => {
  api
    .put('/notification/read')
    .then(callback)
    .catch((err) => error(err));
};

export const markNotificationRead = (notification, callback, error) => {
  api
    .put(`/notification/read/${notification}`)
    .then(callback)
    .catch((err) => error(err));
};

export const getMessageCount = (callback, error) => {
  api
    .get('/message/unread')
    .then((response) => {
      callback(Number.parseInt(response.data));
    })
    .catch((err) => error(err));
};

export const getThreads = (callback, error, page = 0) => {
  api
    .get(`/message?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.userEntityMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const markThreadRead = (friend, callback, error) => {
  api
    .put(`/message/${friend}/read`)
    .then(callback)
    .catch((err) => error(err));
};

export const markThreadsRead = (callback, error) => {
  api
    .put('/message/read')
    .then(callback)
    .catch((err) => error(err));
};

export const getMessages = (friend, callback, error, page = 0) => {
  api
    .get(`/message/${friend}?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Message.messageMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const sendMessage = (friend, message, callback, error) => {
  api
    .post(`/message/${friend}`, message, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then(callback)
    .catch((err) => error(err));
};

export const markMessageRead = (friend, message, callback, error) => {
  api
    .put(`/message/${friend}/read/${message}`)
    .then(callback)
    .catch((err) => error(err));
};

export const onNotification = (onNotification) => {
  subscribe('/user/topic/notification', onNotification);
};

export const unsubscribeNotification = () => {
  unsubscribe('/user/topic/notification');
};

export const onMessage = (onMessage) => {
  subscribe('/user/topic/message', onMessage);
};

export const unsubscribeMessage = () => {
  unsubscribe('/user/topic/message');
};

export const onEntrance = (onEntrance) => {
  subscribe('/user/topic/location', onEntrance);
};

export const unsubscribeLocation = () => {
  unsubscribe('/user/topic/location');
};

export const getEquipment = (callback, error, page = 0, slot = 'all') => {
  api
    .get(`/equipment?page=${page}&slot=${slot}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Equipment.equipmentMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const equipItem = (item, callback, error) => {
  api
    .put(`/equipment/${item}/equip`)
    .then(callback)
    .catch((err) => error(err));
};

export const unequipItem = (item, callback, error) => {
  api
    .put(`/equipment/${item}/unequip`)
    .then(callback)
    .catch((err) => error(err));
};

export const getFavorites = (callback, error, page = 0, type) => {
  if (type) {
    api
      .get(`/favorite?page=${page}&type=${type}`)
      .then((response) => {
        const page = Page.fromJSON(response.data, Favorite.favoriteMapper);
        callback(page);
      })
      .catch((err) => error(err));
  } else {
    api
      .get(`/favorite?page=${page}`)
      .then((response) => {
        const page = Page.fromJSON(response.data, Favorite.favoriteMapper);
        callback(page);
      })
      .catch((err) => error(err));
  }
};

export const favoritePost = (post, callback, error) => {
  api
    .post('/favorite/post', post, {
      headers: { 'Content-Type': 'application/json' }
    })
    .then(callback)
    .catch((err) => error(err));
};

export const favoriteForum = (forum, callback, error) => {
  api
    .post('/favorite/forum', forum, {
      headers: { 'Content-Type': 'application/json' }
    })
    .then(callback)
    .catch((err) => error(err));
};

export const favoriteProfile = (user, callback, error) => {
  api
    .post('/favorite/profile', user, {
      headers: { 'Content-Type': 'application/json' }
    })
    .then(callback)
    .catch((err) => error(err));
};

export const unfavoritePost = (post, callback, error) => {
  api
    .delete(`/favorite/post/${post}`)
    .then(callback)
    .catch((err) => error(err));
};

export const unfavoriteForum = (forum, callback, error) => {
  api
    .delete(`/favorite/forum/${forum}`)
    .then(callback)
    .catch((err) => error(err));
};

export const unfavoriteProfile = (user, callback, error) => {
  api
    .delete(`/favorite/profile/${user}`)
    .then(callback)
    .catch((err) => error(err));
};
