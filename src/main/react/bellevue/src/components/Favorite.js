import React, { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getForum, getPost, getProfile } from '../api/api.js';
import LoadingSpinner from './LoadingSpinner.js';

function Favorite({ favorite, pushAlert }) {
  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const [entity, setEntity] = useState(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (error) {
      let func = pushAlert;
      if (outletContext) func = outletContext.pushAlert;
      func({
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
  }, [outletContext, pushAlert, error]);

  useEffect(() => {
    setLoading(true);
    let request = getProfile;
    switch (favorite.type.toLowerCase()) {
      case 'profile':
        request = getProfile;
        break;
      case 'forum':
        request = getForum;
        break;
      case 'post':
        request = getPost;
        break;
      default:
        break;
    }
    request(
      favorite.entity,
      (entity) => {
        setEntity(entity);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      }
    );
  }, [favorite]);

  const getType = (type) => {
    switch (type.toLowerCase()) {
      case 'profile':
        return 'home';
      case 'forum':
        return 'town';
      case 'post':
        return 'flyer';
      default:
        return type.toLowerCase();
    }
  };

  const navigateToFavorite = (favorite) => {
    navigate(`/${getType(favorite.type)}/${favorite.entity}`);
  };

  const renderFavorite = (favorite) => {
    switch (favorite.type.toLowerCase()) {
      case 'profile':
        return `${entity?.name}'s profile`;
      case 'forum':
        return `the ${entity?.name} building`;
      case 'post':
        return `flyer posted by ${entity?.user?.name} to the ${entity?.forum?.name} building`;
      default:
        return favorite.type.toLowerCase();
    }
  };

  return (
    <div className="favorite">
      {loading ? (
        <LoadingSpinner onClick={() => {}} size="extra-small" />
      ) : (
        <button onClick={() => navigateToFavorite(favorite)}>
          {renderFavorite(favorite)}
        </button>
      )}
    </div>
  );
}

Favorite.propTypes = {
  favorite: PropTypes.object.isRequired,
  pushAlert: PropTypes.func.isRequired
};

Favorite.displayName = 'Favorite';

export default Favorite;
