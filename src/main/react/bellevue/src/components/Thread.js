import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useAuth } from '../utils/AuthContext.js';
import {
  getMessage,
  markThreadRead,
  onMessage,
  onMessageRead,
  onThreadCount,
  unsubscribeMessage,
  unsubscribeMessageRead,
  unsubscribeThreadCount
} from '../api/api.js';

function Thread({ thread, onClick }) {
  const { userId } = useAuth();
  const [stateThread, setThread] = useState(thread);
  const [friend, setFriend] = useState(null);
  const [received, setReceived] = useState(false);
  const [error, setError] = useState(false);

  const markAsRead = () => {
    markThreadRead(
      friend.id,
      getMessage(stateThread.id, setThread, setError),
      setError
    );
  };

  const threadClick = () => {
    onClick(stateThread);
  };

  useEffect(() => {
    setThread(thread);
  }, [thread]);

  useEffect(() => {
    if (stateThread) {
      if (stateThread.receiver.id === userId) {
        setFriend(stateThread.sender);
        setReceived(true);
      } else {
        setFriend(stateThread.receiver);
        setReceived(false);
      }
    }
  }, [stateThread, userId]);

  useEffect(() => {
    if (friend) {
      onMessage(friend.id, (event) => setThread(event.message));
      return () => unsubscribeMessage(friend.id);
    }
  }, [friend]);

  useEffect(() => {
    if (friend && stateThread) {
      onMessageRead(stateThread.id, () =>
        getMessage(stateThread.id, setThread, setError)
      );
      onThreadCount(friend.id, () =>
        getMessage(stateThread.id, setThread, setError)
      );
      return () => {
        unsubscribeMessageRead(stateThread.id);
        unsubscribeThreadCount(friend.id);
      };
    }
  }, [friend, stateThread]);

  if (error) return JSON.stringify(error);

  return (
    <div className="thread">
      <button onClick={threadClick}>
        {friend?.name} - {stateThread.message}
      </button>
      {received && !stateThread.read && (
        <button onClick={markAsRead}>Mark read</button>
      )}
    </div>
  );
}

Thread.propTypes = {
  thread: PropTypes.object.isRequired,
  onClick: PropTypes.func.isRequired
};

Thread.displayName = 'Thread';

export default Thread;
