import React, { useEffect, useState } from 'react';
import { Outlet } from 'react-router';
import { useAuth } from './AuthContext.js';
import {
  onNotification,
  onThread,
  unsubscribeNotification,
  unsubscribeThread
} from '../api/api.js';
import Header from '../components/Header.js';
import MapSlider from '../components/MapSlider.js';
import ForumForm from '../components/ForumForm.js';
import Notifications from '../components/Notifications.js';
import Threads from '../components/Threads.js';
import Messages from '../components/Messages.js';
import Favorites from '../components/Favorites.js';
import NoAuthHeader from '../components/NoAuthHeader.js';
import Alert from '../components/Alert.js';
import Notification from '../components/Notification.js';
import Thread from '../components/Thread.js';

export const AuthLayout = () => {
  const { userId } = useAuth();
  const [showForumForm, setShowForumForm] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showThreads, setShowThreads] = useState(false);
  const [showMessages, setShowMessages] = useState(false);
  const [showFavorites, setShowFavorites] = useState(false);
  const [friend, setFriend] = useState(-1);
  const [alerts, setAlerts] = useState([]);

  const [className, setClassName] = useState('');
  const [mapSlider, setMapSlider] = useState(true);

  useEffect(() => {
    if (!showNotifications) {
      onNotification((notification) =>
        pushAlert({
          key: `notification-${notification.id}`,
          type: 'info',
          content: (
            <div>
              <h4>notification</h4>
              <Notification
                notification={notification}
                onClose={() => {}}
                openMessages={openMessages}
                pushAlert={pushAlert}
              />
            </div>
          )
        })
      );
    }

    if (!showThreads) {
      onThread((event) => {
        if (
          '' + event?.message?.sender?.id === '' + userId ||
          '' + event?.message?.sender?.id === '' + friend
        )
          return;
        pushAlert({
          key: `message-${event?.message?.id}`,
          type: 'info',
          content: (
            <div>
              <h4>message</h4>
              <Thread
                thread={event?.message}
                onClick={(thread) => {
                  openMessages(thread?.sender?.id);
                  setShowThreads(false);
                }}
                pushAlert={pushAlert}
              />
            </div>
          )
        });
      });
    }

    return () => {
      if (!showNotifications) unsubscribeNotification();
      if (!showThreads) unsubscribeThread();
    };
  }, [showNotifications, showThreads, userId, friend]);

  const openMessages = (friend) => {
    setFriend(Number.parseInt(friend));
    setShowMessages(true);
  };

  const closeMessages = () => {
    setShowMessages(false);
    setFriend(-1);
  };

  const pushAlert = (alert) => {
    alert.remove = () =>
      setAlerts((currentAlerts) =>
        currentAlerts.filter(
          (item) => JSON.stringify(item) !== JSON.stringify(alert)
        )
      );
    setAlerts((currentAlerts) => {
      currentAlerts.push(alert);
      return [...currentAlerts];
    });
  };

  const context = {};

  context.setShowForumForm = setShowForumForm;
  context.setShowNotifications = setShowNotifications;
  context.setShowThreads = setShowThreads;
  context.setShowMessages = setShowMessages;
  context.setShowFavorites = setShowFavorites;
  context.openMessages = openMessages;
  context.closeMessages = closeMessages;
  context.pushAlert = pushAlert;

  context.setClassName = setClassName;
  context.setMapSlider = setMapSlider;

  return (
    <div className={`page ${className}`}>
      <div className="alerts">
        {alerts?.map((alert, i) => (
          <Alert key={alert.key} {...alert} />
        ))}
      </div>
      <Header
        setShowNotifications={setShowNotifications}
        setShowThreads={setShowThreads}
        setShowFavorites={setShowFavorites}
        pushAlert={pushAlert}
      />
      {mapSlider && (
        <MapSlider
          key="page-map-slider"
          setShowForumForm={setShowForumForm}
          pushAlert={pushAlert}
        />
      )}
      <Outlet context={context} />
      <ForumForm
        show={showForumForm}
        onClose={() => setShowForumForm(false)}
        pushAlert={pushAlert}
      />
      <Notifications
        show={showNotifications}
        onClose={() => setShowNotifications(false)}
        openMessages={openMessages}
        pushAlert={pushAlert}
      />
      <Threads
        show={showThreads}
        onClose={() => setShowThreads(false)}
        openMessages={openMessages}
        pushAlert={pushAlert}
      />
      <Messages
        show={showMessages}
        friendId={friend}
        onClose={closeMessages}
        setShowThreads={setShowThreads}
        pushAlert={pushAlert}
      />
      <Favorites
        show={showFavorites}
        onClose={() => setShowFavorites(false)}
        pushAlert={pushAlert}
      />
    </div>
  );
};

AuthLayout.displayName = 'AuthLayout';

export const NoAuthLayout = () => {
  const [className, setClassName] = useState('');
  const [alerts, setAlerts] = useState([]);

  const pushAlert = (alert) => {
    alert.remove = () =>
      setAlerts((currentAlerts) =>
        currentAlerts.filter(
          (item) => JSON.stringify(item) !== JSON.stringify(alert)
        )
      );
    setAlerts((currentAlerts) => {
      currentAlerts.push(alert);
      return [...currentAlerts];
    });
  };

  const context = {};

  context.setClassName = setClassName;
  context.pushAlert = pushAlert;

  return (
    <div key="main-page-container" className={`page ${className}`}>
      <div className="alerts">
        {alerts?.map((alert, i) => (
          <Alert key={alert.key} {...alert} />
        ))}
      </div>
      <NoAuthHeader />
      <Outlet context={context} />
    </div>
  );
};

NoAuthLayout.displayName = 'NoAuthLayout';
