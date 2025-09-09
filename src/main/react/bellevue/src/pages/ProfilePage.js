import React, { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import { useAuth } from '../utils/AuthContext';
import {
  getProfile,
  requestFriend,
  acceptFriend,
  removeFriend,
  updateBlackboard,
  onProfileUpdate,
  favoriteProfile,
  unfavoriteProfile,
  unsubscribeProfile,
  onFriendshipStatusUpdate,
  unsubscribeFriendshipStatus
} from '../api/api.js';
import asPage from '../utils/asPage.js';
import ImageButton from '../components/ImageButton.js';
import FavoriteButton from '../components/FavoriteButton.js';

function ProfilePage({ openMessages }) {
  const { id } = useParams();
  const { userId } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const [blackboard, setBlackboard] = useState('');
  const debounceTimeout = useRef(null);

  useEffect(() => {
    onProfileUpdate(id, (message) => {
      if (
        message === 'blackboard' ||
        message === 'location' ||
        message === 'status'
      )
        getProfile(id, setProfile, setError);
    });
    onFriendshipStatusUpdate(id, () => getProfile(id, setProfile, setError));
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
    return () => {
      unsubscribeProfile(id);
      unsubscribeFriendshipStatus(id);
    };
  }, [id]);

  useEffect(() => {
    if (profile) setBlackboard(profile.blackboard || '');
  }, [profile]);

  const self = userId === Number.parseInt(id);

  const refresh = () => getProfile(id, setProfile, setError);

  const favorite = () => {
    favoriteProfile(id, refresh, setError);
  };

  const unfavorite = () => {
    unfavoriteProfile(id, refresh, setError);
  };

  if (error) return JSON.stringify(error);
  if (loading) return <p>Loading...</p>;

  const buttons = [];
  switch (profile?.friendshipStatus) {
    case 'SELF':
      break;
    case 'ACCEPTED':
      buttons.push(
        <button
          onClick={() => removeFriend(id, refresh)}
          key="remove"
          className="remove"
        >
          remove
        </button>
      );
      buttons.push(
        <ImageButton
          name="phone"
          onClick={() => openMessages(id)}
          className="message-button"
        >
          <span>message</span>
        </ImageButton>
      );
      buttons.push(
        <FavoriteButton
          favorited={profile.favorite}
          onClick={() => (profile.favorite ? unfavorite() : favorite())}
        />
      );
      break;
    case 'PENDING_YOU':
      buttons.push(
        <button onClick={() => acceptFriend(id, refresh)} key="accept">
          accept
        </button>
      );
      break;
    case 'PENDING_THEM':
      buttons.push(<button key="pending">request pending</button>);
      break;
    default:
      buttons.push(
        <button onClick={() => requestFriend(id, refresh)} key="request">
          request
        </button>
      );
      break;
  }

  const updateBlackBoard = (event) => {
    if (debounceTimeout.current) clearTimeout(debounceTimeout.current);
    debounceTimeout.current = setTimeout(() => {
      updateBlackboard(event.target.value || '', refresh, setError);
    }, 1000);
    event && event.preventDefault();
    setBlackboard(event.target.value || '');
  };

  return (
    <div className="page-contents">
      {self ? (
        <form>
          <textarea
            name="blackboard"
            className="chalkboard"
            value={blackboard || ''}
            onChange={updateBlackBoard}
            placeholder="This is your profile. Write anything you want here!"
          />
        </form>
      ) : (
        <form>
          <textarea
            name="blackboard"
            className="chalkboard"
            value={blackboard || ''}
            disabled
          />
        </form>
      )}
      {profile && (
        <div className="avatar">
          <img
            className={`image ${profile?.avatar} large`}
            src={require(`../asset/${profile?.avatar}-large.png`)}
            alt={profile?.avatar}
          />
          <img
            className="image face large"
            src={require('../asset/face-large.png')}
            alt="face"
          />
          {profile?.equipment?.hat && (
            <img
              className={`image ${profile?.equipment?.hat} large`}
              src={require(`../asset/${profile?.equipment?.hat}-large.png`)}
              alt={profile?.equipment?.hat}
            />
          )}
        </div>
      )}
      <div className="metadata pixel-corners">
        <h2>
          {profile?.name}&nbsp;
          <span className={`status ${profile?.status?.toLowerCase()}`}>
            ({profile?.status?.toLowerCase()})
          </span>
        </h2>
        <h3>{profile?.username}</h3>
        <div className="buttons">
          {self ? (
            <span className="self-indication">This is you</span>
          ) : (
            buttons
          )}
        </div>
      </div>
    </div>
  );
}

ProfilePage.propTypes = {
  openMessages: PropTypes.func.isRequired
};

ProfilePage.displayName = 'ProfilePage';

export default withAuth(asPage(ProfilePage, 'profile-page'));
