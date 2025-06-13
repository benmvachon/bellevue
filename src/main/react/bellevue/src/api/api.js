import axios from 'axios';
import Page from './Page.js';
import User from './User.js';
import Profile from './Profile.js';
import Forum from './Forum.js';
import Post from './Post.js';
import Notification from './Notification.js';
import Message from './Message.js';
import Equipment from './Equipment.js';
import Favorite from './Favorite.js';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const api = axios.create({
  baseURL: '/api',
  withCredentials: true
});

api.interceptors.response.use(
  (response) => {
    // If the server returned an HTML page instead of JSON, assume redirection
    if (
      response.headers['content-type'] &&
      response.headers['content-type'].includes('text/html')
    ) {
      console.warn('Session expired. Redirecting to login...');
      window.location.href = response.request.responseURL;
    }

    return response;
  },
  (error) => {
    // Handle 401 Unauthorized explicitly
    if (error.response && error.response.status === 401) {
      console.warn('Unauthorized. Redirecting to login...');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

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
    console.warn('[STOMP] Attempting to reconnect...');
    client?.deactivate().then(() => {
      reconnectTimeout = null;
      resetConnectedPromise();
      getClient(true); // Force reactivation
    });
  }, 3000);
}

export const waitForConnection = () => connectedPromise?.promise;

let client = null;

export const getClient = async (force = false) => {
  if (client && client.connected && !force) {
    return client;
  }

  if (isCreatingClient) {
    await waitForConnection(); // Wait for the current connection to complete
    return client;
  }

  isCreatingClient = true;

  client = new Client({
    webSocketFactory: () => new SockJS('/ws'), // Matches your WebSocketConfig
    connectHeaders: {
      login: 'guest',
      passcode: 'guest'
    },
    debug: (str) => console.debug(`[STOMP DEBUG] ${str}`),
    reconnectDelay: 0, // We handle it ourselves
    onConnect: () => {
      console.log('[STOMP] Connected');
      isCreatingClient = false;
      connectedPromise.resolve();
      // resubscribe
      subscriptions?.forEach(({ onMessage }, destination) => {
        subscribe(destination, onMessage);
      });
    },
    onDisconnect: () => {
      console.log('[STOMP] Disconnected');
      tryReconnect();
    },
    onWebSocketClose: () => {
      console.warn('[STOMP] WebSocket closed');
      tryReconnect();
    },
    onWebSocketError: (event) => {
      console.error('[STOMP] WebSocket error', event);
      tryReconnect();
    },
    onStompError: (frame) => {
      console.error('[STOMP] Broker STOMP error', frame);
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

  const sub = client.subscribe(destination, (message) => {
    console.warn('[STOMP] Received non-JSON message', message.body);
    onMessage(message.body);
  });

  subscriptions.set(destination, { sub, onMessage });
};

export const unsubscribe = (destination) => {
  const value = subscriptions.get(destination);
  if (value && value.sub) {
    value.sub.unsubscribe();
    subscriptions.delete(destination);
  }
};

export const login = (username, password, callback, error) => {
  api.post('/user/login', { username, password }).then(callback).catch(error);
};

export const logout = (callback, error) => {
  api.post('/user/logout').then(callback).catch(error);
};

export const signup = (name, username, email, password, callback, error) => {
  const newUser = new User(undefined, name, username, email, password);
  api.post('/user/signup', newUser.toJSON()).then(callback).catch(error);
};

export const getProfile = (friend, callback, error) => {
  api
    .get(`/user/${friend}`)
    .then((response) => callback && callback(Profile.fromJSON(response.data)))
    .catch(error);
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

export const updateBlackboard = (blackboard, callback, error) => {
  api
    .put('/user/blackboard', blackboard, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then(callback)
    .catch(error);
};

export const updateLocation = (location, locationType, callback, error) => {
  if (location && locationType) {
    api
      .put(`/user/location/${locationType}/${location}`)
      .then(callback)
      .catch(error);
  } else {
    api.put('/user/location').then(callback).catch(error);
  }
};

export const getMyFriends = (
  callback,
  error,
  query = '',
  excluded = [],
  page = 0,
  size = 10
) => {
  api
    .get(
      `/friend?query=${query}&excluded=${excluded}&page=${page}&size=${size}`
    )
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch(error);
};

export const getSuggestedFriends = (callback, error, page = 0, size = 10) => {
  api
    .get(`/friend/suggestions?page=${page}&size=${size}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch(error);
};

export const getFriends = (friend, callback, error, page = 0, size = 10) => {
  api
    .get(`/friend/${friend}/friends?page=${page}&size=${size}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch(error);
};

export const findUsers = (query, callback, error, page = 0, size = 10) => {
  api
    .get(`/user/search/${query}?page=${page}&size=${size}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch(error);
};

export const getFriendsInLocation = (callback, error, page = 0, size = 10) => {
  api
    .get(`/user/location/friends?page=${page}&size=${size}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch(error);
};

export const getOthersInLocation = (callback, error, page = 0) => {
  api
    .get(`/user/location/nonfriends?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback && callback(page);
    })
    .catch(error);
};

export const onProfileUpdate = (profile, onProfileUpdate) => {
  subscribe(`/topic/profile.${profile}`, onProfileUpdate);
};

export const unsubscribeProfile = (profile) => {
  unsubscribe(`/topic/profile.${profile}`);
};

export const onFriendshipStatusUpdate = (profile, onProfileUpdate) => {
  subscribe(`/user/topic/friendshipStatus.${profile}`, onProfileUpdate);
};

export const unsubscribeFriendshipStatus = (profile) => {
  unsubscribe(`/user/topic/friendshipStatus.${profile}`);
};

export const getForums = (
  callback,
  error,
  unread = false,
  query = '',
  page = 0,
  size = 9
) => {
  api
    .get(`/forum?unread=${unread}&query=${query}&page=${page}&size=${size}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Forum.forumMapper);
      callback && callback(page);
    })
    .catch(error);
};

export const getTags = (callback, error, query = '', page = 0, size = 10) => {
  api
    .get(`/forum/tags?&query=${query}&page=${page}&size=${size}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, {
        fromPage: (_embedded) => {
          return _embedded?.stringList || [];
        }
      });
      callback && callback(page);
    })
    .catch(error);
};

export const getForum = (id, callback, error) => {
  api
    .get(`/forum/${id}`)
    .then((response) => callback && callback(Forum.fromJSON(response.data)))
    .catch(error);
};

export const addForum = (name, description, tags, users, callback, error) => {
  api
    .post(
      '/forum',
      new Forum(undefined, undefined, name, description, tags, users).toJSON()
    )
    .then((response) => callback && callback(Forum.fromJSON(response.data)))
    .catch(error);
};

export const updateForum = (
  id,
  name,
  description,
  tags,
  users,
  callback,
  error
) => {
  api
    .put(
      `/forum/${id}`,
      new Forum(undefined, undefined, name, description, tags, users).toJSON()
    )
    .then((response) => callback && callback(Forum.fromJSON(response.data)))
    .catch(error);
};

export const deleteForum = (id, callback, error) => {
  api.delete(`/forum/${id}`).then(callback).catch(error);
};

export const setForumNotification = (forum, notify, callback, error) => {
  api
    .put(`/forum/${forum}/${notify ? 'notify' : 'mute'}`)
    .then(callback)
    .catch(error);
};

export const onForumUpdate = (forum, onForumUpdate) => {
  let uri = '/user/topic/feed';
  if (forum) uri = `/topic/forum.${forum}`;
  subscribe(uri, (post) => onForumUpdate(Number.parseInt(post)));
};

export const unsubscribeForum = (forum) => {
  let uri = '/user/topic/feed';
  if (forum) uri = `/topic/forum.${forum}`;
  unsubscribe(uri);
};

export const onForumPopularityUpdate = (forum, onForumUpdate) => {
  let uri = '/user/topic/feed.popularity';
  if (forum) uri = `/user/topic/forum.${forum}.popularity`;
  subscribe(uri, (message) => {
    onForumUpdate(JSON.parse(message));
  });
};

export const unsubscribeForumPopularity = (forum) => {
  let uri = '/user/topic/feed.popularity';
  if (forum) uri = `/user/topic/forum.${forum}.popularity`;
  unsubscribe(uri);
};

export const onForumUnreadUpdate = (forum, onForumUpdate) => {
  let uri = '/user/topic/feed.unread';
  if (forum) uri = `/user/topic/forum.unread.${forum}`;
  subscribe(uri, (message) => onForumUpdate(message));
};

export const unsubscribeForumUnread = (forum) => {
  let uri = '/user/topic/feed.unread';
  if (forum) uri = `/user/topic/forum.unread.${forum}`;
  unsubscribe(uri);
};

export const getPosts = (
  callback,
  error,
  sortCursor,
  idCursor,
  excludedForums,
  sortByPopular = false,
  limit = 1
) => {
  let uri = `/post?limit=${limit}&sortByPopular=${sortByPopular}`;
  if (sortCursor) uri += `&sortCursor=${sortCursor}`;
  if (idCursor) uri += `&idCursor=${idCursor}`;
  if (excludedForums) uri += `&excludedForums=${excludedForums}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Post.fromJSON(post)));
    })
    .catch(error);
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
    .catch(error);
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
    .catch(error);
};

export const getTotalPosts = (forum, callback, error, excludedForums) => {
  let uri = '/post/count';
  if (forum) uri = `/post/forum/${forum}/count`;
  else if (excludedForums) uri += `?excludedForums=${excludedForums}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data);
    })
    .catch(error);
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
    .catch(error);
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
    .catch(error);
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
    .catch(error);
};

export const getTotalReplies = (post, callback, error, selectedChildId) => {
  let uri = `/post/children/${post}/count`;
  if (selectedChildId) uri = `/post/children/${post}/${selectedChildId}/count`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data);
    })
    .catch(error);
};

export const getPost = (id, callback, error) => {
  api
    .get(`/post/${id}`)
    .then((response) => callback && callback(Post.fromJSON(response.data)))
    .catch(error);
};

export const addPost = (forum, content, callback, error) => {
  api
    .post(`/post/${forum}`, content, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then((response) => callback && callback(Post.fromJSON(response.data)))
    .catch(error);
};

export const addReply = (forum, parent, content, callback, error) => {
  api
    .post(`/post/${forum}/${parent}`, content, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then((response) => callback && callback(Post.fromJSON(response.data)))
    .catch(error);
};

export const ratePost = (post, rating, callback, error) => {
  api.put(`/rating/${post}/${rating}`).then(callback).catch(error);
};

export const deletePost = (post, callback, error) => {
  api.delete(`/post/${post}`).then(callback).catch(error);
};

export const markPostRead = (post, callback, error) => {
  api.put(`/post/read/${post}`).then(callback).catch(error);
};

export const markForumRead = (forum, callback, error) => {
  api.put(`/post/readall/${forum}`).then(callback).catch(error);
};

export const markPostsRead = (callback, error) => {
  api.put('/post/readall').then(callback).catch(error);
};

export const onPostDelete = (forum, onPostDelete) => {
  let uri = '/user/topic/feed.delete';
  if (forum) uri = `/user/topic/forum.${forum}.delete`;
  subscribe(uri, (message) => {
    onPostDelete(Number.parseInt(message));
  });
};

export const unsubscribePostDelete = (forum) => {
  let uri = '/user/topic/feed.delete';
  if (forum) uri = `/user/topic/forum.${forum}.delete`;
  unsubscribe(uri);
};

export const onReplyDelete = (post, onReplyDelete) => {
  subscribe(`/user/topic/post.${post}.delete`, (message) => {
    onReplyDelete(Number.parseInt(message));
  });
};

export const unsubscribeReplyDelete = (post) => {
  unsubscribe(`/user/topic/post.${post}.delete`);
};

export const onPostUpdate = (post, onPostUpdate) => {
  subscribe(`/topic/post.${post}`, (message) => {
    if (message === 'rating') onPostUpdate(message);
    else onPostUpdate(Number.parseInt(message));
  });
};

export const unsubscribePost = (post) => {
  unsubscribe(`/topic/post.${post}`);
};

export const onPostPopularityUpdate = (post, onPostUpdate) => {
  subscribe(`/user/topic/post.${post}.popularity`, (message) => {
    onPostUpdate(JSON.parse(message));
  });
};

export const unsubscribePostPopularity = (post) => {
  unsubscribe(`/user/topic/post.${post}.popularity`);
};

export const getNotification = (notification, callback, error) => {
  api
    .get(`/notification/${notification}`)
    .then((response) => {
      callback && callback(Notification.fromJSON(response.data));
    })
    .catch(error);
};

export const getNotificationCount = (callback, error) => {
  api
    .get('/notification/unread')
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch(error);
};

export const getNotificationTotal = (callback, error) => {
  api
    .get('/notification/total')
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch(error);
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
    .catch(error);
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
    .catch(error);
};

export const markNotificationsRead = (callback, error) => {
  api.put('/notification/read').then(callback).catch(error);
};

export const markNotificationRead = (notification, callback, error) => {
  api.put(`/notification/read/${notification}`).then(callback).catch(error);
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
  subscribe('/user/topic/notification.unread', onNotificationCount);
};

export const unsubscribeNotificationCount = () => {
  unsubscribe('/user/topic/notification.unread');
};

export const onNotificationRead = (notification, onNotificationRead) => {
  subscribe(
    `/user/topic/notification/unread/${notification}`,
    onNotificationRead
  );
};

export const unsubscribeNotificationRead = (notification) => {
  unsubscribe(`/user/topic/notification.unread.${notification}`);
};

export const onNotificationsRead = (onNotificationsRead) => {
  subscribe('/user/topic/notification.all', onNotificationsRead);
};

export const unsubscribeNotificationsRead = () => {
  unsubscribe('/user/topic/notification.all');
};

export const getUnreadCount = (callback, error) => {
  api
    .get('/message/unread')
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch(error);
};

export const getThreadCount = (callback, error) => {
  api
    .get('/message/total')
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch(error);
};

export const getThreads = (callback, error, cursor, limit = 5) => {
  let uri = `/message/threads?limit=${limit}`;
  if (cursor) uri += `&cursor=${cursor}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Message.fromJSON(post)));
    })
    .catch(error);
};

export const refreshThreads = (callback, error, cursor) => {
  let uri = '/message/threads/refresh';
  if (cursor) uri += `&cursor=${cursor}`;
  api
    .get(uri)
    .then((response) => {
      callback && callback(response.data.map((post) => Message.fromJSON(post)));
    })
    .catch(error);
};

export const markThreadRead = (friend, callback, error) => {
  api.put(`/message/${friend}/read`).then(callback).catch(error);
};

export const markThreadsRead = (callback, error) => {
  api.put('/message/read').then(callback).catch(error);
};

export const getMessage = (message, callback, error) => {
  api
    .get(`/message/${message}`)
    .then((response) => {
      callback && callback(Message.fromJSON(response.data));
    })
    .catch(error);
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
    .catch(error);
};

export const getMessageCount = (friend, callback, error) => {
  api
    .get(`/message/${friend}/total`)
    .then((response) => {
      callback && callback(Number.parseInt(response.data));
    })
    .catch(error);
};

export const sendMessage = (friend, message, callback, error) => {
  api
    .post(`/message/${friend}`, message, {
      headers: { 'Content-Type': 'text/plain' }
    })
    .then(callback)
    .catch(error);
};

export const markMessageRead = (friend, message, callback, error) => {
  api.put(`/message/${friend}/read/${message}`).then(callback).catch(error);
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
  subscribe('/user/topic/thread.unread', onThreadsCount);
};

export const unsubscribeThreadsCount = () => {
  unsubscribe('/user/topic/thread.unread');
};

export const onThreadCount = (friend, onThreadCount) => {
  subscribe(`/user/topic/thread.unread.${friend}`, onThreadCount);
};

export const unsubscribeThreadCount = (friend) => {
  unsubscribe(`/user/topic/thread.unread.${friend}`);
};

export const onMessage = (friend, onMessage) => {
  subscribe(`/user/topic/message.${friend}`, (message) => {
    const event = JSON.parse(message);
    event.message = Message.fromJSON(event.message);
    onMessage(event);
  });
};

export const unsubscribeMessage = (friend) => {
  unsubscribe(`/user/topic/message.${friend}`);
};

export const onMessageRead = (message, onMessage) => {
  subscribe(`/user/topic/message.uread.${message}`, onMessage);
};

export const unsubscribeMessageRead = (message) => {
  unsubscribe(`/user/topic/message.uread.${message}`);
};

export const onThreadsRead = (onThreadsRead) => {
  subscribe('/user/topic/thread.all', onThreadsRead);
};

export const unsubscribeThreadsRead = () => {
  unsubscribe('/user/topic/thread.all');
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
    .catch(error);
};

export const equipItem = (item, callback, error) => {
  api.put(`/equipment/${item}/equip`).then(callback).catch(error);
};

export const unequipItem = (item, callback, error) => {
  api.put(`/equipment/${item}/unequip`).then(callback).catch(error);
};

export const getFavorites = (callback, error, page = 0, type) => {
  if (type) {
    api
      .get(`/favorite?page=${page}&type=${type}`)
      .then((response) => {
        const page = Page.fromJSON(response.data, Favorite.favoriteMapper);
        callback && callback(page);
      })
      .catch(error);
  } else {
    api
      .get(`/favorite?page=${page}`)
      .then((response) => {
        const page = Page.fromJSON(response.data, Favorite.favoriteMapper);
        callback && callback(page);
      })
      .catch(error);
  }
};

export const favoritePost = (post, callback, error) => {
  api
    .post('/favorite/post', post, {
      headers: { 'Content-Type': 'application/json' }
    })
    .then(callback)
    .catch(error);
};

export const favoriteForum = (forum, callback, error) => {
  api
    .post('/favorite/forum', forum, {
      headers: { 'Content-Type': 'application/json' }
    })
    .then(callback)
    .catch(error);
};

export const favoriteProfile = (user, callback, error) => {
  api
    .post('/favorite/profile', user, {
      headers: { 'Content-Type': 'application/json' }
    })
    .then(callback)
    .catch(error);
};

export const unfavoritePost = (post, callback, error) => {
  api.delete(`/favorite/post/${post}`).then(callback).catch(error);
};

export const unfavoriteForum = (forum, callback, error) => {
  api.delete(`/favorite/forum/${forum}`).then(callback).catch(error);
};

export const unfavoriteProfile = (user, callback, error) => {
  api.delete(`/favorite/profile/${user}`).then(callback).catch(error);
};
