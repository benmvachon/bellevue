import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import {
  getCategories,
  getMyFriends,
  getSuggestedFriends
} from '../api/api.js';
import Header from '../components/Header.js';
import Page from '../components/Page.js';

function HomePage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [myFriends, setMyFriends] = useState([]);
  const [suggestedFriends, setSuggestedFriends] = useState([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    getCategories(setCategories, setError);
    getMyFriends(setMyFriends, setError);
    getSuggestedFriends(setSuggestedFriends, setError);
  }, [setCategories]);

  const loadCategoryPage = (page) => {
    getCategories(setCategories, setError, page);
  };

  const categoryClick = (event) => {
    event.preventDefault();
    navigate(`/category/${event.target.value}`);
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
      <h2>Categories</h2>
      <div>
        <Page
          page={categories}
          renderItem={(category) => (
            <div key={`category-${category.name}`}>
              <button value={category.name} onClick={categoryClick}>
                {category.name}
              </button>
            </div>
          )}
          loadPage={loadCategoryPage}
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
