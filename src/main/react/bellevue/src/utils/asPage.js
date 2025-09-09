import React, { useState } from 'react';
import Header from '../components/Header.js';
import MapSlider from '../components/MapSlider.js';
import ForumForm from '../components/ForumForm.js';
import Notifications from '../components/Notifications.js';
import Threads from '../components/Threads.js';
import Messages from '../components/Messages.js';
import Favorites from '../components/Favorites.js';

const asPage = (WrappedComponent, className, mapSlider = true) => {
  const PageComponent = (props) => {
    const [showForumForm, setShowForumForm] = useState(false);
    const [showNotifications, setShowNotifications] = useState(false);
    const [showThreads, setShowThreads] = useState(false);
    const [showMessages, setShowMessages] = useState(false);
    const [showFavorites, setShowFavorites] = useState(false);
    const [friend, setFriend] = useState(-1);

    const openMessages = (friend) => {
      setFriend(Number.parseInt(friend));
      setShowMessages(true);
    };

    const closeMessages = () => {
      setShowMessages(false);
      setFriend(-1);
    };

    props.setShowForumForm = setShowForumForm;
    props.setShowNotifications = setShowNotifications;
    props.setShowThreads = setShowThreads;
    props.setShowMessages = setShowMessages;
    props.setShowFavorites = setShowFavorites;
    props.openMessages = openMessages;
    props.closeMessages = closeMessages;

    return (
      <div key="main-page-container" className={`page ${className}`}>
        <Header
          key="page-header"
          setShowNotifications={setShowNotifications}
          setShowThreads={setShowThreads}
          setShowFavorites={setShowFavorites}
        />
        {mapSlider && (
          <MapSlider
            key="page-map-slider"
            setShowForumForm={setShowForumForm}
          />
        )}
        <WrappedComponent {...props} />
        <ForumForm
          show={showForumForm}
          onClose={() => setShowForumForm(false)}
        />
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
        <Favorites
          show={showFavorites}
          onClose={() => setShowFavorites(false)}
        />
      </div>
    );
  };

  PageComponent.displayName = `asPage(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`;

  return PageComponent;
};

export default asPage;
