import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { markNotificationRead } from '../api/api.js';

function Notifications({ notification, onClose }) {
  const navigate = useNavigate();

  const markAsRead = () => {
    markNotificationRead(notification.id);
  };

  const profileClick = () => {
    navigate(`/profile/${notification.notifier.id}`);
    onClose();
  };

  const notificationClick = () => {
    switch (notification.typeName) {
      default:
        navigate(`/profile/${notification.notifier.id}`);
    }
    markAsRead();
  };

  return (
    <div className="notification">
      <button onClick={() => profileClick()}>
        {notification.notifier.name}
      </button>
      <button onClick={() => notificationClick()}>
        {notification.typeName}
      </button>
    </div>
  );
}

Notifications.propTypes = {
  notification: PropTypes.object.isRequired,
  onClose: PropTypes.func.isRequired
};

export default withAuth(Notifications);
