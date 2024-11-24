import React, { createContext, useState, useContext } from 'react';
import PropTypes from 'prop-types';
import { login, logout } from '../api/api.js';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const storedAuth = localStorage.getItem('isAuthenticated');
  const storedUserId = localStorage.getItem('userId');
  const [isAuthenticated, setIsAuthenticated] = useState(storedAuth === 'true');
  const [userId, setUserId] = useState(storedUserId);

  const handleLogin = (username, password, callback, error) => {
    login(
      username,
      password,
      (response) => {
        console.log('Login successful:', JSON.stringify(response));
        setUserId(response.data);
        localStorage.setItem('userId', response.data);
        setIsAuthenticated(true);
        localStorage.setItem('isAuthenticated', 'true');
        if (callback) callback(response);
      },
      (err) => {
        console.error('Login failed: ', JSON.stringify(err));
        setIsAuthenticated(false);
        localStorage.removeItem('isAuthenticated');
        if (error) error(err);
      }
    );
  };

  const handleLogout = (callback, error) => {
    logout((response) => {
      setUserId(undefined);
      localStorage.removeItem('userId');
      setIsAuthenticated(false);
      localStorage.removeItem('isAuthenticated');
      if (callback) callback(response);
    }, error);
  };

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, userId, handleLogin, handleLogout }}
    >
      {children}
    </AuthContext.Provider>
  );
};

// Add PropTypes validation
AuthProvider.propTypes = {
  children: PropTypes.node.isRequired
};

export const useAuth = () => useContext(AuthContext);
