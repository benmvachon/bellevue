import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import {
  getNotifications,
  getNotificationTotal,
  markNotificationsRead,
  onNotification,
  onNotificationsRead,
  refreshNotifications,
  unsubscribeNotification,
  unsubscribeNotificationsRead
} from '../api/api.js';
import Notification from './Notification.js';
import ScrollLoader from './ScrollLoader.js';
import Modal from './Modal.js';

function Notifications({ show = false, onClose, openMessages }) {
  const [notifications, setNotifications] = useState([]);
  const [totalNotifications, setTotalNotifications] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const loadMore = () => {
    if (totalNotifications <= notifications.length) return;
    const cursor = notifications[notifications.length - 1].created.getTime();
    getNotificationTotal((totalNotifications) => {
      getNotifications(
        (more) => {
          setTotalNotifications(totalNotifications);
          if (more) {
            setNotifications(notifications.concat(more));
          }
        },
        setError,
        cursor,
        5
      );
    }, setError);
  };

  const markAllAsRead = () => {
    markNotificationsRead();
    onClose();
  };

  useEffect(() => {
    if (show) {
      setLoading(true);
      getNotificationTotal(
        (totalNotifications) => {
          getNotifications(
            (notifications) => {
              setTotalNotifications(totalNotifications);
              setNotifications(notifications);
              setLoading(false);
            },
            (error) => {
              setError(error);
              setLoading(false);
            }
          );
        },
        (error) => {
          setError(error);
          setLoading(false);
        }
      );
    }
  }, [show]);

  useEffect(() => {
    if (show) {
      onNotification((notification) => {
        setNotifications([notification].concat(notifications));
      });
      if (notifications && notifications.length) {
        const cursor =
          notifications[notifications.length - 1].created.getTime();
        onNotificationsRead(() =>
          refreshNotifications(setNotifications, setError, cursor)
        );
      }
      return () => {
        unsubscribeNotification();
        unsubscribeNotificationsRead();
      };
    }
  }, [show, notifications]);

  if (error) return JSON.stringify(error);
  if (!show) return;

  return (
    <Modal className="notifications-container" show={show} onClose={onClose}>
      {loading ? (
        <p>Loading...</p>
      ) : totalNotifications > 0 ? (
        <ScrollLoader
          total={totalNotifications}
          loadMore={loadMore}
          className="notifications"
        >
          {notifications?.map((notification) => (
            <Notification
              key={`notification-${notification.id}`}
              notification={notification}
              onClose={onClose}
              openMessages={openMessages}
            />
          ))}
        </ScrollLoader>
      ) : (
        <p>No notifications</p>
      )}
      <div className="buttons">
        <button onClick={onClose}>Close</button>
        <button onClick={markAllAsRead}>Mark all as read</button>
      </div>
    </Modal>
  );
}

Notifications.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired
};

Notifications.displayName = 'Notifications';

export default withAuth(Notifications);
