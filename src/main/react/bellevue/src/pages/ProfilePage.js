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
  favoriteProfile,
  unfavoriteProfile
} from '../api/api.js';
import Header from '../components/Header.js';
import Messages from '../components/Messages.js';
import InfiniteScroll from '../components/InfiniteScroll.js';
import Equipment from '../components/Equipment.js';

function ProfilePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { userId } = useAuth();
  const [profile, setProfile] = useState(null);
  const [friends, setFriends] = useState(null);
  const [showMessages, setShowMessages] = useState(false);
  const [error, setError] = useState(false);
  const [blackboard, setBlackboard] = useState('');
  const [showEquipment, setShowEquipment] = useState(false);

  const self = '' + userId === '' + id;

  const refresh = () => getProfile(id, setProfile, setError);

  const favorite = () => {
    favoriteProfile(id, refresh, setError);
  };

  const unfavorite = () => {
    unfavoriteProfile(id, refresh, setError);
  };

  useEffect(() => {
    refresh();
    getFriends(id, setFriends, setError);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (profile) setBlackboard(profile.blackboard);
  }, [profile]);

  const loadMoreFriends = (page) => {
    getFriends(
      id,
      (more) => {
        if (more) {
          more.content = friends?.content?.concat(more.content);
          more.number = more.number + friends?.number || 0;
          setFriends(more);
        }
      },
      setError,
      page
    );
  };

  const friendClick = (event) => {
    event.preventDefault();
    navigate('/profile/' + event.target.value);
  };

  const openMessages = () => setShowMessages(true);
  const closeMessages = () => setShowMessages(false);

  const openEquipment = () => setShowEquipment(true);
  const closeEquipment = () => setShowEquipment(false);

  if (error) return JSON.stringify(error);
  if (!profile) return;

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
    <div className="page profile-page">
      <Header />
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
      <h3>Friends</h3>
      <div>
        <InfiniteScroll
          page={friends}
          renderItem={(friend) => (
            <div key={`friend-${friend.id}`}>
              <button value={friend.id} onClick={friendClick}>
                {friend.name} - {friend.friendshipStatus}
              </button>
            </div>
          )}
          loadMore={loadMoreFriends}
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

export default withAuth(ProfilePage);
