import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { findUsers, getSuggestedFriends } from '../api/api.js';
import Page from '../components/Page.js';

function SuggestedFriendsMap() {
  const navigate = useNavigate();
  const [suggestedFriends, setSuggestedFriends] = useState(undefined);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

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
        page
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
        page
      );
    }
  };

  const profileClick = (event) => {
    event.preventDefault();
    navigate('/profile/' + event.target.value);
  };

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
        }
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
        }
      );
    }
  }, [query]);

  if (error) return JSON.stringify(error);

  return (
    <div className="suggested-friends">
      <h2>Suggested Friends</h2>
      <input
        type="text"
        placeholder="Search users..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        className="user-select-input"
      />
      <div>
        {loading ? (
          <p>Loading...</p>
        ) : (
          <Page
            page={suggestedFriends}
            renderItem={(profile) => (
              <div key={`profile-${profile.id}`}>
                <button value={profile.id} onClick={profileClick}>
                  {profile.name} - {profile.friendshipStatus}
                </button>
              </div>
            )}
            loadPage={loadSuggestedFriendPage}
          />
        )}
      </div>
    </div>
  );
}

SuggestedFriendsMap.displayName = 'SuggestedFriendsMap';

export default SuggestedFriendsMap;
