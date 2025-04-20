import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { useAuth } from '../utils/AuthContext.js';
import { getNotificationCount, getMessageCount } from '../api/api.js';
import Notifications from './Notifications.js';
import Threads from './Threads.js';
import Messages from './Messages.js';

function Header() {
  const navigate = useNavigate();
  const { userId } = useAuth();
  const [notificationCount, setNotificationCount] = useState(0);
  const [unreadThreadCount, setUnreadThreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showThreads, setShowThreads] = useState(false);
  const [showMessages, setShowMessages] = useState(false);
  const [friend, setFriend] = useState(-1);

  useEffect(() => {
    getNotificationCount(setNotificationCount, setNotificationCount);
    getMessageCount(setUnreadThreadCount, setUnreadThreadCount);
    const interval = setInterval(() => {
      getNotificationCount(setNotificationCount, setNotificationCount);
      getMessageCount(setUnreadThreadCount, setUnreadThreadCount);
    }, 5000);
    return () => clearInterval(interval);
  }, [setNotificationCount, setUnreadThreadCount]);

  const openNotifications = () => {
    setShowNotifications(true);
  };

  const closeNotifications = () => {
    setShowNotifications(false);
    getNotificationCount(setNotificationCount, setNotificationCount);
  };

  const openThreads = () => {
    setShowThreads(true);
  };

  const closeThreads = () => {
    setShowThreads(false);
    getMessageCount(setUnreadThreadCount, setUnreadThreadCount);
  };

  const openMessages = (friend) => {
    setFriend(friend);
    setShowMessages(true);
  };

  const closeMessages = () => {
    setShowMessages(false);
    setFriend(-1);
    setShowThreads(true);
  };

  return (
    <div className="header">
      <button onClick={() => navigate('/')}>Home</button>
      <button onClick={() => navigate(`/profile/${userId}`)}>Profile</button>
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
    </div>
  );
}

export default withAuth(Header);
