import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSuggestedFriends } from '../api/api.js';
import Page from '../components/Page.js';

function SuggestedFriendsMap() {
  const navigate = useNavigate();
  const [suggestedFriends, setSuggestedFriends] = useState(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const loadSuggestedFriendPage = (page) => {
    setLoading(true);
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
  };

  const profileClick = (event) => {
    event.preventDefault();
    navigate('/profile/' + event.target.value);
  };

  useEffect(() => {
    setLoading(true);
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
  }, []);

  if (error) return JSON.stringify(error);

  return (
    <div className="suggested-friends">
      <h2>Suggested Friends</h2>
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
