import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import {
  getForum,
  getForums,
  getMyFriends,
  getSuggestedFriends,
  markPostsRead
} from '../api/api.js';
import Header from '../components/Header.js';
import Page from '../components/Page.js';
import Forum from '../components/Forum.js';

function HomePage() {
  const navigate = useNavigate();
  const [forums, setForums] = useState(undefined);
  const [myFriends, setMyFriends] = useState(undefined);
  const [suggestedFriends, setSuggestedFriends] = useState(undefined);
  const [filter, setFilter] = useState(false);
  const [error, setError] = useState(false);

  const loadForumPage = (page) => {
    getForums(setForums, setError, page, filter);
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

  useEffect(() => {
    getForums(setForums, setError, 0, filter);
    getMyFriends(setMyFriends, setError);
    getSuggestedFriends(setSuggestedFriends, setError);
  }, [filter]);

  useEffect(() => {
    if (
      forums &&
      forums.content &&
      !forums.content.find((forum) => forum.id === 1)
    ) {
      getForum(
        1,
        (forum) => {
          forums.content.push(forum);
          setForums({ ...forums });
        },
        setError
      );
    }
  }, [forums]);

  if (error) return JSON.stringify(error);

  return (
    <div className="page home-page">
      <Header />
      <h2>
        <button onClick={() => setFilter(!filter)}>
          {filter ? 'Show all' : 'Show unread'}
        </button>
        Forums
        <button onClick={markPostsRead}>Mark all as read</button>
      </h2>
      <div>
        <Page
          page={forums}
          renderItem={(forum) => <Forum id={forum.id} forumProp={forum} />}
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
