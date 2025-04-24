import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { markNotificationRead } from '../api/api.js';

function Notification({ notification, onClose, openMessages }) {
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
      case 'reply':
      case 'rating':
        navigate(`/post/${notification.entity}`);
        break;
      case 'request':
      case 'acceptance':
        navigate(`/profile/${notification.entity}`);
        break;
      case 'message':
        openMessages(notification.entity);
        break;
      case 'equipment':
        navigate(`/profile/${notification.notified}`);
        break;
      default:
        navigate(`/${notification.typeName}/${notification.entity}`);
        break;
    }
    onClose();
    markAsRead();
  };

  return (
    <div className="notification">
      <button onClick={profileClick}>{notification.notifier.name}</button>
      <button onClick={notificationClick}>{notification.typeName}</button>
      <button onClick={markAsRead}>Mark as read</button>
    </div>
  );
}

Notification.propTypes = {
  notification: PropTypes.object.isRequired,
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired
};

export default withAuth(Notification);
