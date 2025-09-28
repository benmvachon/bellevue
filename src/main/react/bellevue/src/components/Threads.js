import React, { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useAuth } from '../utils/AuthContext.js';
import {
  getProfile,
  getThreadCount,
  getThreads,
  getMyFriends,
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
import Avatar from './Avatar.js';
import LoadingSpinner from './LoadingSpinner.js';
import Button from './Button.js';

function Threads({ show = false, onClose, openMessages, pushAlert }) {
  const outletContext = useOutletContext();
  const { userId } = useAuth();
  const [threads, setThreads] = useState(null);
  const [totalThreads, setTotalThreads] = useState(0);
  const [friends, setFriends] = useState(undefined);
  const [profile, setProfile] = useState(null);
  const [query, setQuery] = useState('');
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
    if (show) {
      getProfile(userId, setProfile, setError);
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
    if (query && query.length) {
      setLoading(true);
      getMyFriends(
        (friends) => {
          setFriends(friends);
          setLoading(false);
        },
        (error) => {
          setError(error);
          setLoading(false);
        },
        query
      );
    } else {
      setFriends(undefined);
    }
  }, [query]);

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

  return (
    <Modal className="threads-container" show={show} onClose={onClose}>
      <input
        type="text"
        placeholder="Search neighbors..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        className="thread-search"
        id="thread-search"
        name="thread-search"
      />
      {loading ? (
        <div className="threads">
          <LoadingSpinner onClick={() => {}} />
        </div>
      ) : query && query.length ? (
        friends?.content?.length > 0 ? (
          <div className="threads">
            {friends.content.map((friend) => (
              <Thread
                key={`friend-${friend.id}`}
                thread={{
                  message: 'new message',
                  sender: { id: userId },
                  receiver: friend,
                  read: true
                }}
                onClick={threadClick}
              />
            ))}
          </div>
        ) : (
          <div className="threads">
            <p>No neighbors matching query</p>
          </div>
        )
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
        <div className="threads">
          <p>No messages</p>
        </div>
      )}
      <div className="buttons">
        <Button onClick={onClose}>close</Button>
        <Button onClick={() => markThreadsRead(undefined, setError)}>
          mark all read
        </Button>
      </div>
      {profile && (
        <Avatar
          userProp={profile}
          size="medium"
          flip
          className="self"
          name={false}
        />
      )}
      <div className="phone-container">
        <img
          className="image phone small"
          src={require('../asset/phone-small.png')}
          alt="phone"
        />
        <img
          className="image face small"
          src={require('../asset/face-small.png')}
          alt="face"
        />
      </div>
    </Modal>
  );
}

Threads.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  openMessages: PropTypes.func.isRequired,
  pushAlert: PropTypes.func
};

Threads.displayName = 'Threads';

export default Threads;
