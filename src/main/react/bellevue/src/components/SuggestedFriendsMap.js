import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { findUsers, getSuggestedFriends } from '../api/api.js';
import Page from '../components/Page.js';
import ImageButton from './ImageButton.js';

function SuggestedFriendsMap() {
  const navigate = useNavigate();
  const [suggestedFriends, setSuggestedFriends] = useState(undefined);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

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

  if (error) return JSON.stringify(error);

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
      </div>
    </div>
  );
}

SuggestedFriendsMap.displayName = 'SuggestedFriendsMap';

export default SuggestedFriendsMap;
