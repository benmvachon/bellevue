import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext.js';
import {
  getNotificationCount,
  getUnreadCount,
  onNotificationCount,
  onThreadsCount,
  unsubscribeMessage,
  unsubscribeNotification
} from '../api/api.js';
import Notifications from './Notifications.js';
import Threads from './Threads.js';
import Messages from './Messages.js';
import Favorites from './Favorites.js';

function Header() {
  const navigate = useNavigate();
  const { userId } = useAuth();
  const [notificationCount, setNotificationCount] = useState(0);
  const [unreadThreadCount, setUnreadThreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showThreads, setShowThreads] = useState(false);
  const [showMessages, setShowMessages] = useState(false);
  const [showFavorites, setShowFavorites] = useState(false);
  const [friend, setFriend] = useState(-1);
  const [error, setError] = useState(false);

  useEffect(() => {
    getNotificationCount(setNotificationCount, setError);
    getUnreadCount(setUnreadThreadCount, setError);
    onNotificationCount(() =>
      getNotificationCount(setNotificationCount, setError)
    );
    onThreadsCount(() => getUnreadCount(setUnreadThreadCount, setError));

    return () => {
      unsubscribeNotification();
      unsubscribeMessage();
    };
  }, []);

  const openNotifications = () => {
    setShowNotifications(true);
  };

  const closeNotifications = () => {
    setShowNotifications(false);
    getNotificationCount(setNotificationCount, setError);
  };

  const openThreads = () => {
    setShowThreads(true);
  };

  const closeThreads = () => {
    setShowThreads(false);
    getUnreadCount(setUnreadThreadCount, setError);
  };

  const openMessages = (friend) => {
    setFriend(Number.parseInt(friend));
    setShowMessages(true);
  };

  const closeMessages = () => {
    setShowMessages(false);
    setFriend(-1);
  };

  const openFavorites = () => {
    setShowFavorites(true);
  };

  const closeFavorites = () => {
    setShowFavorites(false);
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="header">
      <button onClick={() => navigate('/')}>Home</button>
      <button onClick={() => navigate(`/profile/${userId}`)}>Profile</button>
      <button onClick={openFavorites}>Favorites</button>
      <h1>BLORVIS</h1>
      <button onClick={openNotifications}>
        Notifications: {notificationCount}
      </button>
      <button onClick={openThreads}>Messages: {unreadThreadCount}</button>
      <Notifications
        show={showNotifications}
        onClose={closeNotifications}
        openMessages={openMessages}
      />
      <Threads
        show={showThreads}
        onClose={closeThreads}
        openMessages={openMessages}
      />
      <Messages show={showMessages} friend={friend} onClose={closeMessages} />
      <Favorites show={showFavorites} onClose={closeFavorites} />
    </div>
  );
}

Header.displayName = 'Header';

export default Header;
