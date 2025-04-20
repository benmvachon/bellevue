import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import { useAuth } from '../utils/AuthContext.js';
import { getMessages, sendMessage, markMessageRead } from '../api/api.js';
import InfiniteScroll from './InfiniteScroll.js';

function Message({ show = false, friend, onClose }) {
  const { userId } = useAuth();
  const [messages, setMessages] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState(false);

  const sentOrReceived = (message) => {
    if ('' + message.receiver.id === '' + userId) return 'received';
    return 'sent';
  };

  useEffect(() => {
    if (show) getMessages(friend, setMessages, setMessages);
  }, [show, friend]);

  useEffect(() => {
    if (messages && messages.content && messages.content.length) {
      messages.content.forEach((message) => {
        if ('received' === sentOrReceived(message) && !message.read)
          markMessageRead(friend, message.id);
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [friend, messages]);

  const loadMore = (page) => {
    getMessages(
      friend,
      (more) => {
        if (more) {
          more.content = more?.content?.concat(messages?.content);
          more.number = more.number + messages?.number || 0;
          setMessages(more);
        }
      },
      setError,
      page
    );
  };

  const send = () => {
    sendMessage(friend, message, () => {
      getMessages(friend, setMessages, setMessages);
      setMessage('');
    });
  };

  if (!show) return;
  if (error) return JSON.stringify(error);

  return (
    <div className="modal-container">
      <div className="modal messages-container">
        <div className="messages">
          <InfiniteScroll
            page={messages}
            renderItem={(message) => (
              <div
                className={`message ${sentOrReceived(message)}`}
                key={`message-${message.id}`}
              >
                <p>{message.message}</p>
              </div>
            )}
            loadMore={loadMore}
            reverse
          />
        </div>
        <div className="buttons">
          <textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
          />
          <button onClick={onClose}>Close</button>
          <button onClick={send}>Send</button>
        </div>
      </div>
    </div>
  );
}

Message.propTypes = {
  show: PropTypes.bool,
  friend: PropTypes.number.isRequired,
  onClose: PropTypes.func.isRequired
};

export default withAuth(Message);
