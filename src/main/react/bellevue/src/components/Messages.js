import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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

function Messages({ show = false, friendId, onClose, setShowThreads }) {
  const { userId } = useAuth();
  const navigate = useNavigate();
  const [messages, setMessages] = useState([]);
  const [totalMessages, setTotalMessages] = useState(0);
  const [message, setMessage] = useState('');
  const [profile, setProfile] = useState(null);
  const [friend, setFriend] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    onMessage(friendId, (event) => {
      setTotalMessages(totalMessages + 1);
      setMessages(messages.concat([event.message]));
    });
    return () => {
      unsubscribeMessage(friendId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [friendId, messages]);

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

  if (error) return JSON.stringify(error);

  return (
    <Modal className="messages-container" show={show} onClose={onClose}>
      <button
        className="name pixel-corners"
        onClick={() => friend?.id > 0 && navigate(`/home/${friend?.id}`)}
      >
        {friend?.name || 'SYSTEM'}
      </button>
      {loading ? (
        <div className="messages">
          <p>Loading...</p>
        </div>
      ) : totalMessages > 0 ? (
        <ScrollLoader
          total={totalMessages}
          loadMore={loadMore}
          className="messages"
          topLoad
        >
          {messages.map((message) => (
            <div
              className={`message ${sentOrReceived(message)}`}
              key={`message-${message.id}`}
            >
              <p>{message.message}</p>
            </div>
          ))}
        </ScrollLoader>
      ) : (
        <div className="messages">
          <p>No messages</p>
        </div>
      )}
      <div className="buttons">
        <button onClick={back}>back</button>
        <textarea
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={friendId === 0}
        />
        <button disabled={friendId === 0} onClick={send}>
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
  setShowThreads: PropTypes.func.isRequired
};

Messages.displayName = 'Messages';

export default Messages;
