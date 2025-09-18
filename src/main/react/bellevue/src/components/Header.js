import React, { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useAuth } from '../utils/AuthContext.js';
import {
  getProfile,
  getNotificationCount,
  getUnreadCount,
  onNotificationCount,
  onThreadsCount,
  unsubscribeMessage,
  unsubscribeNotification
} from '../api/api.js';
import ImageButton from './ImageButton.js';

function Header({
  setShowNotifications,
  setShowThreads,
  setShowFavorites,
  pushAlert
}) {
  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const { userId } = useAuth();
  const [profile, setProfile] = useState(null);
  const [notificationCount, setNotificationCount] = useState(0);
  const [unreadThreadCount, setUnreadThreadCount] = useState(0);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (error) {
      let func = pushAlert;
      if (outletContext) func = outletContext.pushAlert;
      func({
        key: JSON.stringify(error),
        type: 'error',
        content: (
          <div>
            <h3>Error: {error.code}</h3>
            <p>{error.message}</p>
          </div>
        )
      });
      setError(false);
    }
  }, [outletContext, pushAlert, error]);

  useEffect(() => {
    getProfile(
      userId,
      (profile) => {
        setProfile(profile);
      },
      (error) => {
        setError(error);
      }
    );

    onNotificationCount(() =>
      getNotificationCount(setNotificationCount, setError)
    );
    onThreadsCount(() => getUnreadCount(setUnreadThreadCount, setError));
    getNotificationCount(setNotificationCount, setError);
    getUnreadCount(setUnreadThreadCount, setError);

    return () => {
      unsubscribeNotification();
      unsubscribeMessage();
    };
  }, [userId]);

  const openNotifications = () => {
    setShowNotifications(true);
  };

  const openThreads = () => {
    setShowThreads(true);
  };

  const openFavorites = () => {
    setShowFavorites(true);
  };

  return (
    <div className="header" key="header" id="header">
      <ImageButton name="townhall" onClick={() => navigate('/')} />
      <ImageButton
        className="header-map-button"
        name="map"
        onClick={() => navigate('/map/town')}
      />
      <ImageButton name="newspaper" onClick={openNotifications}>
        <span className={`count${notificationCount > 0 ? ' red' : ''}`}>
          {notificationCount}
        </span>
      </ImageButton>
      <ImageButton name="phone" onClick={openThreads}>
        <span className={`count${unreadThreadCount > 0 ? ' red' : ''}`}>
          {unreadThreadCount}
        </span>
      </ImageButton>
      <ImageButton name="notepad" onClick={openFavorites} />
      <ImageButton
        name={profile?.avatar}
        hat={profile?.equipment?.hat}
        onClick={() => navigate(`/home/${userId}`)}
      />
    </div>
  );
}

Header.propTypes = {
  setShowNotifications: PropTypes.func.isRequired,
  setShowThreads: PropTypes.func.isRequired,
  setShowFavorites: PropTypes.func.isRequired,
  pushAlert: PropTypes.func
};

Header.displayName = 'Header';

export default Header;
