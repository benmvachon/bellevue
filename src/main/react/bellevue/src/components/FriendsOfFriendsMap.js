import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getFriends, getProfile } from '../api/api.js';
import Page from '../components/Page.js';
import ImageButton from './ImageButton.js';

function FriendsOfFriendsMap({ id }) {
  const navigate = useNavigate();
  const [friend, setFriend] = useState(undefined);
  const [friends, setFriends] = useState(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    setLoading(true);
    getProfile(
      id,
      (friend) => {
        setFriend(friend);
        getFriends(
          id,
          (friends) => {
            setFriends(friends);
            setLoading(false);
          },
          (error) => {
            setError(error);
            setLoading(false);
          }
        );
      },
      (error) => {
        setError(error);
        setLoading(false);
      }
    );
  }, [id]);

  const loadMyFriendPage = (page) => {
    setLoading(true);
    getFriends(
      id,
      (friends) => {
        setFriends(friends);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      },
      [],
      page
    );
  };

  const profileClick = (id) => {
    navigate('/home/' + id);
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="friends friends-of-friends">
      <div className="header pixel-corners">
        <h2>{friend?.name}&apos;s Neighborhood</h2>
      </div>
      <p className="pixel-corners">
        Drop in on one of your neighbor&apos;s neighbors
      </p>
      <div className="grid">
        <Page
          page={friends}
          renderItem={(friend) => (
            <ImageButton
              name="house"
              size="medium"
              face={false}
              onClick={() => profileClick(friend.id)}
            >
              <span>{friend.name}</span>
              <img
                className={`image ${friend?.avatar} small`}
                src={require(`../asset/${friend?.avatar}-small.png`)}
                alt={friend?.avatar}
              />
              <img
                className="image face small"
                src={require('../asset/face-small.png')}
                alt={'face'}
              />
              {friend?.equipment?.hat && (
                <img
                  className={`image ${friend?.equipment?.hat} small`}
                  src={require(`../asset/${friend?.equipment?.hat}-small.png`)}
                  alt={friend?.equipment?.hat}
                />
              )}
            </ImageButton>
          )}
          loadPage={loadMyFriendPage}
          loading={loading}
        />
      </div>
    </div>
  );
}

FriendsOfFriendsMap.displayName = 'FriendsOfFriendsMap';

FriendsOfFriendsMap.propTypes = {
  id: PropTypes.number.isRequired
};

export default FriendsOfFriendsMap;
