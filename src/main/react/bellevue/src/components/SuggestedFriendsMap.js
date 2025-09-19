import { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { findUsers, getSuggestedFriends } from '../api/api.js';
import Page from '../components/Page.js';
import ImageButton from './ImageButton.js';

function SuggestedFriendsMap({ pushAlert }) {
  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const [suggestedFriends, setSuggestedFriends] = useState(undefined);
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
    if (query) {
      findUsers(
        query,
        (friends) => {
          setSuggestedFriends(friends);
          setLoading(false);
        },
        (error) => {
          setError(error);
          setLoading(false);
        },
        0,
        9
      );
    } else {
      getSuggestedFriends(
        (friends) => {
          setSuggestedFriends(friends);
          setLoading(false);
        },
        (error) => {
          setError(error);
          setLoading(false);
        },
        0,
        9
      );
    }
  }, [query]);

  const loadSuggestedFriendPage = (page) => {
    setLoading(true);
    if (query && query.trim().length) {
      findUsers(
        query,
        (friends) => {
          setSuggestedFriends(friends);
          setLoading(false);
        },
        (error) => {
          setError(error);
          setLoading(false);
        },
        page,
        9
      );
    } else {
      getSuggestedFriends(
        (friends) => {
          setSuggestedFriends(friends);
          setLoading(false);
        },
        (error) => {
          setError(error);
          setLoading(false);
        },
        page,
        9
      );
    }
  };

  const profileClick = (id) => {
    navigate('/home/' + id);
  };

  return (
    <div className="suggested-friends">
      <div className="header pixel-corners">
        <h2>Suburbs</h2>
        <input
          type="text"
          placeholder="Search suburbs..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="user-select-input"
        />
      </div>
      <p className="pixel-corners">
        Find potential neighbors and send them friend requests
      </p>
      <div className="grid">
        {loading ||
        (suggestedFriends &&
          suggestedFriends.content &&
          suggestedFriends.content.length > 0) ? (
          <Page
            page={suggestedFriends}
            renderItem={(profile) => (
              <ImageButton
                name="house"
                size="medium"
                face={false}
                onClick={() => profileClick(profile.id)}
              >
                <span>{profile.name}</span>
              </ImageButton>
            )}
            loadPage={loadSuggestedFriendPage}
            loading={loading}
          />
        ) : (
          <div className="empty-result-set">
            <h1>no results</h1>
            <p>try adjusting your query or your filters</p>
          </div>
        )}
      </div>
    </div>
  );
}

SuggestedFriendsMap.propTypes = {
  pushAlert: PropTypes.func
};

SuggestedFriendsMap.displayName = 'SuggestedFriendsMap';

export default SuggestedFriendsMap;
