import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useAuth } from '../utils/AuthContext.js';
import {
  getThreadCount,
  getThreads,
  onThread,
  markThreadsRead,
  unsubscribeThread,
  onThreadsRead,
  refreshThreads,
  unsubscribeThreadsRead
} from '../api/api.js';
import Thread from './Thread.js';
import ScrollLoader from './ScrollLoader.js';
import Modal from './Modal.js';

function Threads({ show = false, onClose, openMessages }) {
  const { userId } = useAuth();
  const [threads, setThreads] = useState(null);
  const [totalThreads, setTotalThreads] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (show) {
      const getOtherUser = (thread) => {
        if (thread.receiver.id === userId) return thread.sender;
        return thread.receiver;
      };
      onThread((event) => {
        const newThread = event.message;
        const friend = getOtherUser(newThread);
        getThreadCount((totalThreads) => {
          const index = threads?.findIndex(
            (thread) => friend.id === getOtherUser(thread).id
          );
          if (index > -1) {
            threads[index] = newThread;
            setThreads(threads);
          } else {
            setThreads([newThread].concat(threads));
          }
          setTotalThreads(totalThreads);
        }, setError);
      });
      return () => unsubscribeThread();
    }
  }, [show, threads, userId]);

  useEffect(() => {
    if (show) {
      if (threads && threads.length) {
        const cursor = threads[threads.length - 1].created.getTime();
        onThreadsRead(() => refreshThreads(setThreads, setError, cursor));
      }
      return unsubscribeThreadsRead;
    }
  }, [show, threads]);

  useEffect(() => {
    if (show) {
      setLoading(true);
      getThreadCount(
        (totalThreads) => {
          getThreads(
            (threads) => {
              setTotalThreads(totalThreads);
              setThreads(threads);
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
  }, [show]);

  const loadMore = () => {
    const cursor = threads[threads.length - 1].created.getTime();
    getThreadCount((totalThreads) => {
      getThreads(
        (more) => {
          setTotalThreads(totalThreads);
          if (more) {
            setThreads(threads?.concat(more));
          }
        },
        setError,
        cursor,
        5
      );
    }, setError);
  };

  const threadClick = (thread) => {
    const otherUser = getOtherUser(thread).id;
    openMessages(otherUser);
    onClose();
  };

  const getOtherUser = (thread) => {
    if (thread.receiver.id === userId) return thread.sender;
    return thread.receiver;
  };

  if (error) return JSON.stringify(error);
  if (!show) return;

  return (
    <Modal className="threads-container" show={show} onClose={onClose}>
      {loading ? (
        <p>Loading...</p>
      ) : totalThreads > 0 ? (
        <ScrollLoader
          total={totalThreads}
          loadMore={loadMore}
          className="threads"
        >
          {threads?.map((thread) => (
            <Thread
              key={`thread-${thread.id}`}
              thread={thread}
              onClick={threadClick}
            />
          ))}
        </ScrollLoader>
      ) : (
        <p>No messages</p>
      )}
      <div className="buttons">
        <button onClick={onClose}>Close</button>
        <button onClick={() => markThreadsRead(onClose, setError)}>
          Mark all as read
        </button>
      </div>
    </Modal>
  );
}

Threads.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired
};

Threads.displayName = 'Threads';

export default Threads;
