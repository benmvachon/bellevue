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
  getFriendsInLocation,
  onEntrance,
  unsubscribeProfile,
  unsubscribeLocation
} from '../api/api.js';
import { useNotifyLocationChange } from '../utils/LocationContext.js';
import Header from '../components/Header.js';
import Messages from '../components/Messages.js';
import InfiniteScroll from '../components/InfiniteScroll.js';
import Equipment from '../components/Equipment.js';

function ProfilePage() {
  const navigate = useNavigate();
  const { locationId, locationType } = useNotifyLocationChange();
  const { id } = useParams();
  const { userId } = useAuth();
  const [profile, setProfile] = useState(null);
  const [friends, setFriends] = useState(null);
  const [attendees, setAttendees] = useState(null);
  const [showMessages, setShowMessages] = useState(false);
  const [error, setError] = useState(false);
  const [blackboard, setBlackboard] = useState('');
  const [showEquipment, setShowEquipment] = useState(false);

  const self = '' + userId === '' + id;

  const refresh = () => getProfile(id, setProfile, setError);

  const refreshAttendees = () => getFriendsInLocation(setAttendees, setError);

  const favorite = () => {
    favoriteProfile(id, refresh, setError);
  };

  const unfavorite = () => {
    unfavoriteProfile(id, refresh, setError);
  };

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

  const loadMoreAttendees = (page) => {
    getFriendsInLocation(
      id,
      'FORUM',
      (more) => {
        if (more) {
          more.content = attendees?.content?.concat(more?.content);
          setAttendees(more);
        }
      },
      setError,
      page
    );
  };

  useEffect(() => {
    refresh();
    getFriends(id, setFriends, setError);
    refreshAttendees();
    onProfileUpdate(id, () => {
      refresh();
      getFriends(id, setFriends, setError);
    });
    onEntrance(refreshAttendees);
    return () => {
      unsubscribeProfile(id);
      unsubscribeLocation();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    refreshAttendees();
  }, [locationId, locationType]);

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
      <div className="attendees">
        <h3>Attendees</h3>
        <InfiniteScroll
          page={attendees}
          renderItem={(attendee) => (
            <button onClick={() => navigate(`/profile/${attendee.id}`)}>
              {attendee.name}
            </button>
          )}
          loadMore={loadMoreAttendees}
        />
      </div>
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
