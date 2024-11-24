import axios from 'axios';
import Page from './Page.js';
import Recipe from './Recipe.js';
import User from './User.js';

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

export const signup = (name, username, password, avatar, callback, error) => {
  const newUser = new User(name, username, password, avatar);
  api.post('/user/signup', newUser.toJSON()).then(callback).catch(error);
};

export const getFriend = (friend, callback, error) => {
  api
    .get('/friend/' + friend)
    .then((response) => callback(User.fromJSON(response.data)))
    .catch((err) => error(err));
};

export const getFriendshipStatus = (friend, callback, error) => {
  api
    .get('/friend/' + friend + '/status')
    .then((response) => callback(response.data))
    .catch(error);
};

export const getFriendRecipes = (friend, callback, error, page = 0) => {
  api
    .get('/recipe/author/' + friend + '?p=' + page)
    .then((response) => {
      const page = Page.fromJSON(response.data);
      const content = [];
      for (const recipe of page.content) {
        content.push(Recipe.fromJSON(recipe));
      }
      page.content = content;
      callback(page);
    })
    .catch(error);
};

export const getFriends = (friend, callback, error, page = 0) => {
  api
    .get('/friend/' + friend + '/friends?p=' + page)
    .then((response) => {
      const page = Page.fromJSON(response.data);
      const content = [];
      for (const friendship of page.content) {
        content.push(User.fromJSON(friendship.friend));
      }
      page.content = content;
      callback(page);
    })
    .catch((err) => error(err));
};

export const getRecipe = (recipe, callback, error) => {
  api
    .get('/recipe/' + recipe)
    .then((response) => callback(Recipe.fromJSON(response.data)))
    .catch((err) => error(err));
};
