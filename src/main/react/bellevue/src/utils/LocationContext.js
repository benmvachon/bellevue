import React, { createContext, useContext, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useAuth } from './AuthContext';
import { updateLocation } from '../api/api.js';

const LocationContext = createContext();

export const LocationProvider = ({ children }) => {
  const location = useLocation();
  const { userId } = useAuth();

  // derive locationType and locationId immediately
  const parseLocation = (path) => {
    const parts = path.split('/');
    const type = parts[1]?.toLowerCase();
    const id = parts[2] || null;

    if (type === 'forum') return { type: 'FORUM', id };
    if (type === 'profile') return { type: 'PROFILE', id };
    if (type === 'post') return { type: 'POST', id };
    if (type === 'login') return { type: 'LOGIN' };
    return { type: null, id: null };
  };

  const { type: locationType, id: locationId } = parseLocation(
    location.pathname
  );

  useEffect(() => {
    if (locationType === 'LOGIN') return;
    updateLocation(locationId, locationType);
  }, [userId, locationId, locationType]);

  return (
    <LocationContext.Provider value={{ locationType, locationId }}>
      {children}
    </LocationContext.Provider>
  );
};

LocationProvider.propTypes = {
  children: PropTypes.node.isRequired
};

export const useNotifyLocationChange = () => useContext(LocationContext);
