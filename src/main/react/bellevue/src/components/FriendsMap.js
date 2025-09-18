import { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getMyFriends } from '../api/api.js';
import Page from '../components/Page.js';
import ImageButton from './ImageButton.js';

function FriendsMap({ pushAlert }) {
  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const [myFriends, setMyFriends] = useState(undefined);
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
    setLoading(true);
    getMyFriends(
      (friends) => {
        setMyFriends(friends);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      },
      query
    );
  }, [query]);

  const loadMyFriendPage = (page) => {
    setLoading(true);
    getMyFriends(
      (friends) => {
        setMyFriends(friends);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      },
      query,
      [],
      page
    );
  };

  const profileClick = (id) => {
    navigate('/home/' + id);
  };

  return (
    <div className="friends">
      <div className="header pixel-corners">
        <h2>Neighborhood</h2>
        <input
          type="text"
          placeholder="Search neighborhood..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="friends-select-input"
        />
      </div>
      <p className="pixel-corners">Visit a friend&apos;s house and say hello</p>
      <div className="grid">
        <Page
          page={myFriends}
          renderItem={(friend) => (
            <ImageButton
              name="house"
              size="medium"
              face={false}
              onClick={() => profileClick(friend.id)}
            >
              <span>{friend.name}</span>
              <img
                className={`image ${friend?.avatar} small`}
                src={require(`../asset/${friend?.avatar}-small.png`)}
                alt={friend?.avatar}
              />
              <img
                className="image face small"
                src={require('../asset/face-small.png')}
                alt={'face'}
              />
              {friend?.equipment?.hat && (
                <img
                  className={`image ${friend?.equipment?.hat} small`}
                  src={require(`../asset/${friend?.equipment?.hat}-small.png`)}
                  alt={friend?.equipment?.hat}
                />
              )}
            </ImageButton>
          )}
          loadPage={loadMyFriendPage}
          loading={loading}
        />
      </div>
    </div>
  );
}

FriendsMap.propTypes = {
  pushAlert: PropTypes.func
};

FriendsMap.displayName = 'FriendsMap';

export default FriendsMap;
