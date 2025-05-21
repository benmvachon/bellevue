import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getForums, getMyFriends, getSuggestedFriends } from '../api/api.js';
import Header from '../components/Header.js';
import Page from '../components/Page.js';

function HomePage() {
  const navigate = useNavigate();
  const [forums, setForums] = useState([]);
  const [myFriends, setMyFriends] = useState([]);
  const [suggestedFriends, setSuggestedFriends] = useState([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    getForums(setForums, setError);
    getMyFriends(setMyFriends, setError);
    getSuggestedFriends(setSuggestedFriends, setError);
  }, [setForums]);

  const loadForumPage = (page) => {
    getForums(setForums, setError, page);
  };

  const forumClick = (event) => {
    event.preventDefault();
    navigate(`/forum/${event.target.value}`);
  };

  const loadMyFriendPage = (page) => {
    getMyFriends(setMyFriends, setError, page);
  };

  const loadSuggestedFriendPage = (page) => {
    getSuggestedFriends(setSuggestedFriends, setError, page);
  };

  const profileClick = (event) => {
    event.preventDefault();
    navigate('/profile/' + event.target.value);
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="page home-page">
      <Header />
      <h2>Forums</h2>
      <div>
        <Page
          page={forums}
          renderItem={(forum) => (
            <div key={`forum-${forum.name}`}>
              <button value={forum.id} onClick={forumClick}>
                {forum.name}
              </button>
            </div>
          )}
          loadPage={loadForumPage}
        />
      </div>
      <h2>Friends</h2>
      <div>
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
      </div>
      <h2>Suggested Friends</h2>
      <div>
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
      </div>
    </div>
  );
}

export default withAuth(HomePage);
