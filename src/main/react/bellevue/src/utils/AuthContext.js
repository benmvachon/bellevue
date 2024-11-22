import React, { createContext, useState, useContext } from 'react';
import PropTypes from 'prop-types';
import { login, logout } from '../api/api.js';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const storedAuth = localStorage.getItem('isAuthenticated');
  const [isAuthenticated, setIsAuthenticated] = useState(storedAuth === 'true');

  const handleLogin = (username, password, callback, error) => {
    login(username, password)
      .then((response) => {
        console.log('Login successful:', JSON.stringify(response));
        setIsAuthenticated(true);
        localStorage.setItem('isAuthenticated', 'true');
        if (callback) callback();
      })
      .catch((err) => {
        console.error('Login failed: ', JSON.stringify(err));
        setIsAuthenticated(false);
        localStorage.removeItem('isAuthenticated');
        if (error) error(err);
      });
  };

  const handleLogout = (callback, error) => {
    logout()
      .then((response) => {
        setIsAuthenticated(false);
        localStorage.removeItem('isAuthenticated');
        if (callback) callback();
      })
      .catch((err) => {
        if (error) error(err);
      });
  };

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, handleLogin, handleLogout }}
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
