import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyFriends } from '../api/api.js';
import Page from '../components/Page.js';

function FriendsMap() {
  const navigate = useNavigate();
  const [myFriends, setMyFriends] = useState(undefined);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

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

  const profileClick = (event) => {
    event.preventDefault();
    navigate('/home/' + event.target.value);
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="friends">
      <h2>Neighborhood</h2>
      <input
        type="text"
        placeholder="Search neighborhood..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        className="friends-select-input"
      />
      <div>
        {loading ? (
          <p>Loading...</p>
        ) : (
          <Page
            page={myFriends}
            renderItem={(friend) => (
              <div key={`friend-${friend.id}`}>
                <button value={friend.id} onClick={profileClick}>
                  {friend.name} - {friend.friendshipStatus}
                </button>
              </div>
            )}
            loadPage={loadMyFriendPage}
          />
        )}
      </div>
    </div>
  );
}

FriendsMap.displayName = 'FriendsMap';

export default FriendsMap;
