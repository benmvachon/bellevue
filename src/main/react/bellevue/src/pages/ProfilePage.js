import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { useAuth } from '../utils/AuthContext';
import {
  getProfile,
  getFriends,
  requestFriend,
  acceptFriend,
  removeFriend,
  blockUser,
  updateBlackboard,
  onProfileUpdate,
  favoriteProfile,
  unfavoriteProfile,
  unsubscribeProfile,
  onFriendshipStatusUpdate,
  unsubscribeFriendshipStatus
} from '../api/api.js';
import Messages from '../components/Messages.js';
import Equipment from '../components/Equipment.js';
import Attendees from '../components/Attendees.js';
import Page from '../components/Page.js';
import asPage from '../utils/asPage.js';

function ProfilePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { userId } = useAuth();
  const [profile, setProfile] = useState(null);
  const [friends, setFriends] = useState(null);
  const [showMessages, setShowMessages] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const [blackboard, setBlackboard] = useState('');
  const [showEquipment, setShowEquipment] = useState(false);

  const self = userId === id;

  const refresh = () => getProfile(id, setProfile, setError);

  const favorite = () => {
    favoriteProfile(id, refresh, setError);
  };

  const unfavorite = () => {
    unfavoriteProfile(id, refresh, setError);
  };

  const loadFriendsPage = (page) => {
    getFriends(id, setFriends, setError, page);
  };

  useEffect(() => {
    setLoading(true);
    getProfile(
      id,
      (profile) => {
        setProfile(profile);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      }
    );
    getFriends(id, setFriends, setError);
    onProfileUpdate(id, (message) => {
      if (
        message === 'blackboard' ||
        message === 'location' ||
        message === 'status'
      )
        getProfile(id, setProfile, setError);
    });
    onFriendshipStatusUpdate(id, () => getProfile(id, setProfile, setError));
    return () => {
      unsubscribeProfile(id);
      unsubscribeFriendshipStatus(id);
    };
  }, [id]);

  useEffect(() => {
    if (profile) setBlackboard(profile.blackboard);
  }, [profile]);

  const friendClick = (event) => {
    event.preventDefault();
    navigate('/profile/' + event.target.value);
  };

  const openMessages = () => setShowMessages(true);
  const closeMessages = () => setShowMessages(false);

  const openEquipment = () => setShowEquipment(true);
  const closeEquipment = () => setShowEquipment(false);

  if (error) return JSON.stringify(error);
  if (loading) return <p>Loading...</p>;

  const buttons = [];
  switch (profile?.friendshipStatus) {
    case 'SELF':
      buttons.push(<button onClick={openEquipment}>Equipment</button>);
      break;
    case 'ACCEPTED':
      buttons.push(
        <button onClick={openMessages} key="message">
          Message
        </button>
      );
      buttons.push(
        <button onClick={() => removeFriend(id, refresh)} key="remove">
          Remove
        </button>
      );
      if (profile.favorite)
        buttons.push(
          <button onClick={unfavorite} key="unfavorite">
            Unfavorite
          </button>
        );
      else
        buttons.push(
          <button onClick={favorite} key="favorite">
            Favorite
          </button>
        );
      buttons.push(
        <button onClick={() => blockUser(id, refresh)} key="block">
          Block
        </button>
      );
      break;
    case 'PENDING_YOU':
      buttons.push(
        <button onClick={() => acceptFriend(id, refresh)} key="accept">
          Accept
        </button>
      );
      buttons.push(
        <button onClick={() => blockUser(id, refresh)} key="block">
          Block
        </button>
      );
      break;
    case 'PENDING_THEM':
      buttons.push(
        <button onClick={() => blockUser(id, refresh)} key="block">
          Block
        </button>
      );
      break;
    default:
      buttons.push(
        <button onClick={() => blockUser(id, refresh)} key="block">
          Block
        </button>
      );
      buttons.push(
        <button onClick={() => requestFriend(id, refresh)} key="request">
          Request
        </button>
      );
      break;
  }

  return (
    <div className="page-contents">
      <h2>{profile?.name}</h2>
      <p>{profile?.username}</p>
      <p>
        {profile?.avatar} {JSON.stringify(profile?.equipment)}
      </p>
      {self ? <p>This is you</p> : <p>{profile?.status}</p>}
      <p>
        {profile?.locationType} - {profile?.location?.id}
      </p>
      {buttons}
      {self ? (
        <form onSubmit={() => updateBlackboard(blackboard, refresh, setError)}>
          <textarea
            value={blackboard}
            onChange={(e) => {
              setBlackboard(e.target.value);
            }}
          />
          <button type="submit">Update blackboard</button>
        </form>
      ) : (
        <p>{blackboard}</p>
      )}
      <Attendees />
      <h3>Friends</h3>
      <div>
        <Page
          page={friends}
          renderItem={(friend) => (
            <div key={`friend-${friend.id}`}>
              <button value={friend.id} onClick={friendClick}>
                {friend.name} - {friend.friendshipStatus}
              </button>
            </div>
          )}
          loadPage={loadFriendsPage}
        />
      </div>
      <Messages show={showMessages} onClose={closeMessages} friend={id} />
      {self && (
        <Equipment
          show={showEquipment}
          onClose={closeEquipment}
          refreshProfile={refresh}
        />
      )}
    </div>
  );
}

ProfilePage.displayName = 'ProfilePage';

export default withAuth(asPage(ProfilePage, 'profile-page'));
