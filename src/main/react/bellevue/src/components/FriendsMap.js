import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getMyFriends } from '../api/api.js';
import Page from '../components/Page.js';

function FriendsMap() {
  const navigate = useNavigate();
  const [myFriends, setMyFriends] = useState(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

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
      page
    );
  };

  const profileClick = (event) => {
    event.preventDefault();
    navigate('/profile/' + event.target.value);
  };

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
      }
    );
  }, []);

  if (error) return JSON.stringify(error);

  return (
    <div className="friends">
      <h2>Friends</h2>
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

export default withAuth(FriendsMap);
