import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getThreads, markThreadRead } from '../api/api.js';
import InfiniteScroll from './InfiniteScroll.js';

function Threads({ show = false, onClose, openMessages }) {
  const navigate = useNavigate();
  const [threads, setThreads] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (show) getThreads(setThreads, setError);
  }, [show]);

  const loadMore = (page) => {
    getThreads(
      (more) => {
        if (more) {
          more.content = threads?.content?.concat(more?.content);
          more.number = more.number + threads?.number || 0;
          setThreads(more);
        }
      },
      setError,
      page
    );
  };

  const markAsRead = (thread) => {
    markThreadRead(thread);
  };

  const profileClick = (thread) => {
    navigate(`/profile/${thread}`);
    onClose();
  };

  const threadClick = (thread) => {
    openMessages(thread);
    onClose();
  };

  if (error) return JSON.stringify(error);
  if (!show) return;

  return (
    <div className="modal-container">
      <div className="modal threads-container">
        <div className="threads">
          <InfiniteScroll
            page={threads}
            renderItem={(thread) => (
              <div className="thread" key={`thread-${thread.id}`}>
                <button onClick={() => profileClick(thread.id)}>
                  {thread.name}
                </button>
                <button onClick={() => threadClick(thread.id)}>Message</button>
                <button onClick={() => markAsRead(thread.id)}>Mark read</button>
              </div>
            )}
            loadMore={loadMore}
          />
        </div>
        <div className="buttons">
          <button onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

Threads.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired
};

export default withAuth(Threads);
