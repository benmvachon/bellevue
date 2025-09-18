import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useOutletContext, useParams } from 'react-router-dom';
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
import ImageButton from '../components/ImageButton.js';
import FavoriteButton from '../components/FavoriteButton.js';
import ConfirmationDialog from '../components/ConfirmationDialog.js';

function ProfilePage() {
  const { id } = useParams();
  const { userId } = useAuth();
  const navigate = useNavigate();
  const { setClassName, setMapSlider, openMessages, pushAlert } =
    useOutletContext();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const [blackboard, setBlackboard] = useState('');
  const [showConfirmationDialog, setShowConfirmationDialog] = useState(false);
  const debounceTimeout = useRef(null);

  useEffect(() => {
    setClassName('profile-page');
    setMapSlider(true);
  }, [setClassName, setMapSlider]);

  useEffect(() => {
    if (error) {
      pushAlert({
        key: JSON.stringify(error),
        type: 'error',
        content: (
          <div>
            <h3>Error: {error.code}</h3>
            <p>{error.message}</p>
          </div>
        )
      });
      setError(false);
    }
  }, [pushAlert, error]);

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
      () => navigate('/error')
    );
    return () => {
      unsubscribeProfile(id);
      unsubscribeFriendshipStatus(id);
    };
  }, [navigate, id]);

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

  const openMap = () => {
    navigate(`/map/neighborhood/${id}`);
  };

  if (loading) return <p>Loading...</p>;

  const buttons = [];
  switch (profile?.friendshipStatus) {
    case 'SELF':
      buttons.push(
        <ImageButton name="map" onClick={openMap} className="map">
          <span>neighbors</span>
        </ImageButton>
      );
      break;
    case 'ACCEPTED':
      buttons.push(
        <ImageButton
          name="move-out"
          onClick={() => setShowConfirmationDialog(true)}
          key="remove"
        >
          <span>remove</span>
        </ImageButton>
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
      buttons.push(
        <ImageButton name="map" onClick={openMap} className="map">
          <span>neighbors</span>
        </ImageButton>
      );
      break;
    case 'PENDING_YOU':
      buttons.push(
        <ImageButton
          name="move-in"
          onClick={() => acceptFriend(id, refresh)}
          key="accept"
        >
          <span>accept</span>
        </ImageButton>
      );
      buttons.push(
        <ImageButton name="map" onClick={openMap} className="map">
          <span>neighbors</span>
        </ImageButton>
      );
      break;
    case 'PENDING_THEM':
      buttons.push(<p key="pending">request pending</p>);
      buttons.push(
        <ImageButton name="map" onClick={openMap} className="map">
          <span>neighbors</span>
        </ImageButton>
      );
      break;
    default:
      buttons.push(
        <ImageButton
          name="move-in"
          onClick={() => requestFriend(id, refresh)}
          key="request"
          className="request"
        >
          <span>request</span>
        </ImageButton>
      );
      buttons.push(
        <ImageButton name="map" onClick={openMap} className="map">
          <span>neighbors</span>
        </ImageButton>
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
        {self && <span className="self-indication">This is you</span>}
        <div className="buttons">{buttons}</div>
      </div>
      <ConfirmationDialog
        show={showConfirmationDialog}
        onConfirm={() => {
          removeFriend(id, refresh);
          setShowConfirmationDialog(false);
        }}
        onCancel={() => setShowConfirmationDialog(false)}
      />
    </div>
  );
}

ProfilePage.propTypes = {
  openMessages: PropTypes.func.isRequired
};

ProfilePage.displayName = 'ProfilePage';

export default withAuth(ProfilePage);
