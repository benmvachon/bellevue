import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { useAuth } from '../utils/AuthContext';
import {
  getFriend,
  getFriendshipStatus,
  getFriendRecipes,
  getFriends
} from '../api/api.js';

function ProfilePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { userId } = useAuth();
  const [profile, setProfile] = useState(null);
  const [status, setStatus] = useState('UNSET');
  const [recipes, setRecipes] = useState(null);
  const [friends, setFriends] = useState(null);

  useEffect(() => {
    getFriend(id, setProfile, setProfile);
    getFriendshipStatus(id, setStatus, setStatus);
    getFriendRecipes(id, setRecipes, (err) => {
      setRecipes(JSON.stringify(err));
    });
    getFriends(id, setFriends, (err) => {
      setFriends(JSON.stringify(err));
    });
  }, [id]);

  const friendClick = (event) => {
    event.preventDefault();
    navigate('/profile/' + event.target.value);
  };

  const recipeClick = (event) => {
    event.preventDefault();
    navigate('/recipe/' + event.target.value);
  };

  return (
    <div className="page profile-page">
      <h1>{profile?.name}</h1>
      {userId === id ? <h4>This is you</h4> : null}
      <div>{profile?.username}</div>
      <h2>Status</h2>
      <div>{status}</div>
      <h2>Recipes</h2>
      <div>
        {recipes?.content?.map((recipe) => (
          <div key={`friend-${recipe.id}`}>
            <button value={recipe.id} onClick={recipeClick}>
              {recipe.name}
            </button>
          </div>
        ))}
      </div>
      <h2>Friends</h2>
      <div>
        {friends?.content?.map((friend) => (
          <div key={`friend-${friend.id}`}>
            <button value={friend.id} onClick={friendClick}>
              {friend.name}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default withAuth(ProfilePage);
