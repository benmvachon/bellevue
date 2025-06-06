import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useAuth } from '../utils/AuthContext.js';
import {
  getMessages,
  onMessage,
  unsubscribeMessage,
  getMessageCount,
  sendMessage,
  markMessageRead
} from '../api/api.js';
import ScrollLoader from './ScrollLoader.js';
import Modal from './Modal.js';

function Messages({ show = false, friend, onClose }) {
  const { userId } = useAuth();
  const [messages, setMessages] = useState([]);
  const [totalMessages, setTotalMessages] = useState(0);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const sentOrReceived = (message) => {
    if (message.receiver.id === userId) return 'received';
    return 'sent';
  };

  const loadMore = () => {
    if (totalMessages <= messages.length) return;
    const cursor = messages[0].created.getTime();
    getMessageCount(
      friend,
      (totalMessages) => {
        getMessages(
          friend,
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
    sendMessage(friend, message, () => {
      setMessage('');
    });
  };

  useEffect(() => {
    const sentOrReceived = (message) => {
      if (message.receiver.id === userId) return 'received';
      return 'sent';
    };
    messages?.forEach((message) => {
      if ('received' === sentOrReceived(message) && !message.read)
        markMessageRead(friend, message.id);
    });
  }, [friend, messages, userId]);

  useEffect(() => {
    onMessage(friend, (event) => {
      setMessages(messages.concat([event.message]));
    });
    return () => {
      unsubscribeMessage(friend);
    };
  }, [friend, messages]);

  useEffect(() => {
    if (show) {
      setLoading(true);
      getMessageCount(
        friend,
        (totalMessages) => {
          getMessages(
            friend,
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
  }, [show, friend]);

  if (!show) return;
  if (error) return JSON.stringify(error);

  return (
    <Modal className="messages-container" show={show} onClose={onClose}>
      {loading ? (
        <p>Loading...</p>
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
        <p>No messages</p>
      )}
      <div className="buttons">
        <textarea
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button onClick={onClose}>Close</button>
        <button onClick={send}>Send</button>
      </div>
    </Modal>
  );
}

Messages.propTypes = {
  show: PropTypes.bool,
  friend: PropTypes.number.isRequired,
  onClose: PropTypes.func.isRequired
};

Messages.displayName = 'Messages';

export default Messages;
