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

let reconnectTimeout = null;
let isCreatingClient = false;

export let connectedPromise = createDeferred();

function createDeferred() {
  let resolve;
  const promise = new Promise((res) => (resolve = res));
  return { promise, resolve };
}

function resetConnectedPromise() {
  connectedPromise = createDeferred();
}

function tryReconnect() {
  if (reconnectTimeout) return;

  reconnectTimeout = setTimeout(() => {
    console.log('[STOMP] Attempting to reconnect...');
    client?.deactivate().then(() => {
      reconnectTimeout = null;
      resetConnectedPromise();
      getClient(true); // Force reactivation
    });
  }, 3000);
}

export const waitForConnection = () => connectedPromise?.promise;

let client = null;

// Async-safe client creation
export const getClient = async (force = false) => {
  if (client && client.connected && !force) {
    return client;
  }

  if (isCreatingClient) {
    await waitForConnection(); // Wait for existing connection attempt
    return client;
  }

  isCreatingClient = true;

  client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    onConnect: () => {
      isCreatingClient = false;
      connectedPromise.resolve();
    },
    onDisconnect: tryReconnect,
    onWebSocketClose: tryReconnect,
    onWebSocketError: (event) => {
      console.error('[STOMP] WebSocket error', event);
      tryReconnect();
    },
    onStompError: (frame) => {
      console.error('[STOMP] Broker error', frame);
      tryReconnect();
    }
  });

  resetConnectedPromise();
  client.activate();

  await waitForConnection();
  return client;
};

export const subscriptions = new Map();

export const subscribe = async (destination, onMessage) => {
  unsubscribe(destination);
  const client = await getClient();
  subscriptions.set(
    destination,
    client.subscribe(destination, (message) => {
      onMessage(message.body);
    })
  );
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
    .then((response) => callback && callback(Profile.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const getFriendshipStatus = (friend, callback, error) => {
  api
    .get(`/friend/${friend}/status`)
    .then((response) => callback && callback(response.data))
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

export const getFriends = (friend, callback, error, page = 0, size = 10) => {
  api
    .get(`/friend/${friend}/friends?page=${page}&size=${size}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch((err) => error(err));
};

export const getFriendsInLocation = (callback, error, page = 0, size = 10) => {
  api
    .get(`/user/location/friends?page=${page}&size=${size}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch((err) => error(err));
};

export const getOthersInLocation = (callback, error, page = 0) => {
  api
    .get(`/user/location/nonfriends?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch((err) => error(err));
};

export const onProfileUpdate = async (profile, onProfileUpdate) => {
  subscribe(`/topic/profile/${profile}`, onProfileUpdate);
};

export const unsubscribeProfile = (profile) => {
  unsubscribe(`/topic/profile/${profile}`);
};

export const onFriendshipStatusUpdate = async (profile, onProfileUpdate) => {
  subscribe(`/user/topic/friendshipStatus/${profile}`, onProfileUpdate);
};

export const unsubscribeFriendshipStatus = (profile) => {
  unsubscribe(`/user/topic/friendshipStatus/${profile}`);
};

export const getCategories = (callback, error, page = 0) => {
  api
    .get(`/forum/category?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Category.categoryMapper);
      callback && callback(page);
    })
    .catch((err) => error(err));
};

export const getForums = (category, callback, error, page = 0) => {
  api
    .get(`/forum/category/${category}?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Forum.forumMapper);
      callback && callback(page);
    })
    .catch((err) => error(err));
};

export const getForum = (id, callback, error) => {
  api
    .get(`/forum/${id}`)
    .then((response) => callback && callback(Forum.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const addForum = (category, name, callback, error) => {
  api
    .post('/forum', new Forum(undefined, category, name).toJSON())
    .then((response) => callback && callback(Forum.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const onForumUpdate = async (forum, onForumUpdate) => {
  subscribe(`/topic/forum/${forum}`, (post) =>
    onForumUpdate(Number.parseInt(post))
  );
};

export const unsubscribeForum = (forum) => {
  unsubscribe(`/topic/forum/${forum}`);
};

export const onForumPopularityUpdate = async (forum, onForumUpdate) => {
  subscribe(`/user/topic/forum/${forum}/popularity`, (message) => {
    onForumUpdate(JSON.parse(message));
  });
};

export const unsubscribeForumPopularity = (forum) => {
  unsubscribe(`/user/topic/forum/${forum}/popularity`);
};

export const getPosts = (forum, callback, error, limit = 1) => {
  let uri = `/post/forum/${forum}?limit=${limit}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Post.fromJSON(post)));
    })
    .catch((err) => error(err));
};

export const getRecentPosts = (
  forum,
  callback,
  error,
  createdCursor,
  idCursor,
  limit = 1
) => {
  let uri = `/post/forum/${forum}/recent?limit=${limit}`;
  if (createdCursor) uri += `&createdCursor=${createdCursor}`;
  if (idCursor) uri += `&idCursor=${idCursor}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Post.fromJSON(post)));
    })
    .catch((err) => error(err));
};

export const getPopularPosts = (
  forum,
  callback,
  error,
  popularityCursor,
  idCursor,
  limit = 1
) => {
  let uri = `/post/forum/${forum}/popular?limit=${limit}`;
  if (popularityCursor) uri += `&popularityCursor=${popularityCursor}`;
  if (idCursor) uri += `&idCursor=${idCursor}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Post.fromJSON(post)));
    })
    .catch((err) => error(err));
};

export const getTotalPosts = (forum, callback, error) => {
  api
    .get(`/post/forum/${forum}/count`)
    .then((response) => {
      callback && callback(response.data);
    })
    .catch((err) => error(err));
};

export const getReplies = (
  post,
  callback,
  error,
  limit = 1,
  selectedChildId
) => {
  let uri = `/post/children/${post}?limit=${limit}`;
  if (selectedChildId)
    uri = `/post/children/${post}/${selectedChildId}?limit=${limit}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Post.fromJSON(post)));
    })
    .catch((err) => error(err));
};

export const getRecentReplies = (
  post,
  callback,
  error,
  createdCursor,
  idCursor,
  limit = 1,
  selectedChildId
) => {
  let uri = `/post/children/${post}/recent?limit=${limit}`;
  if (selectedChildId)
    uri = `/post/children/${post}/${selectedChildId}/recent?limit=${limit}`;
  if (createdCursor) uri += `&createdCursor=${createdCursor}`;
  if (idCursor) uri += `&idCursor=${idCursor}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Post.fromJSON(post)));
    })
    .catch((err) => error(err));
};

export const getPopularReplies = (
  post,
  callback,
  error,
  popularityCursor,
  idCursor,
  limit = 1,
  selectedChildId
) => {
  let uri = `/post/children/${post}/popular?limit=${limit}`;
  if (selectedChildId)
    uri = `/post/children/${post}/${selectedChildId}/popular?limit=${limit}`;
  if (popularityCursor) uri += `&popularityCursor=${popularityCursor}`;
  if (idCursor) uri += `&idCursor=${idCursor}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Post.fromJSON(post)));
    })
    .catch((err) => error(err));
};

export const getTotalReplies = (post, callback, error, selectedChildId) => {
  let uri = `/post/children/${post}/count`;
  if (selectedChildId) uri = `/post/children/${post}/${selectedChildId}/count`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data);
    })
    .catch((err) => error(err));
};

export const getPost = (id, callback, error) => {
  api
    .get(`/post/${id}`)
    .then((response) => callback && callback(Post.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const addPost = (forum, content, callback, error) => {
  api
    .post(`/post/${forum}`, content, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then((response) => callback && callback(Post.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const addReply = (forum, parent, content, callback, error) => {
  api
    .post(`/post/${forum}/${parent}`, content, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then((response) => callback && callback(Post.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const ratePost = (post, rating, callback, error) => {
  api
    .put(`/rating/${post}/${rating}`)
    .then(callback)
    .catch((err) => error(err));
};

export const onPostUpdate = async (post, onPostUpdate) => {
  subscribe(`/topic/post/${post}`, (message) => {
    if (message === 'rating') onPostUpdate(message);
    else onPostUpdate(Number.parseInt(message));
  });
};

export const unsubscribePost = (post) => {
  unsubscribe(`/topic/post/${post}`);
};

export const onPostPopularityUpdate = async (post, onPostUpdate) => {
  subscribe(`/user/topic/post/${post}/popularity`, (message) => {
    onPostUpdate(JSON.parse(message));
  });
};

export const unsubscribePostPopularity = (post) => {
  unsubscribe(`/user/topic/post/${post}/popularity`);
};

export const getNotification = (notification, callback, error) => {
  api
    .get(`/notification/${notification}`)
    .then((response) => {
      callback && callback(Notification.fromJSON(response.data));
    })
    .catch((err) => error(err));
};

export const getNotificationCount = (callback, error) => {
  api
    .get('/notification/unread')
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch((err) => error(err));
};

export const getNotificationTotal = (callback, error) => {
  api
    .get('/notification/total')
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch((err) => error(err));
};

export const getNotifications = (callback, error, cursor, limit = 5) => {
  let uri = `/notification?limit=${limit}`;
  if (cursor) uri += `&cursor=${cursor}`;
  api
    .get(uri)
    .then(
      (response) =>
        callback &&
        callback(
          response.data.map((notification) =>
            Notification.fromJSON(notification)
          )
        )
    )
    .catch((err) => error(err));
};

export const refreshNotifications = (callback, error, cursor) => {
  let uri = '/notification/refresh';
  if (cursor) uri += `&cursor=${cursor}`;
  api
    .get(uri)
    .then(
      (response) =>
        callback &&
        callback(
          response.data.map((notification) =>
            Notification.fromJSON(notification)
          )
        )
    )
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

export const onNotification = (onNotification) => {
  subscribe('/user/topic/notification', (notification) => {
    onNotification(Notification.fromJSON(JSON.parse(notification)));
  });
};

export const unsubscribeNotification = () => {
  unsubscribe('/user/topic/notification');
};

export const onNotificationCount = (onNotificationCount) => {
  subscribe('/user/topic/notification/unread', onNotificationCount);
};

export const unsubscribeNotificationCount = () => {
  unsubscribe('/user/topic/notification/unread');
};

export const onNotificationRead = (notification, onNotificationRead) => {
  subscribe(
    `/user/topic/notification/unread/${notification}`,
    onNotificationRead
  );
};

export const unsubscribeNotificationRead = (notification) => {
  unsubscribe(`/user/topic/notification/unread/${notification}`);
};

export const onNotificationsRead = (onNotificationsRead) => {
  subscribe('/user/topic/notification/all', onNotificationsRead);
};

export const unsubscribeNotificationsRead = () => {
  unsubscribe('/user/topic/notification/all');
};

export const getUnreadCount = (callback, error) => {
  api
    .get('/message/unread')
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch((err) => error(err));
};

export const getThreadCount = (callback, error) => {
  api
    .get('/message/total')
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch((err) => error(err));
};

export const getThreads = (callback, error, cursor, limit = 5) => {
  let uri = `/message/threads?limit=${limit}`;
  if (cursor) uri += `&cursor=${cursor}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Message.fromJSON(post)));
    })
    .catch((err) => error(err));
};

export const refreshThreads = (callback, error, cursor) => {
  let uri = '/message/threads/refresh';
  if (cursor) uri += `&cursor=${cursor}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Message.fromJSON(post)));
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

export const getMessage = (message, callback, error) => {
  api
    .get(`/message/${message}`)
    .then((response) => {
      callback && callback(Message.fromJSON(response.data));
    })
    .catch((err) => error(err));
};

export const getMessages = (friend, callback, error, cursor, limit = 5) => {
  let uri = `/message/${friend}/all?limit=${limit}`;
  if (cursor) uri += `&cursor=${cursor}`;
  api
    .get(uri)
    .then((response) => {
      callback &&
        callback(response.data.map((message) => Message.fromJSON(message)));
    })
    .catch((err) => error(err));
};

export const getMessageCount = (friend, callback, error) => {
  api
    .get(`/message/${friend}/total`)
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
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

export const onThread = (onThread) => {
  subscribe('/user/topic/thread', (message) => {
    const event = JSON.parse(message);
    event.message = Message.fromJSON(event.message);
    onThread(event);
  });
};

export const unsubscribeThread = () => {
  unsubscribe('/user/topic/thread');
};

export const onThreadsCount = (onThreadsCount) => {
  subscribe('/user/topic/thread/unread', onThreadsCount);
};

export const unsubscribeThreadsCount = () => {
  unsubscribe('/user/topic/thread/unread');
};

export const onThreadCount = (friend, onThreadCount) => {
  subscribe(`/user/topic/thread/unread/${friend}`, onThreadCount);
};

export const unsubscribeThreadCount = (friend) => {
  unsubscribe(`/user/topic/thread/unread/${friend}`);
};

export const onMessage = (friend, onMessage) => {
  subscribe(`/user/topic/message/${friend}`, (message) => {
    const event = JSON.parse(message);
    event.message = Message.fromJSON(event.message);
    onMessage(event);
  });
};

export const unsubscribeMessage = (friend) => {
  unsubscribe(`/user/topic/message/${friend}`);
};

export const onMessageRead = (message, onMessage) => {
  subscribe(`/user/topic/message/uread/${message}`, onMessage);
};

export const unsubscribeMessageRead = (message) => {
  unsubscribe(`/user/topic/message/uread/${message}`);
};

export const onThreadsRead = (onThreadsRead) => {
  subscribe('/user/topic/thread/all', onThreadsRead);
};

export const unsubscribeThreadsRead = () => {
  unsubscribe('/user/topic/thread/all');
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
      callback && callback(page);
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
        callback && callback(page);
      })
      .catch((err) => error(err));
  } else {
    api
      .get(`/favorite?page=${page}`)
      .then((response) => {
        const page = Page.fromJSON(response.data, Favorite.favoriteMapper);
        callback && callback(page);
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
