import React, { createContext, useContext, useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useAuth } from './AuthContext';
import {
  getFriendsInLocation,
  onEntrance,
  updateLocation
} from '../api/api.js';

const LocationContext = createContext();

export const LocationProvider = ({ children }) => {
  const location = useLocation();
  const { userId } = useAuth();
  const [attendees, setAttendees] = useState(null);

  // derive locationType and locationId immediately
  const parseLocation = (path) => {
    const parts = path.split('/');
    const section = parts[1]?.toLowerCase();
    const id = parts[2] || null;

    if (section === 'forum') return { type: 'FORUM', id };
    if (section === 'profile') return { type: 'PROFILE', id };
    return { type: null, id: null };
  };

  const { type: locationType, id: locationId } = parseLocation(
    location.pathname
  );

  useEffect(() => {
    if (!userId || !locationId || !locationType) {
      updateLocation(locationId, locationType);
      return;
    }

    const refreshAttendees = () => {
      getFriendsInLocation(locationId, locationType, setAttendees);
    };

    updateLocation(locationId, locationType, refreshAttendees);
    onEntrance(refreshAttendees);
  }, [userId, locationId, locationType]);

  return (
    <LocationContext.Provider value={{ attendees }}>
      {children}
    </LocationContext.Provider>
  );
};

LocationProvider.propTypes = {
  children: PropTypes.node.isRequired
};

export const useNotifyLocationChange = () => useContext(LocationContext);
