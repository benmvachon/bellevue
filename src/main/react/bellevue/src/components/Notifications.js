import React, { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
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

function Notifications({ show = false, onClose, openMessages, pushAlert }) {
  const outletContext = useOutletContext();
  const [notifications, setNotifications] = useState([]);
  const [totalNotifications, setTotalNotifications] = useState(0);
  const [loading, setLoading] = useState(true);
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
  };

  if (error) return JSON.stringify(error);

  return (
    <Modal className="notifications-container" show={show} onClose={onClose}>
      {loading ? (
        <div className="notifications">
          <p>Loading...</p>
        </div>
      ) : totalNotifications > 0 ? (
        <ScrollLoader
          total={totalNotifications}
          loadMore={loadMore}
          className="notifications"
          pushAlert={pushAlert}
        >
          {notifications?.map((notification) => (
            <Notification
              key={`notification-${notification.id}`}
              notification={notification}
              onClose={onClose}
              openMessages={openMessages}
              pushAlert={pushAlert}
            />
          ))}
        </ScrollLoader>
      ) : (
        <div className="notifications">
          <p>No notifications</p>
        </div>
      )}
      <div className="buttons">
        <button onClick={onClose}>close</button>
        <button onClick={markAllAsRead}>mark all read</button>
      </div>
    </Modal>
  );
}

Notifications.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired,
  pushAlert: PropTypes.func
};

Notifications.displayName = 'Notifications';

export default Notifications;
