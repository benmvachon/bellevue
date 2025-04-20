import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import { getNotifications, markNotificationsRead } from '../api/api.js';
import InfiniteScroll from './InfiniteScroll.js';
import Notification from './Notification.js';

function Notifications({ show = false, onClose, openMessages }) {
  const [notifications, setNotifications] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (show) getNotifications(setNotifications, setError);
  }, [show]);

  const loadMore = (page) => {
    getNotifications(
      (more) => {
        if (more) {
          more.content = notifications?.content?.concat(more?.content);
          more.number = more.number + notifications?.number || 0;
          setNotifications(more);
        }
      },
      setError,
      page
    );
  };

  const markAllAsRead = () => {
    markNotificationsRead();
    onClose();
  };

  if (error) return JSON.stringify(error);
  if (!show) return;

  return (
    <div className="modal-container">
      <div className="modal notifications-container">
        <div className="notifications">
          <InfiniteScroll
            page={notifications}
            renderItem={(notification) => (
              <Notification
                notification={notification}
                onClose={onClose}
                openMessages={openMessages}
              />
            )}
            loadMore={loadMore}
          />
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
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired
};

export default withAuth(Notifications);
