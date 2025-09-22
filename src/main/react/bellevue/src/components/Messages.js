import React, { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useAuth } from '../utils/AuthContext.js';
import {
  getProfile,
  getMessages,
  onMessage,
  unsubscribeMessage,
  getMessageCount,
  sendMessage,
  markMessageRead
} from '../api/api.js';
import ScrollLoader from './ScrollLoader.js';
import Modal from './Modal.js';
import Avatar from './Avatar.js';
import Message from './Message.js';
import LoadingSpinner from './LoadingSpinner.js';

function Messages({
  show = false,
  friendId,
  onClose,
  setShowThreads,
  pushAlert
}) {
  const { userId } = useAuth();
  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const [messages, setMessages] = useState([]);
  const [totalMessages, setTotalMessages] = useState(0);
  const [message, setMessage] = useState('');
  const [profile, setProfile] = useState(null);
  const [friend, setFriend] = useState(null);
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
    if (show && friendId) {
      onMessage(friendId, (event) => {
        setTotalMessages(totalMessages + 1);
        setMessages(messages.concat([event.message]));
      });
      return () => {
        unsubscribeMessage(friendId);
      };
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [friendId, messages, show]);

  useEffect(() => {
    const sentOrReceived = (message) => {
      if (message.receiver.id === userId) return 'received';
      return 'sent';
    };
    messages?.forEach((message) => {
      if ('received' === sentOrReceived(message) && !message.read)
        markMessageRead(friendId, message.id);
    });
  }, [friendId, messages, userId]);

  useEffect(() => {
    if (show) {
      setLoading(true);
      getProfile(userId, setProfile, setError);
      friendId > 0 && getProfile(friendId, setFriend, setError);
      getMessageCount(
        friendId,
        (totalMessages) => {
          getMessages(
            friendId,
            (messages) => {
              setTotalMessages(totalMessages);
              setMessages(messages ? messages.reverse() : []);
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
  }, [show, friendId, userId]);

  const sentOrReceived = (message) => {
    if (message.receiver.id === userId) return 'received';
    return 'sent';
  };

  const loadMore = () => {
    if (totalMessages <= messages.length) return;
    const cursor = messages[0].created.getTime();
    getMessageCount(
      friendId,
      (totalMessages) => {
        getMessages(
          friendId,
          (more) => {
            setTotalMessages(totalMessages);
            if (more) {
              setMessages(more.reverse().concat(messages));
            }
          },
          setError,
          cursor,
          5
        );
      },
      setError
    );
  };

  const handleKeyDown = (e) => {
    if (e.shiftKey) return;
    switch (e.key) {
      case 'Enter':
        if (message) send(e);
        break;
      default:
        break;
    }
  };

  const send = (event) => {
    event && event.preventDefault();
    sendMessage(friendId, message, () => {
      setMessage('');
    });
  };

  const back = (event) => {
    event && event.preventDefault();
    onClose();
    setShowThreads(true);
  };

  return (
    <Modal className="messages-container" show={show} onClose={onClose}>
      <button
        className="name pixel-corners"
        onClick={() => friend?.id > 1 && navigate(`/home/${friend?.id}`)}
      >
        {friend?.name || 'SYSTEM'}
      </button>
      {loading ? (
        <div className="messages">
          <LoadingSpinner onClick={() => {}} />
        </div>
      ) : totalMessages > 0 ? (
        <ScrollLoader
          total={totalMessages}
          loadMore={loadMore}
          className="messages"
          topLoad
        >
          {messages.map((message) => (
            <Message
              key={`message-${message.id}`}
              friendId={friend}
              id={message.id}
              message={message.message}
              sentOrReceived={sentOrReceived(message)}
              read={message.read}
              date={message.created}
            />
          ))}
        </ScrollLoader>
      ) : (
        <div className="messages">
          <p>No messages</p>
        </div>
      )}
      <div className="buttons">
        <button className="back" onClick={back}>
          back
        </button>
        <textarea
          name="new-message"
          placeholder={friendId <= 1 ? 'no replies' : 'new message...'}
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={friendId <= 1}
        />
        <button
          className="send"
          disabled={friendId <= 1}
          onClick={send}
          type="submit"
        >
          send
        </button>
      </div>
      {profile && (
        <Avatar
          userId={profile?.id}
          userProp={profile}
          size="medium"
          flip
          name={false}
        />
      )}
      <div className="phone-container">
        <img
          className="image phone-open medium"
          src={require('../asset/phone-open-medium.png')}
          alt="phone"
        />
      </div>
      <Avatar
        userId={friend?.id || 0}
        userProp={friend}
        size="medium"
        className="friend"
        name={false}
      />
      <div className="phone-container friend">
        <img
          className="image phone-open medium flip"
          src={require('../asset/phone-open-medium.png')}
          alt="phone"
        />
      </div>
    </Modal>
  );
}

Messages.propTypes = {
  show: PropTypes.bool,
  friendId: PropTypes.number.isRequired,
  onClose: PropTypes.func.isRequired,
  setShowThreads: PropTypes.func.isRequired,
  pushAlert: PropTypes.func
};

Messages.displayName = 'Messages';

export default Messages;
