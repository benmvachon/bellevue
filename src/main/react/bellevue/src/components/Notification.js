import PropTypes from 'prop-types';
import React, { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext.js';
import {
  markNotificationRead,
  getNotification,
  onNotificationRead,
  unsubscribeNotificationRead
} from '../api/api.js';
import Avatar from './Avatar.js';

function Notification({ notification, onClose, openMessages, pushAlert }) {
  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const { userId } = useAuth();
  const [stateNotification, setNotification] = useState(notification);
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
    if (stateNotification) {
      onNotificationRead(stateNotification.id, () =>
        getNotification(stateNotification.id, setNotification, setError)
      );
      return () => {
        unsubscribeNotificationRead(stateNotification.id);
      };
    }
  }, [stateNotification]);

  useEffect(() => {
    if (notification) setNotification(notification);
  }, [notification]);

  const markAsRead = (callback) => {
    markNotificationRead(stateNotification.id, callback);
  };

  const notificationClick = () => {
    markAsRead(() => {
      switch (stateNotification.type) {
        case 'POST':
        case 'REPLY':
        case 'RATING':
          navigate(`/flyer/${stateNotification.entity}`);
          break;
        case 'REQUEST':
        case 'ACCEPTANCE':
          navigate(`/home/${stateNotification.entity}`);
          break;
        case 'MESSAGE':
          openMessages(stateNotification.entity);
          break;
        case 'EQUIPMENT':
          navigate(`/home/${userId}`);
          break;
        case 'FORUM':
          navigate(`/town/${stateNotification.entity}`);
          break;
        case 'SYSTEM':
          navigate('/map/suburbs');
          break;
        default:
          navigate(`/${stateNotification.type}/${stateNotification.entity}`);
          break;
      }
      onClose();
    });
  };

  const getNotificationText = () => {
    switch (stateNotification.type) {
      case 'POST':
        return 'posted a new flyer';
      case 'REPLY':
        return 'posted a new reply to your flyer';
      case 'RATING':
        return 'rated your flyer';
      case 'REQUEST':
        return 'sent you a friend request';
      case 'ACCEPTANCE':
        return 'accepted your friend request';
      case 'MESSAGE':
        return 'sent you a message';
      case 'EQUIPMENT':
        return 'unlocked new equipment for you';
      case 'FORUM':
        return 'added you to a custom forum';
      case 'SYSTEM':
        return 'visit the suburbs to start requesting friends';
      default:
        return stateNotification.type;
    }
  };

  if (!stateNotification) return;

  return (
    <div className="notification">
      <Avatar
        userId={
          stateNotification.notifier?.id === userId
            ? 0
            : stateNotification.notifier?.id
        }
        userProp={stateNotification.notifier}
        pushAlert={outletContext ? outletContext.pushAlert : pushAlert}
      />
      <button className="notification-button" onClick={notificationClick}>
        {getNotificationText()}
      </button>
      <button
        className="notification-button"
        onClick={markAsRead}
        disabled={stateNotification.read}
      >
        mark read
      </button>
    </div>
  );
}

Notification.propTypes = {
  notification: PropTypes.object.isRequired,
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired,
  pushAlert: PropTypes.func
};

Notification.displayName = 'Notification';

export default Notification;
