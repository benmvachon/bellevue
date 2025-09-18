import React, { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
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
import Avatar from './Avatar.js';

function Thread({ thread, onClick, pushAlert }) {
  const outletContext = useOutletContext();
  const { userId } = useAuth();
  const [stateThread, setThread] = useState(thread);
  const [friend, setFriend] = useState(null);
  const [received, setReceived] = useState(false);
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

  return (
    <div className="thread">
      <button
        className="mark-read"
        onClick={markAsRead}
        disabled={!received || stateThread.read || friend?.id < 1}
      >
        mark read
      </button>
      <button className="message" onClick={threadClick}>
        <span className="received-indication">{received ? '<' : '>'}</span>
        {stateThread.message}
      </button>
      {friend && (
        <Avatar
          userId={friend?.id}
          userProp={friend}
          className="thread-avatar"
          pushAlert={pushAlert}
        />
      )}
    </div>
  );
}

Thread.propTypes = {
  thread: PropTypes.object.isRequired,
  onClick: PropTypes.func.isRequired,
  pushAlert: PropTypes.func
};

Thread.displayName = 'Thread';

export default Thread;
