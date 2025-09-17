import React, { useState } from 'react';
import Header from '../components/Header.js';
import MapSlider from '../components/MapSlider.js';
import ForumForm from '../components/ForumForm.js';
import Notifications from '../components/Notifications.js';
import Threads from '../components/Threads.js';
import Messages from '../components/Messages.js';
import Favorites from '../components/Favorites.js';
import { Outlet } from 'react-router';
import NoAuthHeader from '../components/NoAuthHeader.js';

export const AuthLayout = () => {
  const [showForumForm, setShowForumForm] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showThreads, setShowThreads] = useState(false);
  const [showMessages, setShowMessages] = useState(false);
  const [showFavorites, setShowFavorites] = useState(false);
  const [friend, setFriend] = useState(-1);

  const [className, setClassName] = useState('');
  const [mapSlider, setMapSlider] = useState(true);

  const openMessages = (friend) => {
    setFriend(Number.parseInt(friend));
    setShowMessages(true);
  };

  const closeMessages = () => {
    setShowMessages(false);
    setFriend(-1);
  };

  const context = {};

  context.setShowForumForm = setShowForumForm;
  context.setShowNotifications = setShowNotifications;
  context.setShowThreads = setShowThreads;
  context.setShowMessages = setShowMessages;
  context.setShowFavorites = setShowFavorites;
  context.openMessages = openMessages;
  context.closeMessages = closeMessages;

  context.setClassName = setClassName;
  context.setMapSlider = setMapSlider;

  return (
    <div className={`page ${className}`}>
      <Header
        setShowNotifications={setShowNotifications}
        setShowThreads={setShowThreads}
        setShowFavorites={setShowFavorites}
      />
      {mapSlider && (
        <MapSlider key="page-map-slider" setShowForumForm={setShowForumForm} />
      )}
      <Outlet context={context} />
      <ForumForm show={showForumForm} onClose={() => setShowForumForm(false)} />
      <Notifications
        show={showNotifications}
        onClose={() => setShowNotifications(false)}
        openMessages={openMessages}
      />
      <Threads
        show={showThreads}
        onClose={() => setShowThreads(false)}
        openMessages={openMessages}
      />
      <Messages
        show={showMessages}
        friendId={friend}
        onClose={closeMessages}
        setShowThreads={setShowThreads}
      />
      <Favorites show={showFavorites} onClose={() => setShowFavorites(false)} />
    </div>
  );
};

AuthLayout.displayName = 'AuthLayout';

export const NoAuthLayout = () => {
  const [className, setClassName] = useState('');

  const context = {};

  context.setClassName = setClassName;

  return (
    <div key="main-page-container" className={`page ${className}`}>
      <NoAuthHeader />
      <Outlet context={context} />
    </div>
  );
};

NoAuthLayout.displayName = 'NoAuthLayout';
