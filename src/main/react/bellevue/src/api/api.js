import axios from 'axios';
import User from './User.js';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

export const api = axios.create({
  baseURL: `${API_BASE_URL ? API_BASE_URL : ''}/api`,
  withCredentials: true
});

export const login = (username, password) => {
  const formData = new URLSearchParams();
  formData.append('username', username);
  formData.append('password', password);
  return api.post('/user/login', formData, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });
};

export const logout = () => {
  return api.post('/user/logout');
};

export const signup = (name, username, password, avatar) => {
  const newUser = new User(name, username, password, avatar);
  return api.post('/user/signup', newUser.toJSON());
};

export const getFriend = (id) => {
  return api.get('/friend/' + id);
};
