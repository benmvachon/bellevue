import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getNotifications, markNotificationsRead } from '../api/api.js';

function Notifications({ show = false, onClose }) {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState(null);

  useEffect(() => {
    if (show) getNotifications(setNotifications, setNotifications);
  }, [show]);

  const markAllAsRead = () => {
    markNotificationsRead();
    onClose();
  };

  const profileClick = (profile) => {
    navigate(`/profile/${profile}`);
    onClose();
  };

  if (!show) return;

  return (
    <div className="modal-container">
      <div className="modal notifications-container">
        <div className="notifications">
          {notifications?.content?.map((notification) => (
            <div
              className="notification"
              key={`notification-${notification.id}`}
            >
              <button onClick={() => profileClick(notification.notifier.id)}>
                {notification.notifier.name}
              </button>
              <p>{notification.typeName}</p>
            </div>
          ))}
        </div>
        <div className="buttons">
          <button onClick={onClose}>Close</button>
          <button onClick={markAllAsRead}>Mark all as read</button>
        </div>
      </div>
    </div>
  );
}

Notifications.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired
};

export default withAuth(Notifications);
