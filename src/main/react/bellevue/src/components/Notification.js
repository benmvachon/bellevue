import PropTypes from 'prop-types';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import {
  markNotificationRead,
  getNotification,
  onNotificationRead,
  unsubscribeNotificationRead
} from '../api/api.js';

function Notification({ notification, onClose, openMessages }) {
  const navigate = useNavigate();
  const [stateNotification, setNotification] = useState(notification);
  const [error, setError] = useState(false);

  const markAsRead = () => {
    markNotificationRead(stateNotification.id);
  };

  const profileClick = () => {
    navigate(`/profile/${stateNotification.notifier.id}`);
    onClose();
  };

  const notificationClick = () => {
    switch (stateNotification.type) {
      case 'REPLY':
      case 'RATING':
        navigate(`/post/${stateNotification.entity}`);
        break;
      case 'REQUEST':
      case 'ACCEPTANCE':
        navigate(`/profile/${stateNotification.entity}`);
        break;
      case 'MESSAGE':
        openMessages(stateNotification.entity);
        break;
      case 'EQUIPMENT':
        navigate(`/profile/${stateNotification.notified}`);
        break;
      default:
        navigate(`/${stateNotification.type}/${stateNotification.entity}`);
        break;
    }
    onClose();
    markAsRead();
  };

  useEffect(() => {
    if (notification) setNotification(notification);
  }, [notification]);

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

  if (error) return JSON.stringify(error);
  if (!stateNotification) return;

  return (
    <div className="notification">
      <button onClick={profileClick}>{stateNotification.notifier.name}</button>
      <button onClick={notificationClick}>{stateNotification.type}</button>
      {!stateNotification.read && (
        <button onClick={markAsRead}>Mark as read</button>
      )}
    </div>
  );
}

Notification.propTypes = {
  notification: PropTypes.object.isRequired,
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired
};

export default withAuth(Notification);
