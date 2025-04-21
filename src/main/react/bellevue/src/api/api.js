import axios from 'axios';
import Page from './Page.js';
import User from './User.js';
import Profile from './Profile.js';
import Category from './Category.js';
import Forum from './Forum.js';
import Post from './Post.js';
import Notification from './Notification.js';
import Message from './Message.js';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

export const api = axios.create({
  baseURL: `${API_BASE_URL ? API_BASE_URL : ''}/api`,
  withCredentials: true
});

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

export const getFriends = (friend, callback, error, page = 0) => {
  api
    .get(`/friend/${friend}/friends?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getFriendsInLocation = (location, callback, error, page = 0) => {
  api
    .get(`/user/friends/${location}?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getOthersInLocation = (location, callback, error, page = 0) => {
  api
    .get(`/user/nonfriends/${location}?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Profile.profileMapper);
      callback(page);
    })
    .catch((err) => error(err));
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

export const getPosts = (forum, callback, error, page = 0) => {
  api
    .get(`/post/forum/${forum}?page=${page}`)
    .then((response) => {
      const page = Page.fromJSON(response.data, Post.postMapper);
      callback(page);
    })
    .catch((err) => error(err));
};

export const getReplies = (post, callback, error, page = 0) => {
  api
    .get(`/post/children/${post}?page=${page}`)
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
